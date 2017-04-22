/*********************************************************************************************
 * 
 *
 * 'IMoleExperiment.java', in plugin 'msi.gama.headless', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.headless.core;

import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.model.IModel;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gaml.expressions.IExpression;

public interface IExperiment { 
	public IModel getModel();
	public IExperimentPlan getExperimentPlan();
	
	public SimulationAgent getSimulation() ;
	
	public void setup(final String experimentName);
	public void setup(final String experimentName, final double seed);
	
	public long step();
	public boolean isInterrupted();
	
	public void setParameter(final String parameterName, final Object value);
	public Object getOutput(final String parameterName);
	public Object getVariableOutput(final String parameterName);
	public IExpression compileExpression(final String expression);
	public Object evaluateExpression(IExpression exp);
	public Object evaluateExpression(String exp);
	public void dispose();
	
	
}
