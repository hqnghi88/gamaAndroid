/*********************************************************************************************
 *
 * 'PreferencesWiper.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.common.preferences;

import java.util.prefs.Preferences;

public class PreferencesWiper {

	public static void main(final String[] args) {
		try {
			Preferences store = Preferences.userRoot().node("gama");
			store.removeNode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}