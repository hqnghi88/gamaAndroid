/*********************************************************************************************
 *
 * 'UserStatusMessage.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.common;

import msi.gama.common.interfaces.IGui;
import msi.gama.util.GamaColor;

/**
 * Class UserStatusMessage.
 *
 * @author drogoul
 * @since 11 mars 2015
 *
 */
public class UserStatusMessage extends StatusMessage {

	GamaColor color;
	String icon;

	/**
	 * @param msg
	 * @param color
	 */
	public UserStatusMessage(final String msg, final GamaColor color, final String icon) {
		super(msg, IGui.USER);
		this.color = color;
		this.icon = icon;
	}

//	@Override
//	public GamaColor getColor() {
//		return color;
//	}

	@Override
	public String getIcon() {
		return icon;
	}

}
