/*********************************************************************************************
 *
 * 'GamaColor.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.util;


import java.util.Map;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.constant;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConstantCategory;
import msi.gama.precompiler.constants.ColorCSS;
import msi.gama.runtime.IScope;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import android.graphics.Color;
/**
 * The Class GamaColor. A simple wrapper on an AWT Color.
 *
 * @author drogoul
 */
@vars ({ @var (
		name = IKeyword.COLOR_RED,
		type = IType.INT,
		doc = { @doc ("Returns the red component of the color (between 0 and 255)") }),
		@var (
				name = IKeyword.COLOR_GREEN,
				type = IType.INT,
				doc = { @doc ("Returns the green component of the color (between 0 and 255)") }),
		@var (
				name = IKeyword.COLOR_BLUE,
				type = IType.INT,
				doc = { @doc ("Returns the blue component of the color (between 0 and 255)") }),
		@var (
				name = IKeyword.ALPHA,
				type = IType.INT,
				doc = { @doc ("Returns the alpha component (transparency) of the color (between 0 for transparent and 255 for opaque)") }),
		@var (
				name = IKeyword.BRIGHTER,
				type = IType.COLOR,
				doc = { @doc ("Returns a lighter color (with increased luminance)") }),
		@var (
				name = IKeyword.DARKER,
				type = IType.COLOR,
				doc = { @doc ("Returns a darker color (with decreased luminance)") }) })
public class GamaColor extends Color implements IValue, Comparable<Color>/* implements IContainer<Integer, Integer> */ {

	@constant (
			value = "the set of CSS colors",
			category = IConstantCategory.COLOR_CSS,
			concept = {},
			doc = @doc ("In addition to the previous units, GAML provides a direct access to the 147 named colors defined in CSS (see [http://www.cssportal.com/css3-color-names/]). E.g, {{{rgb my_color <- °teal;}}}")) public final static Object[] array =
					ColorCSS.array;

	public final static Map<String, GamaColor> colors = new THashMap<>();
	public final static TIntObjectMap<GamaColor> int_colors =
			TCollections.synchronizedMap(new TIntObjectHashMap<GamaColor>());

	public static GamaColor getInt(final int rgb) {
		GamaColor result = int_colors.get(rgb);
		if (result == null) {
			result = new GamaColor(rgb);
			int_colors.put(rgb, result);
		}
		return result;
	}

	public static GamaColor getNamed(final String rgb) {
		final GamaColor result = colors.get(rgb);
		return result;
	}

	static {
		for (int i = 0; i < array.length; i += 2) {
			final GamaColor color = new NamedGamaColor((String) array[i], (int[]) array[i + 1]);
			colors.put((String) array[i], color);
			int_colors.put(color.getRGB(), color);
		}
		// A.G add the GAMA Color corresponding to the GAMA1.7 Logo
		final GamaColor orange = new NamedGamaColor("gamaorange", new int[] { 244, 165, 40, 1 });
		colors.put("gamaorange", orange);
		int_colors.put(orange.getRGB(), orange);

		final GamaColor red = new NamedGamaColor("gamared", new int[] { 217, 72, 33, 1 });
		colors.put("gamared", red);
		int_colors.put(red.getRGB(), red);

		final GamaColor blue = new NamedGamaColor("gamablue", new int[] { 22, 94, 147, 1 });
		colors.put("gamablue", blue);
		int_colors.put(blue.getRGB(), blue);
	}

	public static class NamedGamaColor extends GamaColor {

		final String name;

		NamedGamaColor(final String n, final int[] c) {
			// c must be of length 4.
			super(c[0], c[1], c[2], (double) c[3]);
			name = n;
		}

		@Override
		public String toString() {
			return "color[" + name + "]";
		}

		@Override
		public String serialize(final boolean includingBuiltIn) {
			return "°" + name;
		}

		@Override
		public String stringValue(final IScope scope) {
			return name;
		}

	}

	private static int normalize(final int rgbComp) {
		return rgbComp < 0 ? 0 : rgbComp > 255 ? 255 : rgbComp;
	}

	public int getRGB() {
		// TODO Auto-generated method stub
		return 0;
	}

