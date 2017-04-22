/*********************************************************************************************
 *
 * 'GeometryUtils.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.common.geometry;

import static msi.gama.metamodel.shape.IShape.Type.LINESTRING;
import static msi.gama.metamodel.shape.IShape.Type.MULTILINESTRING;
import static msi.gama.metamodel.shape.IShape.Type.MULTIPOINT;
import static msi.gama.metamodel.shape.IShape.Type.NULL;
import static msi.gama.metamodel.shape.IShape.Type.POINT;
import static msi.gama.metamodel.shape.IShape.Type.POLYGON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.geotools.geometry.jts.JTS;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.triangulate.ConformingDelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.ConstraintEnforcementException;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import com.vividsolutions.jts.triangulate.quadedge.LocateFailureException;

import msi.gama.common.interfaces.IEnvelopeComputer;
import msi.gama.common.util.RandomUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.shape.IShape.Type;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.IGamaFile;
import msi.gama.util.graph.IGraph;
import msi.gaml.operators.Files;
import msi.gaml.operators.Graphs;
import msi.gaml.operators.Random;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.ThreeD;
import msi.gaml.operators.fastmaths.FastMath;
import msi.gaml.species.ISpecies;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.Types;

/**
 * The class GamaGeometryUtils.
 *
 * @author drogoul
 * @since 14 dec. 2011
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GeometryUtils {

	public static GamaPoint toCoordinate(final ILocation l) {
		return l.toGamaPoint();
	}

	private static List<IEnvelopeComputer> envelopeComputers = new ArrayList<>();

	public static void addEnvelopeComputer(final IEnvelopeComputer ec) {
		envelopeComputers.add(ec);
	}

	public static GamaGeometryFactory GEOMETRY_FACTORY = new GamaGeometryFactory();
	public static PreparedGeometryFactory PREPARED_GEOMETRY_FACTORY = new PreparedGeometryFactory();

	public static GamaPoint pointInGeom(final IShape shape, final RandomUtils rand) {
		final Geometry geom = shape.getInnerGeometry();
		// WARNING Only in 2D for Polygons !
		if (geom == null || geom.getCoordinate() == null) { return null; }

		if (geom instanceof Point || geom.getCoordinates().length < 2) { return new GamaPoint(geom.getCoordinate()); }
		if (geom instanceof LineString) {
			final double perimeter = shape.getPerimeter();
			final double dist = perimeter * rand.between(0.0, 1.0);
			double sumDist = 0;
			Coordinate pS = ((LineString) geom).getCoordinateN(0);
			for (int i = 1; i < geom.getNumPoints(); i++) {
				final Coordinate pT = ((LineString) geom).getCoordinateN(i);
				final double d = pS.distance3D(pT);
				if (d + sumDist >= dist) {
					final double ratio = (dist - sumDist) / d;
					final double newX = pS.x + ratio * (pT.x - pS.x);
					final double newY = pS.y + ratio * (pT.y - pS.y);
					final double newZ = pS.z + ratio * (pT.z - pS.z);
					return new GamaPoint(newX, newY, newZ);
				}
				pS = pT;
				sumDist += d;
			}
		}
		if (geom instanceof Polygon) {
			final Envelope env = geom.getEnvelopeInternal();
			final double xMin = env.getMinX();
			final double xMax = env.getMaxX();
			final double yMin = env.getMinY();
			final double yMax = env.getMaxY();
			final double x = rand.between(xMin, xMax);

			if (geom.getArea() > 0) {
				final double y = rand.between(yMin, yMax);
				GamaPoint pt = new GamaPoint(x, y);
				while (!shape.intersects(pt)) {
					pt = new GamaPoint(rand.between(xMin, xMax), rand.between(yMin, yMax));
				}
				return pt;
			}

			final Coordinate coord1 = new Coordinate(x, yMin);
			final Coordinate coord2 = new Coordinate(x, yMax);
			final Coordinate[] coords = { coord1, coord2 };
			Geometry line = GEOMETRY_FACTORY.createLineString(coords);
			try {
				line = line.intersection(geom);
			} catch (final Exception e) {
				final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				line = GeometryPrecisionReducer.reducePointwise(line, pm)
						.intersection(GeometryPrecisionReducer.reducePointwise(geom, pm));

			}
			return pointInGeom(new GamaShape(line), rand);
		}
		if (geom instanceof GeometryCollection) {
			if (geom instanceof MultiLineString) {
				final IList<Double> distribution = GamaListFactory.create(Types.FLOAT);
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add(new GamaShape(geom.getGeometryN(i)).getPerimeter());
				}
				final int index = Random.opRndChoice(GAMA.getRuntimeScope(), distribution);
				return pointInGeom(new GamaShape(geom.getGeometryN(index)), rand);
			} else if (geom instanceof MultiPolygon) {
				final IList<Double> distribution = GamaListFactory.create(Types.FLOAT);
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add(new GamaShape(geom.getGeometryN(i)).getArea());
				}
				final int index = Random.opRndChoice(GAMA.getRuntimeScope(), distribution);
				return pointInGeom(new GamaShape(geom.getGeometryN(index)), rand);
			}
			return pointInGeom(new GamaShape(geom.getGeometryN(rand.between(0, geom.getNumGeometries() - 1))), rand);
		}

		return null;

	}

	private static Coordinate[] minimiseLength(final Coordinate[] coords) {
		final double dist1 = GEOMETRY_FACTORY.createLineString(coords).getLength();
		final Coordinate[] coordstest1 = new Coordinate[3];
		coordstest1[0] = coords[0];
		coordstest1[1] = coords[2];
		coordstest1[2] = coords[1];
		final double dist2 = GEOMETRY_FACTORY.createLineString(coordstest1).getLength();

		final Coordinate[] coordstest2 = new Coordinate[3];
		coordstest2[0] = coords[1];
		coordstest2[1] = coords[0];
		coordstest2[2] = coords[2];
		final double dist3 = GEOMETRY_FACTORY.createLineString(coordstest2).getLength();

		if (dist1 <= dist2 && dist1 <= dist3) { return coords; }
		if (dist2 <= dist1 && dist2 <= dist3) { return coordstest1; }
		if (dist3 <= dist1 && dist3 <= dist2) { return coordstest2; }
		return coords;
	}

	public static int nbCommonPoints(final Geometry p1, final Geometry p2) {
		final Set<Coordinate> cp = new HashSet<Coordinate>();
		final List<Coordinate> coords = Arrays.asList(p1.getCoordinates());
		for (final Coordinate pt : p2.getCoordinates()) {
			if (coords.contains(pt)) {
				cp.add(pt);
			}
		}
		return cp.size();
	}

	public static Coordinate[] extractPoints(final IShape triangle, final Set<IShape> connectedNodes) {
		final Coordinate[] coords = triangle.getInnerGeometry().getCoordinates();
		final int degree = connectedNodes.size();
		final Coordinate[] c1 = { coords[0], coords[1] };
		final Coordinate[] c2 = { coords[1], coords[2] };
		final Coordinate[] c3 = { coords[2], coords[3] };
		final LineString l1 = GEOMETRY_FACTORY.createLineString(c1);
		final LineString l2 = GEOMETRY_FACTORY.createLineString(c2);
		final LineString l3 = GEOMETRY_FACTORY.createLineString(c3);
		final Coordinate[] pts = new Coordinate[degree];
		if (degree == 3) {
			pts[0] = l1.getCentroid().getCoordinate();
			pts[1] = l2.getCentroid().getCoordinate();
			pts[2] = l3.getCentroid().getCoordinate();
			return minimiseLength(pts);
		} else if (degree == 2) {
			int cpt = 0;
			for (final IShape n : connectedNodes) {
				if (nbCommonPoints(l1, n.getInnerGeometry()) == 2) {
					pts[cpt] = l1.getCentroid().getCoordinate();
					cpt++;
				} else if (nbCommonPoints(l2, n.getInnerGeometry()) == 2) {
					pts[cpt] = l2.getCentroid().getCoordinate();
					cpt++;
				} else if (nbCommonPoints(l3, n.getInnerGeometry()) == 2) {
					pts[cpt] = l3.getCentroid().getCoordinate();
					cpt++;
				}
			}

		} else {
			return null;
		}
		return pts;
	}

	public static IList<IShape> hexagonalGridFromGeom(final IShape geom, final int nbRows, final int nbColumns) {
		final Envelope env = geom.getEnvelope();
		final double widthEnv = env.getWidth();
		final double heightEnv = env.getHeight();
		double xmin = env.getMinX();
		double ymin = env.getMinY();
		final double widthHex = widthEnv / (nbColumns * 0.75 + 0.25);
		final double heightHex = heightEnv / nbRows;
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		xmin += widthHex / 2.0;
		ymin += heightHex / 2.0;
		for (int l = 0; l < nbRows; l++) {
			for (int c = 0; c < nbColumns; c = c + 2) {
				final GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex,
						new GamaPoint(xmin + c * widthHex * 0.75, ymin + l * heightHex, 0));
				if (geom.covers(poly)) {
					geoms.add(poly);
				}
			}
		}
		for (int l = 0; l < nbRows; l++) {
			for (int c = 1; c < nbColumns; c = c + 2) {
				final GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex,
						new GamaPoint(xmin + c * widthHex * 0.75, ymin + (l + 0.5) * heightHex, 0));
				if (geom.covers(poly)) {
					geoms.add(poly);
				}
			}
		}
		return geoms;
	}

	public static IList<IShape> squareDiscretization(final Geometry geom, final int nb_squares, final boolean overlaps,
			final double coeff_precision) {
		double size = FastMath.sqrt(geom.getArea() / nb_squares);
		List<IShape> rectToRemove = new ArrayList<IShape>();
		IList<IShape> squares = discretization(geom, size, size, overlaps, rectToRemove);
		if (squares.size() < nb_squares) {
			while (squares.size() < nb_squares) {
				size *= coeff_precision;
				rectToRemove = new ArrayList<IShape>();
				squares = discretization(geom, size, size, overlaps, rectToRemove);
			}
		} else if (squares.size() > nb_squares) {
			while (squares.size() > nb_squares) {
				size /= coeff_precision;
				final List<IShape> rectToRemove2 = new ArrayList<IShape>();
				final IList<IShape> squares2 = discretization(geom, size, size, overlaps, rectToRemove2);
				if (squares2.size() < nb_squares) {
					break;
				}
				squares = squares2;
				rectToRemove = rectToRemove2;
			}
		}
		final int nb = squares.size();
		if (nb > nb_squares) {

			if (nb - nb_squares > rectToRemove.size()) {
				squares.removeAll(rectToRemove);
			} else {
				for (int i = 0; i < nb - nb_squares; i++) {
					squares.remove(rectToRemove.get(i));
				}
			}
		}
		return squares;
	}

	public static IList<IShape> discretization(final Geometry geom, final double size_x, final double size_y,
			final boolean overlaps) {
		return discretization(geom, size_x, size_y, overlaps, null);
	}

	public static IList<IShape> discretization(final Geometry geom, final double size_x, final double size_y,
			final boolean overlaps, final List<IShape> borders) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		if (geom instanceof GeometryCollection) {
			final GeometryCollection gc = (GeometryCollection) geom;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				geoms.addAll(discretization(gc.getGeometryN(i), size_x, size_y, overlaps, borders));
			}
		} else {
			final Envelope env = geom.getEnvelopeInternal();
			final double xMax = env.getMaxX();
			final double yMax = env.getMaxY();
			double x = env.getMinX();
			double y = env.getMinY();
			boolean firstX = true;
			while (x < xMax) {
				y = env.getMinY();
				firstX = true;
				while (y < yMax) {
					final Coordinate c1 = new Coordinate(x, y);
					final Coordinate c2 = new Coordinate(x + size_x, y);
					final Coordinate c3 = new Coordinate(x + size_x, y + size_y);
					final Coordinate c4 = new Coordinate(x, y + size_y);
					final Coordinate[] cc = { c1, c2, c3, c4, c1 };
					final Geometry square = GEOMETRY_FACTORY.createPolygon(GEOMETRY_FACTORY.createLinearRing(cc), null);
					y += size_y;
					if (!overlaps) {
						if (square.coveredBy(geom)) {
							final IShape sq = new GamaShape(square);
							geoms.add(sq);
							if (firstX && borders != null) {
								borders.add(sq);
							}
							firstX = false;

						}
					} else {
						if (square.intersects(geom)) {
							final IShape sq = new GamaShape(square);
							geoms.add(sq);
							if (firstX && borders != null) {
								borders.add(sq);
							}
							firstX = false;
						}
					}
				}
				x += size_x;
			}
		}
		return geoms;
	}

	public static IList<IShape> geometryDecomposition(final IShape geom, final double x_size, final double y_size) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final double zVal = geom.getLocation().getZ();
		final IList<IShape> rects = discretization(geom.getInnerGeometry(), x_size, y_size, true);
		for (final IShape shape : rects) {
			final IShape gg = Operators.inter(null, shape, geom);
			if (gg != null && !gg.getInnerGeometry().isEmpty()) {
				final GamaShape sp = new GamaShape(gg);
				final IList<ILocation> pts = (IList<ILocation>) sp.getPoints();
				for (int i = 0; i < pts.size(); i++) {
					final ILocation gp = pts.get(i);
					if (zVal != gp.getZ()) {
						ThreeD.set_z(null, sp, i, zVal);
					}
				}
				geoms.add(sp);
			}
		}
		return geoms;
	}

	public static IList<IShape> geometryDecomposition(final IShape geom, final int nbCols, final int nbRows) {
		final Envelope env = geom.getEnvelope();
		final double x_size = env.getWidth() / nbCols;
		final double y_size = env.getHeight() / nbRows;
		return geometryDecomposition(geom, x_size, y_size);
	}

	public static IList<IShape> triangulation(final IScope scope, final IList<IShape> lines) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();

		final Geometry points = GamaGeometryType.geometriesToGeometry(scope, lines).getInnerGeometry();
		final double sizeTol = FastMath.sqrt(points.getEnvelope().getArea()) / 100.0;

		dtb.setSites(points);
		dtb.setConstraints(points);
		dtb.setTolerance(sizeTol);
		final GeometryCollection tri = (GeometryCollection) dtb.getTriangles(GEOMETRY_FACTORY);
		final int nb = tri.getNumGeometries();
		for (int i = 0; i < nb; i++) {
			final Geometry gg = tri.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static IList<IShape> voronoi(final IScope scope, final IList<GamaPoint> points) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final VoronoiDiagramBuilder dtb = new VoronoiDiagramBuilder();
		dtb.setClipEnvelope(scope.getSimulation().getEnvelope());
		dtb.setSites(points);
		final GeometryCollection g = (GeometryCollection) dtb.getDiagram(GEOMETRY_FACTORY);
		final int nb = g.getNumGeometries();
		for (int i = 0; i < nb; i++) {
			final Geometry gg = g.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static IList<IShape> voronoi(final IScope scope, final IList<GamaPoint> points, final IShape clip) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final VoronoiDiagramBuilder dtb = new VoronoiDiagramBuilder();
		dtb.setClipEnvelope(clip.getEnvelope());
		dtb.setSites(points);
		final GeometryCollection g = (GeometryCollection) dtb.getDiagram(GEOMETRY_FACTORY);
		final int nb = g.getNumGeometries();
		for (int i = 0; i < nb; i++) {
			final Geometry gg = g.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static void simplifiedTriangulation(final Polygon polygon, final Collection<Polygon> geoms) {
		final double elevation = getContourCoordinates(polygon).averageZ();
		final double sizeTol = FastMath.sqrt(polygon.getArea()) / 100.0;
		final DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
		GeometryCollection tri = null;
		try {
			dtb.setSites(polygon);
			dtb.setTolerance(sizeTol);
			tri = (GeometryCollection) dtb.getTriangles(GEOMETRY_FACTORY);
		} catch (final LocateFailureException | ConstraintEnforcementException e) {
			final IScope scope = GAMA.getRuntimeScope();
			GamaRuntimeException.warning("Impossible to triangulate: " + new WKTWriter().write(polygon), scope);
			geoms.clear();
			simplifiedTriangulation((Polygon) DouglasPeuckerSimplifier.simplify(polygon, 0.1), geoms);
			return;
		}
		final PreparedGeometry buffered = PREPARED_GEOMETRY_FACTORY.create(polygon.buffer(sizeTol, 5, 0));
		final Envelope3D env = Envelope3D.of(buffered.getGeometry());
		applyToInnerGeometries(tri, (gg) -> {
			final ICoordinates cc = getContourCoordinates(gg);
			if (cc.isCoveredBy(env) && buffered.covers(gg)) {
				cc.setAllZ(elevation);
				gg.geometryChanged();
				geoms.add((Polygon) gg);
			}
		});

	}

	public static void iterateOverTriangles(final Polygon polygon, final Consumer<Geometry> action) {
		final double elevation = getContourCoordinates(polygon).averageZ();
		final double sizeTol = FastMath.sqrt(polygon.getArea()) / 100.0;
		final DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
		final PreparedGeometry buffered = PREPARED_GEOMETRY_FACTORY.create(polygon.buffer(sizeTol, 5, 0));
		final Envelope3D env = Envelope3D.of(buffered.getGeometry());
		try {
			dtb.setSites(polygon);
			dtb.setTolerance(sizeTol);
			applyToInnerGeometries(dtb.getTriangles(GEOMETRY_FACTORY), (gg) -> {
				final ICoordinates cc = getContourCoordinates(gg);
				if (cc.isCoveredBy(env) && buffered.covers(gg)) {
					cc.setAllZ(elevation);
					gg.geometryChanged();
					action.accept(gg);
				}
			});
		} catch (final LocateFailureException | ConstraintEnforcementException e) {
			final IScope scope = GAMA.getRuntimeScope();
			GamaRuntimeException.warning("Impossible to triangulate: " + new WKTWriter().write(polygon), scope);
			iterateOverTriangles((Polygon) DouglasPeuckerSimplifier.simplify(polygon, 0.1), action);
			return;
		}
	}

	public static IList<IShape> triangulation(final IScope scope, final Geometry geom) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		if (geom instanceof GeometryCollection) {
			final GeometryCollection gc = (GeometryCollection) geom;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				geoms.addAll(triangulation(scope, gc.getGeometryN(i)));
			}
		} else if (geom instanceof Polygon) {
			final Polygon polygon = (Polygon) geom;
			final double sizeTol = FastMath.sqrt(polygon.getArea()) / 100.0;
			final ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();
			GeometryCollection tri = null;
			try {
				dtb.setSites(polygon);
				dtb.setConstraints(polygon);
				dtb.setTolerance(sizeTol);
				tri = (GeometryCollection) dtb.getTriangles(GEOMETRY_FACTORY);
			} catch (final LocateFailureException e) {
				GamaRuntimeException.warning("Impossible to triangulate Geometry: " + new WKTWriter().write(geom),
						scope);
				return triangulation(scope, DouglasPeuckerSimplifier.simplify(geom, 0.1));
			} catch (final ConstraintEnforcementException e) {
				/* GAMA.reportError(scope, */GamaRuntimeException.warning(
						"Impossible to triangulate Geometry: " + new WKTWriter().write(geom), scope)/* , false) */;
				return triangulation(scope, DouglasPeuckerSimplifier.simplify(geom, 0.1));
			}
			final PreparedGeometry pg = PREPARED_GEOMETRY_FACTORY.create(polygon.buffer(sizeTol, 5, 0));
			final PreparedGeometry env = PREPARED_GEOMETRY_FACTORY.create(pg.getGeometry().getEnvelope());
			final int nb = tri.getNumGeometries();
			for (int i = 0; i < nb; i++) {

				final Geometry gg = tri.getGeometryN(i);

				if (env.covers(gg) && pg.covers(gg) && gg.intersects(polygon)
						&& gg.intersection(polygon).getArea() > 0.2 * gg.getArea()) {
					geoms.add(new GamaShape(gg));
				}
			}
		}
		return geoms;
	}

	public static List<LineString> squeletisation(final IScope scope, final Geometry geom) {
		final List<LineString> network = new ArrayList<LineString>();
		final IList polys = GeometryUtils.triangulation(scope, geom);
		final IGraph graph = Graphs.spatialLineIntersection(scope, polys);
		final Collection<GamaShape> nodes = graph.vertexSet();
		for (final GamaShape node : nodes) {
			final Coordinate[] coordsArr =
					GeometryUtils.extractPoints(node, new HashSet(Graphs.neighborsOf(scope, graph, node)));
			if (coordsArr != null) {
				network.add(GEOMETRY_FACTORY.createLineString(coordsArr));
			}
		}
		return network;
	}

	public static Geometry buildGeometryJTS(final List<List<List<ILocation>>> listPoints) {
		final IShape.Type geometryType = geometryType(listPoints);
		switch (geometryType) {
			case NULL:
				return null;
			case POINT:
				return buildPoint(listPoints.get(0));
			case LINESTRING:
				return buildLine(listPoints.get(0));
			case POLYGON:
				return buildPolygon(listPoints.get(0));
			case MULTIPOINT:
				final int nb = listPoints.size();
				final Point[] geoms = new Point[nb];
				for (int i = 0; i < nb; i++) {
					geoms[i] = (Point) buildPoint(listPoints.get(i));
				}
				return GEOMETRY_FACTORY.createMultiPoint(geoms);
			case MULTILINESTRING:
				final int n = listPoints.size();
				final LineString[] lines = new LineString[n];
				for (int i = 0; i < n; i++) {
					lines[i] = (LineString) buildLine(listPoints.get(i));
				}
				return GEOMETRY_FACTORY.createMultiLineString(lines);
			case MULTIPOLYGON:
				final int n3 = listPoints.size();
				final Polygon[] polys = new Polygon[n3];
				for (int i = 0; i < n3; i++) {
					polys[i] = (Polygon) buildPolygon(listPoints.get(i));
				}
				return GEOMETRY_FACTORY.createMultiPolygon(polys);
			default:
				return null;
		}
	}

	private static Geometry buildPoint(final List<List<ILocation>> listPoints) {
		return GEOMETRY_FACTORY.createPoint((Coordinate) listPoints.get(0).get(0));
	}

	public static Geometry buildGeometryCollection(final List<IShape> geoms) {
		final int nb = geoms.size();
		final Geometry[] geometries = new Geometry[nb];
		for (int i = 0; i < nb; i++) {
			geometries[i] = geoms.get(i).getInnerGeometry();
		}
		final Geometry geom = GEOMETRY_FACTORY.createGeometryCollection(geometries);

		return geom;
	}

	private static Geometry buildLine(final List<List<ILocation>> listPoints) {
		final List<ILocation> coords = listPoints.get(0);
		final int nb = coords.size();
		final Coordinate[] coordinates = new Coordinate[nb];
		for (int i = 0; i < nb; i++) {
			coordinates[i] = (Coordinate) coords.get(i);
		}
		return GEOMETRY_FACTORY.createLineString(coordinates);
	}

	private static Geometry buildPolygon(final List<List<ILocation>> listPoints) {
		final List<ILocation> coords = listPoints.get(0);
		final int nb = coords.size();
		final Coordinate[] coordinates = new Coordinate[nb];
		for (int i = 0; i < nb; i++) {
			coordinates[i] = (Coordinate) coords.get(i);
		}
		final int nbHoles = listPoints.size() - 1;
		LinearRing[] holes = null;
		if (nbHoles > 0) {
			holes = new LinearRing[nbHoles];
			for (int i = 0; i < nbHoles; i++) {
				final List<ILocation> coordsH = listPoints.get(i + 1);
				final int nbp = coordsH.size();
				final Coordinate[] coordinatesH = new Coordinate[nbp];
				for (int j = 0; j < nbp; j++) {
					coordinatesH[j] = (Coordinate) coordsH.get(j);
				}
				holes[i] = GEOMETRY_FACTORY.createLinearRing(coordinatesH);
			}
		}
		final Polygon poly = GEOMETRY_FACTORY.createPolygon(GEOMETRY_FACTORY.createLinearRing(coordinates), holes);
		return poly;
	}

	private static IShape.Type geometryType(final List<List<List<ILocation>>> listPoints) {
		final int size = listPoints.size();
		if (size == 0) { return NULL; }
		if (size == 1) { return geometryTypeSimp(listPoints.get(0)); }
		final IShape.Type type = geometryTypeSimp(listPoints.get(0));
		switch (type) {
			case POINT:
				return MULTIPOINT;
			case LINESTRING:
				return MULTILINESTRING;
			case POLYGON:
				return POLYGON;
			default:
				return NULL;
		}
	}

	private static IShape.Type geometryTypeSimp(final List<List<ILocation>> listPoints) {
		if (listPoints.isEmpty() || listPoints.get(0).isEmpty()) { return NULL; }
		final List<ILocation> list0 = listPoints.get(0);
		final int size0 = list0.size();
		if (size0 == 1 || size0 == 2 && list0.get(0).equals(list0.get(listPoints.size() - 1))) { return POINT; }
		if (!list0.get(0).equals(list0.get(listPoints.size() - 1)) || size0 < 3) { return LINESTRING; }
		return POLYGON;
	}

	public static IList<GamaPoint> locsOnGeometry(final Geometry geom, final Double distance) {
		final IList<GamaPoint> locs = GamaListFactory.create(Types.POINT);
		if (geom instanceof Point) {
			locs.add(new GamaPoint(geom.getCoordinate()));
		} else if (geom instanceof LineString) {
			double distCur = 0;
			final Coordinate[] coordsSimp = geom.getCoordinates();
			if (coordsSimp.length > 0) {
				locs.add(new GamaPoint(coordsSimp[0]));
			}
			final int nbSp = coordsSimp.length;
			for (int i = 0; i < nbSp - 1; i++) {

				Coordinate s = coordsSimp[i];
				final Coordinate t = coordsSimp[i + 1];
				while (true) {
					final double dist = s.distance3D(t);
					if (distance - distCur < dist) {
						final double distTravel = distance - distCur;
						final double ratio = distTravel / dist;
						final double x_s = s.x + ratio * (t.x - s.x);
						final double y_s = s.y + ratio * (t.y - s.y);
						final double z_s = s.z + ratio * (t.z - s.z);
						s = new Coordinate(x_s, y_s, z_s);
						locs.add(new GamaPoint(s));
						distCur = 0;

					} else if (distance - distCur > dist) {
						distCur += dist;
						break;
					} else {
						distCur = 0;
						locs.add(new GamaPoint(t));
						break;
					}
				}

			}
			if (locs.size() > 1) {
				if (locs.get(0).distance3D(locs.get(locs.size() - 1)) < 0.1 * distance) {
					locs.remove(locs.size() - 1);
				}
			}
		} else if (geom instanceof Polygon) {
			final Polygon poly = (Polygon) geom;
			locs.addAll(locsOnGeometry(poly.getExteriorRing(), distance));
			for (int i = 0; i < poly.getNumInteriorRing(); i++) {
				locs.addAll(locsOnGeometry(poly.getInteriorRingN(i), distance));
			}
		}
		return locs;
	}

	public static IList<GamaPoint> locsAlongGeometry(final Geometry geom, final List<Double> rates) {
		final IList<GamaPoint> locs = GamaListFactory.create(Types.POINT);
		if (rates == null || rates.isEmpty())
			return locs;
		if (geom instanceof Point) {
			for (int i = 0; i < rates.size(); i++)
				locs.add(new GamaPoint(geom.getCoordinate()));
		} else if (geom instanceof LineString) {
			for (Double rate : rates) {
				final Coordinate[] coordsSimp = geom.getCoordinates();
				final int nbSp = coordsSimp.length;
				if (nbSp <= 0)
					return locs;
				if (rate > 1.0)
					rate = 1.0;
				if (rate < 0.0)
					rate = 0.0;
				if (rate == 0) {
					locs.add(new GamaPoint(coordsSimp[0]));
					continue;
				}
				if (rate == 1) {
					locs.add(new GamaPoint(coordsSimp[nbSp - 1]));
					continue;
				}
				double distCur = 0;
				final double distance = rate * geom.getLength();
				for (int i = 0; i < nbSp - 1; i++) {
					Coordinate s = coordsSimp[i];
					final Coordinate t = coordsSimp[i + 1];
					final double dist = s.distance3D(t);
					if (distance - distCur < dist) {
						final double distTravel = distance - distCur;
						final double ratio = distTravel / dist;
						final double x_s = s.x + ratio * (t.x - s.x);
						final double y_s = s.y + ratio * (t.y - s.y);
						final double z_s = s.z + ratio * (t.z - s.z);
						s = new Coordinate(x_s, y_s, z_s);
						locs.add(new GamaPoint(s));
						break;
					} else if (distance - distCur > dist) {
						distCur += dist;
					} else {
						locs.add(new GamaPoint(t));
						break;
					}
				}
			}
		} else if (geom instanceof Polygon) {
			final Polygon poly = (Polygon) geom;
			locs.addAll(locsAlongGeometry(poly.getExteriorRing(), rates));
		}
		return locs;
	}

	// ---------------------------------------------------------------------------------------------
	// Thai.truongminh@gmail.com
	// Created date:24-Feb-2013: Process for SQL - MAP type
	// Modified: 03-Jan-2014

	public static Envelope3D computeEnvelopeFrom(final IScope scope, final Object obj) {
		Envelope3D result = new Envelope3D();
		if (obj instanceof ISpecies) {
			return computeEnvelopeFrom(scope, ((ISpecies) obj).getPopulation(scope));
		} else if (obj instanceof Number) {
			final double size = ((Number) obj).doubleValue();
			result = new Envelope3D(0, size, 0, size, 0, size);
		} else if (obj instanceof ILocation) {
			final ILocation size = (ILocation) obj;
			result = new Envelope3D(0, size.getX(), 0, size.getY(), 0, size.getZ());
		} else if (obj instanceof IShape) {
			result = ((IShape) obj).getEnvelope();
		} else if (obj instanceof Envelope) {
			result = new Envelope3D((Envelope) obj);
		} else if (obj instanceof String) {
			result = computeEnvelopeFrom(scope, Files.from(scope, (String) obj));
		} else if (obj instanceof IGamaFile) {
			result = ((IGamaFile) obj).computeEnvelope(scope);
		} else if (obj instanceof IList) {
			Envelope3D boundsEnv = null;
			for (final Object bounds : (IList) obj) {
				final Envelope3D env = computeEnvelopeFrom(scope, bounds);
				if (boundsEnv == null) {
					boundsEnv = env;
				} else {
					boundsEnv.expandToInclude(env);
				}
			}
			result = boundsEnv;
		} else {
			for (final IEnvelopeComputer ec : envelopeComputers) {
				result = ec.computeEnvelopeFrom(scope, obj);
				if (result != null)
					return result;
			}
		}
		return result;
	}

	public static IList<IShape> split_at(final IShape geom, final ILocation pt) {
		final IList<IShape> lines = GamaListFactory.create(Types.GEOMETRY);
		List<Geometry> geoms = null;
		if (geom.getInnerGeometry() instanceof LineString) {
			final Coordinate[] coords = ((LineString) geom.getInnerGeometry()).getCoordinates();
			final Point pt1 = GEOMETRY_FACTORY.createPoint(new GamaPoint(pt.getLocation()));
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for (int i = 0; i < nb - 1; i++) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = GEOMETRY_FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if (distT < distanceT) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for (int i = 0; i <= indexTarget; i++) {
				coords1[i] = coords[i];
			}
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for (int i = indexTarget + 1; i < coords.length; i++) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<Geometry>();
			geoms1.add(GEOMETRY_FACTORY.createLineString(coords1));
			geoms1.add(GEOMETRY_FACTORY.createLineString(coords2));
			geoms = geoms1;
		} else if (geom.getInnerGeometry() instanceof MultiLineString) {
			final Point point = GEOMETRY_FACTORY.createPoint((Coordinate) pt);
			Geometry geom2 = null;
			double distMin = Double.MAX_VALUE;
			final MultiLineString ml = (MultiLineString) geom.getInnerGeometry();
			for (int i = 0; i < ml.getNumGeometries(); i++) {
				final double dist = ml.getGeometryN(i).distance(point);
				if (dist <= distMin) {
					geom2 = ml.getGeometryN(i);
					distMin = dist;
				}
			}
			final Coordinate[] coords = ((LineString) geom2).getCoordinates();
			final Point pt1 = GEOMETRY_FACTORY.createPoint(new GamaPoint(pt.getLocation()));
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for (int i = 0; i < nb - 1; i++) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = GEOMETRY_FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if (distT < distanceT) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for (int i = 0; i <= indexTarget; i++) {
				coords1[i] = coords[i];
			}
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for (int i = indexTarget + 1; i < coords.length; i++) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<Geometry>();
			geoms1.add(GEOMETRY_FACTORY.createLineString(coords1));
			geoms1.add(GEOMETRY_FACTORY.createLineString(coords2));
			geoms = geoms1;
		}
		if (geoms != null) {
			for (final Geometry g : geoms) {
				lines.add(new GamaShape(g));
			}
		}
		return lines;
	}

	/**
	 * @param intersect
	 * @return
	 */
	public static Type getTypeOf(final Geometry g) {
		if (g == null) { return Type.NULL; }
		return IShape.JTS_TYPES.get(g.getGeometryType());
	}

	/**
	 * @param scope
	 * @param innerGeometry
	 * @param param
	 * @return
	 */
	public static IShape smooth(final Geometry geom, final double fit) {
		return new GamaShape(JTS.smooth(geom, fit, GEOMETRY_FACTORY));
	}

	public static ICoordinates getContourCoordinates(final Polygon g) {
		if (g.isEmpty())
			return ICoordinates.EMPTY;
		return (ICoordinates) g.getExteriorRing().getCoordinateSequence();
	}

	public static ICoordinates getContourCoordinates(final LineString g) {
		if (g.isEmpty())
			return ICoordinates.EMPTY;
		return (ICoordinates) g.getCoordinateSequence();
	}

	public static ICoordinates getContourCoordinates(final Point g) {
		if (g.isEmpty())
			return ICoordinates.EMPTY;
		return (ICoordinates) g.getCoordinateSequence();
	}

	public static ICoordinates getContourCoordinates(final Geometry g) {
		if (g instanceof Polygon) { return getContourCoordinates((Polygon) g); }
		if (g instanceof LineString) { return getContourCoordinates((LineString) g); }
		if (g instanceof Point) { return getContourCoordinates((Point) g); }
		if (g instanceof GeometryCollection) { return getContourCoordinates(g.convexHull()); }
		return ICoordinates.EMPTY;
	}

	/**
	 * Applies a GeometryComponentFilter to internal geometries. Concerns the geometries contained in multi-geometries,
	 * and the holes in polygons. Limited to one level (i.e. holes in polygons in a MultiPolygon will not be visited)
	 * 
	 * @param g
	 *            the geometry to visit
	 * @param f
	 *            the filter to apply
	 */
	public static void applyToInnerGeometries(final Geometry g, final GeometryFilter f) {
		if (g instanceof Polygon)
			applyToInnerGeometries((Polygon) g, f);
		else if (g instanceof GeometryCollection) {
			applyToInnerGeometries((GeometryCollection) g, f);
		}
	}

	public static void applyToInnerGeometries(final Polygon g, final GeometryFilter f) {
		final int holes = g.getNumInteriorRing();
		if (holes == 0)
			return;
		for (int i = 0; i < holes; i++) {
			g.getInteriorRingN(i).apply(f);
		}
	}

	public static void applyToInnerGeometries(final GeometryCollection g, final GeometryFilter f) {
		final int geoms = g.getNumGeometries();
		if (geoms == 0)
			return;
		for (int i = 0; i < geoms; i++) {
			final Geometry sub = g.getGeometryN(i);
			sub.apply(f);
		}
	}

	public static void translate(final Geometry geometry, final double dx, final double dy, final double dz) {
		geometry.apply((final Coordinate p) -> {
			p.x += dx;
			p.y += dy;
			p.z += dz;
		});
		geometry.geometryChanged();
	}

	public static ICoordinates getYNegatedCoordinates(final Geometry geom) {
		return getContourCoordinates(geom).yNegated();
	}

	public static int getHolesNumber(final Geometry p) {
		return p instanceof Polygon ? ((Polygon) p).getNumInteriorRing() : 0;
	}
}
