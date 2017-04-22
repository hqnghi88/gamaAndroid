/*********************************************************************************************
 *
 * 'IntWrapper.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.operators.fastmaths;

public class IntWrapper {

	public int value;

	@Override
	public String toString() {
		return Integer.toString(this.value);
	}
}