	// returns a value between 0 and 255 from a double between 0 and 1
	private static int normalize(final double transp) {
		return (int) (transp < 0 ? 0 : transp > 1 ? 255 : 255 * transp);
	}

	public GamaColor(final Color c) {
		super();
	}

	public GamaColor(final Color c, final int alpha) {
		this(c.red(0), c.green(0), c.blue(0), normalize(alpha));
	}

	public GamaColor(final Color c, final double alpha) {
		this(c.red(0), c.green(0), c.blue(0), normalize(alpha));
	}

	public GamaColor(final int awtRGB) {
		super();
	}

	public GamaColor(final int r, final int g, final int b) {
		this(normalize(r), normalize(g), normalize(b), 255);

	}

	public GamaColor(final int r, final int g, final int b, final int t) {
		// t between 0 and 255
		super();
	}

	public GamaColor(final double r, final double g, final double b, final double t) {
		// t between 0 and 1
		super();
	}

	public GamaColor(final int r, final int g, final int b, final double t) {
		// t between 0 and 1
		super();
	}

	/**
	 * @param is
	 */
	// public GamaColor(final int[] c) {
	// this(c[0], c[1], c[2], c[3]); // c[3] not considered yet
	// }

	@Override
	public String toString() {
		return serialize(true);
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return "rgb (" + red() + ", " + green() + ", " + blue() + "," + alpha() + ")";
	}

	@Override
	public String stringValue(final IScope scope) {
		return String.valueOf(getRGB());
	}

	@getter (IKeyword.COLOR_RED)
	public Integer red() {
		return super.red(0);
	}

	@getter (IKeyword.COLOR_BLUE)
	public Integer blue() {
		return super.blue(0);
	}

	@getter (IKeyword.COLOR_GREEN)
	public Integer green() {
		return super.green(0);
	}

	@getter (IKeyword.ALPHA)
	public Integer alpha() {
		return super.alpha(0);
	}

	@getter (IKeyword.BRIGHTER)
	public GamaColor getBrighter() {
		return new GamaColor(brighter());
	}

	private Color brighter() {
		// TODO Auto-generated method stub
		return null;
	}

	@getter (IKeyword.DARKER)
	public GamaColor getDarker() {
		return new GamaColor(darker());
	}

	private Color darker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GamaColor copy(final IScope scope) {
		return new GamaColor(this);
	}

	public static GamaColor merge(final GamaColor c1, final GamaColor c2) {
		return new GamaColor(c1.red() + c2.red(), c1.green() + c2.green(), c1.blue() + c2.blue(),
				c1.alpha() + c2.alpha());
	}

	public int compareRgbTo(final GamaColor c2) {
		return Integer.signum(getRGB() - c2.getRGB());
	}

	public int compareLuminescenceTo(final Color c2) {
		return Double.compare(this.red() * 0.299d + this.green() * 0.587d + this.blue() * 0.114d,
				c2.red(0) * 0.299d + c2.green(0) * 0.587d + c2.blue(0) * 0.114d);
	}

	public int compareBrightnessTo(final Color c2) {
		final float[] hsb = RGBtoHSB(red(), green(), blue(), null);
		final float[] hsb2 = RGBtoHSB(c2.red(0), c2.green(0), c2.blue(0), null);
		return Float.compare(hsb[2], hsb2[2]);
	}

	private float[] RGBtoHSB(Integer red, Integer green, Integer blue, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareLumaTo(final Color c2) {
		return Double.compare(this.red() * 0.21d + this.green() * 0.72d + this.blue() * 0.07d,
				c2.red(0) * 0.21d + c2.green(0) * 0.72d + c2.blue(0) * 0.07d);
	}

	@Override
	public int compareTo(final Color c2) {
		return compareRgbTo((GamaColor) c2);
	}

	/**
	 * Method getType()
	 * 
	 * @see msi.gama.common.interfaces.ITyped#getType()
	 */
	@Override
	public IType<?> getType() {
		return Types.COLOR;
	}

	public GamaColor withAlpha(final double d) {
		return new GamaColor(red(), green(), blue(), d);
	}

	public static Color getHSBColor(float floatValue, float floatValue2, float floatValue3) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Color decode(String s) {
		// TODO Auto-generated method stub
		return new GamaColor(Color.BLACK);
	}

}
