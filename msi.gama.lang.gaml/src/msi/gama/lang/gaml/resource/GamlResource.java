/*********************************************************************************************
 *
 * 'GamlResource.java, in plugin msi.gama.lang.gaml, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.lang.gaml.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.util.OnChangeEvictingCache;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import gnu.trove.map.hash.THashMap;
import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.lang.gaml.gaml.GamlPackage;
import msi.gama.lang.gaml.gaml.Model;
import msi.gama.lang.gaml.indexer.GamlResourceIndexer;
import msi.gama.runtime.IExecutionContext;
import msi.gama.util.GAML;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.compilation.ast.ISyntacticElement;
import msi.gaml.descriptions.ModelDescription;
import msi.gaml.descriptions.ValidationContext;
import msi.gaml.factories.ModelFactory;

/*
 *
 * The class GamlResource.
 *
 * @author drogoul
 *
 * @since 24 avr. 2012
 */
public class GamlResource extends LazyLinkingResource {

	private static boolean MEMOIZE_DESCRIPTION = false;
	ModelDescription description;
	ISyntacticElement element;

	public ValidationContext getValidationContext() {
		return GamlResourceServices.getValidationContext(this);
	}

	public boolean hasSemanticErrors() {
		return getValidationContext().hasErrors();
	}

	@Override
	public String getEncoding() {
		return "UTF-8";
	}

	@Override
	public String toString() {
		return "GamlResource[" + getURI().lastSegment() + "]";
	}

	public void updateWith(final ModelDescription model, final boolean newState) {
		GamlResourceServices.updateState(getURI(), model, newState, GamlResourceServices.getValidationContext(this));
	}

	public ISyntacticElement getSyntacticContents() {
		if (element == null)
			setElement(GamlResourceServices.buildSyntacticContents(this));
		return element;
	}

	private final static Function<GamlResource, ISyntacticElement> TO_SYNTACTIC_CONTENTS = input -> {
		input.getResourceSet().getResource(input.getURI(), true);
		return input.getSyntacticContents();
	};

	private ModelDescription buildModelDescription(final LinkedHashMultimap<String, GamlResource> resources) {

		// Initializations
		GAML.getExpressionFactory().resetParser();
		final ModelFactory f = GAML.getModelFactory();
		final String modelPath = GamlResourceServices.getModelPathOf(this);
		final String projectPath = GamlResourceServices.getProjectPathOf(this);
		final boolean isEdited = GamlResourceServices.isEdited(this);
		final ValidationContext context = GamlResourceServices.getValidationContext(this);
		// If the resources imported are null, no need to go through their
		// validation
		if (resources == null) {
			final List<ISyntacticElement> self = Collections.singletonList(getSyntacticContents());
			return f.createModelDescription(projectPath, modelPath, self, context, isEdited, null);
		}
		// If there are no micro-models
		final Set<String> keySet = resources.keySet();
		if (keySet.size() == 1 && keySet.contains(null)) {
			final Iterable<ISyntacticElement> selfAndImports =
					Iterables.concat(Collections.singleton(getSyntacticContents()),
							Multimaps.transformValues(resources, TO_SYNTACTIC_CONTENTS).get(null));
			return f.createModelDescription(projectPath, modelPath, selfAndImports, context, isEdited, null);
		}
		final ListMultimap<String, ISyntacticElement> models = ArrayListMultimap.create();
		models.put(null, getSyntacticContents());
		models.putAll(Multimaps.transformValues(resources, TO_SYNTACTIC_CONTENTS));
		final List<ISyntacticElement> ownImports = models.removeAll(null);
		final Map<String, ModelDescription> compiledMicroModels = new THashMap<String, ModelDescription>();
		for (final String aliasName : models.keySet()) {
			final ModelDescription mic = GAML.getModelFactory().createModelDescription(projectPath, modelPath,
					models.get(aliasName), context, isEdited, null);
			mic.setAlias(aliasName);
			compiledMicroModels.put(aliasName, mic);
		}
		return f.createModelDescription(projectPath, modelPath, ownImports, context, isEdited, compiledMicroModels);
	}

	public void invalidate(final GamlResource r, final String s) {
		GamlCompilationError error = null;
		if (GamlResourceIndexer.equals(r.getURI(), getURI())) {
			error = new GamlCompilationError(s, IGamlIssue.GENERAL, r.getContents().get(0), false, false);
		} else {
			error = new GamlCompilationError(s, IGamlIssue.GENERAL, r.getURI(), false, false);
		}
		getValidationContext().add(error);
		updateWith(null, true);
	}

