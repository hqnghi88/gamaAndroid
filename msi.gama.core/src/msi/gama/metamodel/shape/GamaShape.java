/*********************************************************************************************
 *
 * 'GamaShape.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.metamodel.shape;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.algorithm.PointLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.AssertionFailedException;

import msi.gama.common.geometry.AxisAngle;
import msi.gama.common.geometry.Envelope3D;
import msi.gama.common.geometry.GeometryUtils;
import msi.gama.common.geometry.ICoordinates;
import msi.gama.common.geometry.Rotation3D;
import msi.gama.common.geometry.Scaling3D;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * Written by drogoul Modified on 25 ao�t 2010
 *
 *
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaShape implements IShape /* , IContainer */ {

	protected Geometry geometry;
	private IAgent agent;

	// Property map to add all kinds of information (e.g to specify if the
	// geometry is a sphere, a
	// cube, etc...). Can be reused by subclasses (for example to store GIS
	// information)
	protected GamaMap attributes;
	// private Envelope3D envelope;

	public GamaShape(final Geometry geom) {
		setInnerGeometry(geom);
	}

	@Override
	public IType getType() {
		return Types.GEOMETRY;
	}

	public GamaShape(final Envelope3D env) {
		this(env == null ? null : env.toGeometry());
	}

	public GamaShape(final IShape geom) {
		this(geom, null);

	}

	/**
	 * Creates a GamaShape from a source and a (optional) geometry. If the geometry is null, the geometry of the source
	 * is used. In any case, we copy its attributes if present and if copyAttributes is true
	 * 
	 * @param source
	 * @param geom
	 * @param copyAttributes
	 */

	public GamaShape(final IShape source, final Geometry geom, final boolean copyAttributes) {
		this((Geometry) (geom == null ? source.getInnerGeometry().clone() : geom));
		if (copyAttributes)
			mixAttributes(source);
	}

	public GamaShape(final IShape source, final Geometry geom) {
		this(source, geom, true);
	}

	/**
	 * This is where the attributes of this shape and the attributes of an incoming shape are mixed. The default
	 * strategy is to copy all the attributes to this
	 * 
	 * @param source
	 */
	private void mixAttributes(final IShape source) {
		if (source == null) { return; }
		final GamaMap<String, Object> attr = (GamaMap<String, Object>) source.getAttributes();
		if (attr == null) { return; }
		for (final Map.Entry<String, Object> entry : attr.entrySet()) {
			if (entry.getValue() != source) {
				setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Same as above, but applies a (optional) rotation around a given vector and (optional) translation to the geometry
	 * 
	 * @param source
	 *            cannot be null
	 * @param geom
	 *            can be null
	 * @param rotation
	 *            can be null, expressed in degrees
	 * @param newLocation
	 *            can be null
	 */

	public GamaShape(final IShape source, final Geometry geom, final AxisAngle rotation, final ILocation newLocation) {
		this(source, geom);
		if (!isPoint() && rotation != null) {
			Double normalZ = null;
			if (is3D()) {
				normalZ = GeometryUtils.getContourCoordinates(geometry).getNormal(true).z;
			}
			final GamaPoint centroid = getLocation();
			final Rotation3D r = new Rotation3D.CenteredOn(rotation, centroid);
			geometry.apply(r);
			geometry.geometryChanged();
			if (normalZ != null) {
				final Double normalZ2 = GeometryUtils.getContourCoordinates(geometry).getNormal(true).z;
				if (normalZ > 0 && normalZ2 < 0) {
					setAttribute(DEPTH_ATTRIBUTE, -(Double) getAttribute(DEPTH_ATTRIBUTE));
				}
			}
		}
		if (newLocation != null) {
			setLocation(newLocation);
		}
	}

	/**
	 * Same as above, but applies a (optional) scaling to the geometry by specifying a bounding box or a set of
	 * coefficients.
	 * 
	 * @param source
	 *            cannot be null
	 * @param geom
	 *            can be null
	 * @param rotation
	 *            can be null, expressed in degrees
	 * @param newLocation
	 *            can be null
	 * @param isBoundingBox
	 *            indicates whether the previous parameter should be considered as an absolute bounding box (width,
	 *            height, ) or as a set of coefficients.
	 */
	public GamaShape(final IShape source, final Geometry geom, final AxisAngle rotation, final ILocation newLocation,
			final Scaling3D bounds, final boolean isBoundingBox) {
		this(source, geom, rotation, newLocation);
		if (bounds != null && !isPoint()) {
			final Envelope3D env = getEnvelope();
			final GamaPoint previous = getLocation();
			final boolean flat = env.isFlat();
			if (isBoundingBox) {
				geometry.apply(bounds.asBoundingBoxIn(env));
			} else {
				geometry.apply(bounds);
			}
			setLocation(previous);
			if (is3D()) {
				setAttribute(IShape.DEPTH_ATTRIBUTE,
						isBoundingBox ? bounds.getZ() : (Double) getAttribute(IShape.DEPTH_ATTRIBUTE) * bounds.getZ());
			}
		}
	}

	/**
	 * Same as above, but applies a (optional) scaling to the geometry by a given coefficient
	 * 
	 * @param source
	 *            cannot be null
	 * @param geom
	 *            can be null
	 * @param rotation
	 *            can be null, expressed in degrees
	 * @param newLocation
	 *            can be null
	 */
	public GamaShape(final IShape source, final Geometry geom, final AxisAngle rotation, final ILocation newLocation,
			final Double scaling) {
		this(source, geom, rotation, newLocation);
		if (scaling != null && !isPoint()) {
			final GamaPoint previous = getLocation();
			geometry.apply(Scaling3D.of(scaling));
			setLocation(previous);
			if (is3D())
				setAttribute(IShape.DEPTH_ATTRIBUTE, (Double) getAttribute(IShape.DEPTH_ATTRIBUTE) * scaling);
		}
	}

	@Override
	public boolean isMultiple() {
		return getInnerGeometry() instanceof GeometryCollection;
	}

	public boolean is3D() {
		return hasAttribute(DEPTH_ATTRIBUTE);
	}

	@Override
	public IList<GamaShape> getGeometries() {
		final IList<GamaShape> result = GamaListFactory.create(Types.GEOMETRY);
		if (isMultiple()) {
			for (int i = 0, n = getInnerGeometry().getNumGeometries(); i < n; i++) {
				result.add(new GamaShape(getInnerGeometry().getGeometryN(i)));
			}
		} else {
			result.add(this);
		}
		return result;
	}

	@Override
	public boolean isPoint() {
		if (geometry == null) { return false; }
		return geometry.getNumPoints() == 1;
	}

	@Override
	public boolean isLine() {
		return getInnerGeometry() instanceof LineString || getInnerGeometry() instanceof MultiLineString;
	}

	@Override
	public String stringValue(final IScope scope) {
		if (geometry == null) { return ""; }
		return SHAPE_WRITER.write(geometry);
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		if (isPoint()) { return getLocation().serialize(includingBuiltIn) + " as geometry"; }
		if (isMultiple()) { return getGeometries().serialize(includingBuiltIn) + " as geometry"; }
		final IList<GamaShape> holes = getHoles();
		String result = "";
		if (getInnerGeometry() instanceof LineString) {
			result = "polyline ("
					+ GamaListFactory.createWithoutCasting(Types.POINT, getPoints()).serialize(includingBuiltIn) + ")";
		} else {
			result = "polygon ("
					+ GamaListFactory.createWithoutCasting(Types.POINT, getPoints()).serialize(includingBuiltIn) + ")";
		}
		if (holes.isEmpty()) { return result; }
		for (final GamaShape g : holes) {
			result = "(" + result + ") - (" + g.serialize(includingBuiltIn) + ")";
		}
		return result;
	}

	@Override
	public String toString() {
		return getInnerGeometry().toText() + " at " + getLocation();
	}

	@Override
	public GamaPoint getLocation() {
		if (isPoint()) { return (GamaPoint) geometry.getCoordinate(); }
		return GeometryUtils.getContourCoordinates(geometry).getCenter();
	}

	@Override
	public void setLocation(final ILocation l) {
		if (isPoint()) {
			geometry = GeometryUtils.GEOMETRY_FACTORY.createPoint(l.toGamaPoint());
		}
		else {
			final GamaPoint previous = getLocation();
			final GamaPoint location = l.toGamaPoint();
			final double dx = location.x - previous.x;
			final double dy = location.y - previous.y;
			final double dz = location.z - previous.z;
			GeometryUtils.translate(geometry, dx, dy, dz);
		}
	}

	public GamaShape translatedTo(final IScope scope, final ILocation target) {
		final GamaShape result = copy(scope);
		result.setLocation(target);
		return result;
	}

	final static PointLocator pl = new PointLocator();

	@Override
	public GamaShape getGeometry() {
		return this;
	}

	@Override
	public Double getArea() {
		// WARNING only 2D (XY) area
		return getInnerGeometry().getArea();
	}

	@Override
	public Double getVolume() {
		return getEnvelope().getVolume();
	}

	@Override
	public double getPerimeter() {
		if (geometry instanceof GeometryCollection) {
			final int[] result = new int[1];
			GeometryUtils.applyToInnerGeometries((GeometryCollection) geometry,
					(g) -> result[0] += GeometryUtils.getContourCoordinates(g).getLength());
			return result[0];
		}
		final ICoordinates seq = GeometryUtils.getContourCoordinates(geometry);
		return seq.getLength();
	}

	@Override
	public IList<GamaShape> getHoles() {
		final IList<GamaShape> holes = GamaListFactory.create(Types.GEOMETRY);
		if (getInnerGeometry() instanceof Polygon) {
			final Polygon p = (Polygon) getInnerGeometry();
			final int n = p.getNumInteriorRing();
			for (int i = 0; i < n; i++) {
				holes.add(new GamaShape(
						GeometryUtils.GEOMETRY_FACTORY.createPolygon(p.getInteriorRingN(i).getCoordinates())));
			}
		}
		return holes;
	}

	@Override
	public GamaPoint getCentroid() {
		if (geometry == null) { return null; }
		if (isPoint()) { return getLocation(); }
		final Coordinate c = geometry.getCentroid().getCoordinate();
		c.z = computeAverageZOrdinate();
		return new GamaPoint(c);
	}

	@Override
	public GamaShape getExteriorRing(final IScope scope) {

		// WARNING Only in 2D
		Geometry result = getInnerGeometry();
		if (result instanceof Polygon) {
			result = ((Polygon) result).getExteriorRing();
		} else

		if (result instanceof MultiPolygon) {
			final MultiPolygon mp = (MultiPolygon) result;
			final LineString lines[] = new LineString[mp.getNumGeometries()];
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				lines[i] = ((Polygon) mp.getGeometryN(i)).getExteriorRing();
			}
			result = GeometryUtils.GEOMETRY_FACTORY.createMultiLineString(lines);

		}
		return new GamaShape(result);
	}

	@Override
	public Double getWidth() {
		return getEnvelope().getWidth();
	}

	@Override
	public Double getHeight() {
		return getEnvelope().getHeight();
	}

	@Override
	public Double getDepth() {
		return (Double) this.getAttribute(IShape.DEPTH_ATTRIBUTE);
	}

	@Override
	public void setDepth(final double depth) {
		this.setAttribute(IShape.DEPTH_ATTRIBUTE, depth);
		// this.setEnvelope(null);
	}

	@Override
	public GamaShape getGeometricEnvelope() {
		return new GamaShape(getEnvelope());
	}

	@Override
	public IList<? extends ILocation> getPoints() {
		if (getInnerGeometry() == null) { return GamaListFactory.create(Types.POINT); }
		return (IList<? extends ILocation>) GamaListFactory.createWithoutCasting(Types.POINT,
				getInnerGeometry().getCoordinates());
	}

	@Override
	public Envelope3D getEnvelope() {
		if (geometry == null) { return null; }
		// if (envelope == null) {
		return Envelope3D.of(this);
	}

	// return envelope;
	// try {
	// Envelope e = (Envelope) envelopeField.get(geometry);
	// if (e == null || !(e instanceof Envelope3D)) {
	// e = Envelope3D.of(this);
	// envelopeField.set(geometry, e);
	// }
	// return (Envelope3D) e;
	// } catch (IllegalArgumentException | IllegalAccessException e) {}
	// return null;
	// }

	@Override
	public IAgent getAgent() {
		return agent;
	}

	@Override
	public void setAgent(final IAgent a) {
		agent = a;
	}

	@Override
	public void setInnerGeometry(final Geometry geom) {
		if (geom == null) {
			geometry = null;
			return;
		}
		if (geom.isEmpty()) {
			// See Issue 725
			return;
		}
		if (geom instanceof GeometryCollection && geom.getNumGeometries() == 1) {
			geometry = geom.getGeometryN(0);
		} else {
			geometry = geom;
		}
	}

	// private void setEnvelope(final Envelope3D envelope) {
	// // if (geometry == null) { return; }
	// // try {
	// // envelopeField.set(geometry, envelope);
	// // } catch (IllegalArgumentException | IllegalAccessException e) {
	// // e.printStackTrace();
	// // }
	// // this.envelope = envelope;
	// }

	@Override
	public void setGeometry(final IShape geom) {
		if (geom == null || geom == this) { return; }
		setInnerGeometry(geom.getInnerGeometry());
		mixAttributes(geom);
	}

	private double computeAverageZOrdinate() {
		double z = 0d;
		final Coordinate[] coords = geometry.getCoordinates();
		for (final Coordinate c : coords) {
			if (Double.isNaN(c.z)) {
				continue;
			}
			z += c.z;
		}
		return z / coords.length;
	}

	@Override
	public void dispose() {
		agent = null;
		if (attributes != null) {
			attributes.clear();
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof GamaShape) {
			final Geometry shape = ((GamaShape) o).geometry;
			// Fix a possible NPE when calling equalsExact with a null shape
			if (shape == null) { return geometry == null; }
			if (geometry == null) { return false; }
			return geometry.equalsExact(((GamaShape) o).geometry);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return geometry == null ? super.hashCode() : geometry.hashCode();
	}

	@Override
	public Geometry getInnerGeometry() {
		return geometry;
	}

	@Override
	public GamaShape copy(final IScope scope) {
		final GamaShape g = new GamaShape(this, (Geometry) geometry.clone());
		return g;
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#covers(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean covers(final IShape g) {
		// WARNING Only 2D now
		if (g.isPoint()) { return pl.intersects((Coordinate) g.getLocation(), geometry); }
		// if ( !USE_PREPARED_OPERATIONS ) {

		try {
			return geometry.covers(g.getInnerGeometry());
		} catch (final TopologyException e) {
			try {
				return geometry.buffer(0).covers(g.getInnerGeometry().buffer(0));
			} catch (final TopologyException e2) {
				return false;
			}
		} catch (final AssertionFailedException e) {
			try {
				return geometry.buffer(0).covers(g.getInnerGeometry().buffer(0));
			} catch (final AssertionFailedException e2) {
				return false;
			}
		} catch (final Exception e) {
			return false;
		}
		// }
		// return operations().covers(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#euclidianDistanceTo(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public double euclidianDistanceTo(final IShape g) {
		// WARNING Only 2D now
		if (isPoint() && g.isPoint()) { return g.getLocation().euclidianDistanceTo(getLocation()); }
		// if ( g.isPoint() ) { return euclidianDistanceTo(g.getLocation()); }
		// if ( isPoint ) { return g.euclidianDistanceTo(getLocation()); }
		// if ( !USE_PREPARED_OPERATIONS ) {
		// return getInnerGeometry().distance(g.getInnerGeometry());
		return getInnerGeometry().distance(g.getInnerGeometry());
		// }
		// return operations().getDistance(g);
	}

	@Override
	public double euclidianDistanceTo(final ILocation g) {
		// WARNING Only 2D now
		if (isPoint()) { return g.euclidianDistanceTo(getLocation()); }

		return getInnerGeometry().distance(g.getInnerGeometry());

		// ppd.initialize();
		// DistanceToPoint.computeDistance(geometry, (Coordinate) g, ppd);
		// return ppd.getDistance();
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#intersects(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean intersects(final IShape g) {
		// WARNING Only 2D now
		if (g.isPoint()) { return pl.intersects((Coordinate) g.getLocation(), getInnerGeometry()); }
		// if ( !USE_PREPARED_OPERATIONS ) {
		try {
			return getInnerGeometry().intersects(g.getInnerGeometry());
		} catch (final TopologyException e) {
			try {
				return getInnerGeometry().buffer(0).intersects(g.getInnerGeometry().buffer(0));
			} catch (final TopologyException e2) {
				return false;
			}
		} catch (final AssertionFailedException e) {
			try {
				return getInnerGeometry().buffer(0).intersects(g.getInnerGeometry().buffer(0));
			} catch (final AssertionFailedException e2) {
				return false;
			}

		}

		// } {
		// return operations().intersects(g);
	}

	@Override
	public boolean crosses(final IShape g) {
		// WARNING Only 2D now
		if (g.isPoint()) { return pl.intersects((Coordinate) g.getLocation(), getInnerGeometry()); }
		try {
			return geometry.crosses(g.getInnerGeometry());
		} catch (final TopologyException e) {
			try {
				return getInnerGeometry().buffer(0).crosses(g.getInnerGeometry().buffer(0));
			} catch (final TopologyException e2) {
				return false;
			}
		} catch (final AssertionFailedException e) {
			try {
				return getInnerGeometry().buffer(0).crosses(g.getInnerGeometry().buffer(0));
			} catch (final AssertionFailedException e2) {
				return false;
			}
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Used when the geometry is not affected to an agent and directly accessed by 'read' or 'get' operators. Can be
	 * used in Java too, of course, to retrieve any value stored in the shape
	 * 
	 * @param s
	 * @return the corresponding value of the attribute named 's' in the feature, or null if it is not present
	 */
	@Override
	public Object getAttribute(final String s) {
		if (attributes == null) { return null; }
		return attributes.get(s);
	}

	@Override
	public void setAttribute(final String key, final Object value) {
		getOrCreateAttributes().put(key, value);
	}

	@Override
	public GamaMap getOrCreateAttributes() {
		if (attributes == null) {
			attributes = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}
		return attributes;
	}

	@Override
	public GamaMap getAttributes() {
		return attributes;
	}

	@Override
	public boolean hasAttribute(final String key) {
		return attributes != null && attributes.containsKey(key);
	}

	/**
	 * Method getGeometricalType()
	 * 
	 * @see msi.gama.metamodel.shape.IShape#getGeometricalType()
	 */
	@Override
	public Type getGeometricalType() {
		if (hasAttribute(TYPE_ATTRIBUTE)) { return (Type) getAttribute(TYPE_ATTRIBUTE); }
		final String type = getInnerGeometry().getGeometryType();
		if (JTS_TYPES.containsKey(type)) { return JTS_TYPES.get(type); }
		return Type.NULL;
	}

	public static Set<Type> PREDEFINED_PROPERTIES = new HashSet(Arrays.asList(Type.BOX, Type.CUBE, Type.CYLINDER,
			Type.CONE, Type.CIRCLE, Type.PLAN, Type.PYRAMID, Type.SPHERE, Type.TEAPOT, Type.POLYPLAN));

	/**
	 * Invoked when a geometrical primitive undergoes an operation (like minus(), plus()) that makes it change
	 */
	public void losePredefinedProperty() {
		if (PREDEFINED_PROPERTIES.contains(getGeometricalType())) {
			setAttribute(TYPE_ATTRIBUTE, Type.POLYHEDRON);
		}
	}

}
