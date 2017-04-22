/*********************************************************************************************
 *
 * 'GamaColorType.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.types;

import android.graphics.Color;
import java.util.List;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.IContainer;
import msi.gaml.operators.Cast;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 * 
 * @todo Description
 * 
 */
@type (
		name = IKeyword.RGB,
		id = IType.COLOR,
		wraps = { GamaColor.class, Color.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.COLOR })
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaColorType extends GamaType<GamaColor> {

	@Override
	public GamaColor cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	public static GamaColor staticCast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// param can contain the alpha value
		if (obj == null) { return null; }
		if (obj instanceof GamaColor) {
			final GamaColor col = (GamaColor) obj;
			if (param instanceof Integer) {
				return new GamaColor(col.red(), col.green(), col.blue(), (Integer) param);
			} else if (param instanceof Double) {
				return new GamaColor(col.red(), col.green(), col.blue(), (Double) param);
			} else {
				return (GamaColor) obj;
			}
		}
		if (obj instanceof List) {
			final List l = (List) obj;
			final int size = l.size();
			if (size == 0) { return new GamaColor(Color.BLACK); }
			if (size == 1 || size == 2) {
				return staticCast(scope, ((List) obj).get(0), param, copy);
			} else if (size == 3) {
				return new GamaColor(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
						Cast.asInt(scope, l.get(2)), 255);
			} else if (size >= 4) { return new GamaColor(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
					Cast.asInt(scope, l.get(2)), Cast.asInt(scope, l.get(3))); }
			/* To allow constructions like rgb [255,255,255] */
		}
		if (obj instanceof IContainer) { return staticCast(scope,
				((IContainer) obj).listValue(scope, Types.NO_TYPE, false), param, copy); }
		if (obj instanceof String) {
			final String s = ((String) obj).toLowerCase();
			GamaColor c = GamaColor.colors.get(s);
			if (c == null) {
				try {
					c = new GamaColor(GamaColor.decode(s));
				} catch (final NumberFormatException e) {
					final GamaRuntimeException ex =
							GamaRuntimeException.error("'" + s + "' is not a valid color name", scope);
					throw ex;
				}
				GamaColor.colors.put(s, c);
			}
			if (param == null) {
				return c;
			} else if (param instanceof Integer) {
				return new GamaColor(c, (Integer) param);
			} else if (param instanceof Double) { return new GamaColor(c, (Double) param); }
		}
		if (obj instanceof Boolean) { return (Boolean) obj ? new GamaColor(Color.BLACK) : new GamaColor(Color.WHITE); }
		final int i = Cast.asInt(scope, obj);
		final GamaColor gc = GamaColor.getInt((255 & 0xFF) << 24 | i & 0xFFFFFF << 0);
		if (param instanceof Integer) {
			return new GamaColor(gc, (Integer) param);
		} else if (param instanceof Double) { return new GamaColor(gc, (Double) param); }
		return gc;
	}

	@Override
	public GamaColor getDefault() {
		return null; // new GamaColor(Color.black);
	}

	@Override
	public IType getContentType() {
		return Types.get(INT);
	}

	@Override
	public IType getKeyType() {
		return Types.get(INT);
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isCompoundType() {
		return true;
	}

}