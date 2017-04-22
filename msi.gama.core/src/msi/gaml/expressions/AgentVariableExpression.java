/*********************************************************************************************
 *
 * 'AgentVariableExpression.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.expressions;

import msi.gama.precompiler.GamlProperties;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.ICollector;
import msi.gaml.compilation.AbstractGamlAdditions;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.VariableDescription;
import msi.gaml.types.IType;

public class AgentVariableExpression extends VariableExpression implements IVarExpression.Agent {

	@SuppressWarnings ("rawtypes")
	protected AgentVariableExpression(final String n, final IType type, final boolean notModifiable,
			final IDescription def) {
		super(n, type, notModifiable, def);
	}

	@Override
	public IExpression getOwner() {
		return new SelfExpression(this.getDefinitionDescription().getSpeciesContext().getType());
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return scope.getAgentVarValue(scope.getAgent(), getName());
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
		scope.setAgentVarValue(scope.getAgent(), getName(), v);
	}

	@Override
	public String getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		String s = "Type " + type.getTitle();
		final String doc = AbstractGamlAdditions.TEMPORARY_BUILT_IN_VARS_DOCUMENTATION.get(name);
		if (doc != null)
			s += "<br>" + doc;
		if (desc == null)
			return s;
		final String quality =
				(desc.isBuiltIn() ? "<br>Built In " : doc == null ? "<br>Defined in " : "<br>Redefined in ")
						+ desc.getTitle();

		return s + quality;
	}

	/**
	 * Method collectPlugins()
	 * 
	 * @see msi.gama.common.interfaces.IGamlDescription#collectPlugins(java.util.Set)
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (getDefinitionDescription().isBuiltIn()) {
			meta.put(GamlProperties.ATTRIBUTES, getName());
		}
	}

	@Override
	public void collectUsedVarsOf(final IDescription species, final ICollector<VariableDescription> result) {
		if (species.equals(this.getDefinitionDescription().getSpeciesContext()))
			result.add(getDefinitionDescription().getSpeciesContext().getAttribute(getName()));
	}

}
