/*********************************************************************************************
 *
 * 'IGraphEventListener.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.util.graph;

import msi.gama.runtime.IScope;

public interface IGraphEventListener {

	public void receiveEvent(final IScope scope, GraphEvent event);

}
