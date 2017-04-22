/*********************************************************************************************
 *
 * 'StatementFactory.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.factories;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.factory;
import msi.gama.precompiler.ISymbolKind;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.PrimitiveDescription;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.descriptions.StatementRemoteWithChildrenDescription;
import msi.gaml.descriptions.StatementWithChildrenDescription;
import msi.gaml.descriptions.SymbolProto;
import msi.gaml.statements.Facets;

/**
 * Written by drogoul Modified on 8 févr. 2010
 *
 * @todo Description
 *
 */
@factory(handles = { ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.SINGLE_STATEMENT, ISymbolKind.BEHAVIOR,
		ISymbolKind.ACTION, ISymbolKind.LAYER, ISymbolKind.BATCH_METHOD, ISymbolKind.OUTPUT })
public class StatementFactory extends SymbolFactory implements IKeyword {

	public StatementFactory(final List<Integer> handles) {
		super(handles);
	}

	@Override
	protected StatementDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final SymbolProto proto) {
		if (proto.isPrimitive()) {
			return new PrimitiveDescription(enclosing, element, children, facets, null);
		}
		if (keyword.equals(ACTION))
			return new ActionDescription(keyword, enclosing, children, element, facets);
		if (proto.hasSequence() && children != null) {
			if (proto.isRemoteContext())
				return new StatementRemoteWithChildrenDescription(keyword, enclosing, children, proto.hasArgs(),
						element, facets, null);
			return new StatementWithChildrenDescription(keyword, enclosing, children, proto.hasArgs(), element, facets,
					null);
		}
		return new StatementDescription(keyword, enclosing, proto.hasArgs(), /* children, */ element, facets, null);
	}

}
