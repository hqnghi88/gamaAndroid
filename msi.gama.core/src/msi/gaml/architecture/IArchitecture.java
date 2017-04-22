/*********************************************************************************************
 *
 * 'IArchitecture.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.architecture;

import msi.gama.common.interfaces.ISkill;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.IStatement;

/**
 * Written by drogoul Modified on 12 sept. 2010
 * 
 * @todo Description
 * 
 */
public interface IArchitecture extends ISkill, IStatement {

	public abstract boolean init(IScope scope) throws GamaRuntimeException;

	public abstract boolean abort(IScope scope) throws GamaRuntimeException;

	public abstract void verifyBehaviors(ISpecies context);

	public abstract void preStep(final IScope scope, IPopulation<? extends IAgent> gamaPopulation);
}