	public ModelDescription buildCompleteDescription() {
		if (MEMOIZE_DESCRIPTION && description != null)
			return description;
		final LinkedHashMultimap<String, GamlResource> imports = GamlResourceIndexer.validateImportsOf(this);
		if (hasErrors() || hasSemanticErrors()) {
			setDescription(null);
			return null;
		}
		final ModelDescription model = buildModelDescription(imports);
		// If, for whatever reason, the description is null, we stop the
		// semantic validation
		if (model == null) {
			invalidate(this, "Impossible to validate " + URI.decode(getURI().lastSegment()) + " (check the logs)");
		}
		setDescription(model);
		return model;
	}

	/**
	 * Validates the resource by compiling its contents into a ModelDescription and discarding this ModelDescription
	 * afterwards
	 * 
	 * @note The errors will be available as part of the ValidationContext, which can later be retrieved from the
	 *       resource, and which contains semantic errors (as opposed to the ones obtained via resource.getErrors(),
	 *       which are syntactic errors), This collector can be probed for compilation errors via its hasErrors(),
	 *       hasInternalErrors(), hasImportedErrors() methods
	 *
	 */
	public void validate() {
		final ModelDescription model = buildCompleteDescription();
		if (model == null) {
			updateWith(null, true);
			return;
		}

		// We then validate it and get rid of the description. The
		// documentation is produced only if the resource is
		// marked as 'edited'
		final boolean edited = GamlResourceServices.isEdited(this.getURI());
		try {
			model.validate(edited);
			updateWith(model, true);
		} finally {
			if (!MEMOIZE_DESCRIPTION)
				if (edited) {
					GamlResourceServices.getResourceDocumenter().addCleanupTask(model);
				} else {
					model.dispose();
				}
		}

	}

	@Override
	protected void updateInternalState(final IParseResult oldParseResult, final IParseResult newParseResult) {
		super.updateInternalState(oldParseResult, newParseResult);
		setElement(null);
		setDescription(null);
	}

	@Override
	protected void clearInternalState() {
		super.clearInternalState();
		setElement(null);
		setDescription(null);
	}

	@Override
	protected void doUnload() {
		super.doUnload();
		setElement(null);
		setDescription(null);
	}

	private void setDescription(final ModelDescription model) {
		if (!MEMOIZE_DESCRIPTION)
			return;
		if (model == description)
			return;
		if (description != null)
			description.dispose();
		description = model;
	}

	private void setElement(final ISyntacticElement model) {
		if (model == element)
			return;
		if (element != null)
			element.dispose();
		element = model;
	}

	/**
	 * In the case of synthetic resources, pass the URI they depend on
	 * 
	 * @throws IOException
	 */
	public void loadSynthetic(final InputStream is, final IExecutionContext additionalLinkingContext)
			throws IOException {
		final OnChangeEvictingCache r = getCache();
		r.getOrCreate(this).set("linking", additionalLinkingContext);
		getCache().execWithoutCacheClear(this, new IUnitOfWork.Void<GamlResource>() {

			@Override
			public void process(final GamlResource state) throws Exception {
				state.load(is, null);
				EcoreUtil.resolveAll(GamlResource.this);
			}
		});
		r.getOrCreate(this).set("linking", null);

	}

	@Override
	public OnChangeEvictingCache getCache() {
		return (OnChangeEvictingCache) super.getCache();
	}

	@Override
	protected void doLinking() {
		// If the imports are not correctly updated, we cannot proceed
		final EObject faulty = GamlResourceIndexer.updateImports(this);
		if (faulty != null) {
			final EAttribute attribute = getContents().get(0) instanceof Model ? GamlPackage.Literals.IMPORT__IMPORT_URI
					: GamlPackage.Literals.HEADLESS_EXPERIMENT__IMPORT_URI;
			getErrors().add(new EObjectDiagnosticImpl(Severity.ERROR, IGamlIssue.IMPORT_ERROR,
					"Impossible to locate import", faulty, attribute, -1, null));
			return;
		}
		super.doLinking();
	}

	public boolean hasErrors() {
		return !getErrors().isEmpty() || getParseResult().hasSyntaxErrors();
	}

}
