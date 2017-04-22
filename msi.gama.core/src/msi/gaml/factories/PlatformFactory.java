/*********************************************************************************************
 *
 * 'PlatformFactory.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.factories;

import static msi.gama.precompiler.ISymbolKind.PLATFORM;

import java.util.List;
import java.util.Set;

import msi.gama.precompiler.GamlAnnotations.factory;
import msi.gaml.compilation.IAgentConstructor;
import msi.gaml.descriptions.PlatformSpeciesDescription;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.statements.Facets;

@factory (
		handles = { PLATFORM })
public class PlatformFactory extends SpeciesFactory {

	public PlatformFactory(final List<Integer> handles) {
		super(handles);
	}

	@Override
	public SpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final SpeciesDescription superDesc, final SpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		DescriptionFactory.addSpeciesNameAsType(name);
		return new PlatformSpeciesDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
	}

}
