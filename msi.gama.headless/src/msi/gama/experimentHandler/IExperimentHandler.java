/*********************************************************************************************
 * 
 *
 * 'IExperimentHandler.java', in plugin 'msi.gama.headless', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.experimentHandler;

import java.util.Observer;

import msi.gaml.expressions.IExpression;

public interface IExperimentHandler {
	//register an external reader to observe an experiment 
	void registerTriggedOutput(final String name, final Observer registredElement);	
	//read data value to be displayed 
	Object getOutputWithName(final String name);
	
	//register an expression to observe expression inside debugging mode
	void registerTriggedExpression(final IExpression exp, final Observer registredElement);
	//read the value of an expression in the experimentation 
	Object getEventWithName(final IExpression exp);
		
	//register an external reader to observe simulation status
	void registerTriggedExperimentStatus(final Observer registredElement);
	//read the current status of the simulation
	Status getSimulationStatus();
		
	enum Status {loaded, initialized, running, paused, stopped, error }	
	
}
