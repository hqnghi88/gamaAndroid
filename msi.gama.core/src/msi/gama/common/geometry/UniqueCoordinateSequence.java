package msi.gama.common.geometry;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import msi.gama.metamodel.shape.GamaPoint;

/**
 * A 'sequence' of points containing an unique point.
 * 
 * @author drogoul
 *
 */
public class UniqueCoordinateSequence implements ICoordinates {

	final GamaPoint point;

	public UniqueCoordinateSequence(final Coordinate coord) {
		point = new GamaPoint(coord);
	}

	public UniqueCoordinateSequence(final boolean copy, final GamaPoint gamaPoint) {
		point = gamaPoint;
	}

	@Override
	public int getDimension() {
		return 3;
	}

	@Override
	public GamaPoint getCoordinate(final int i) {
		return point;
	}

	@Override
	public Coordinate getCoordinateCopy(final int i) {
		return new GamaPoint((Coordinate) point);
	}

	@Override
	public void getCoordinate(final int index, final Coordinate coord) {
		coord.x = point.x;
		coord.y = point.y;
		coord.z = point.z;

	}

	@Override
	public double getX(final int index) {
		return point.x;
	}

	@Override
	public double getY(final int index) {
		return point.y;
	}

	@Override
	public double getOrdinate(final int index, final int ordinateIndex) {
		return point.getOrdinate(ordinateIndex);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void setOrdinate(final int index, final int ordinateIndex, final double value) {
		point.setOrdinate(ordinateIndex, value);

	}

	@Override
	public GamaPoint[] toCoordinateArray() {
		return new GamaPoint[] { point };
	}

	@Override
	public Envelope expandEnvelope(final Envelope env) {
		env.expandToInclude(point);
		return env;
	}

	@Override
	public Iterator<GamaPoint> iterator() {
		return Iterators.singletonIterator(point);
	}

	@Override
	public ICoordinates yNegated() {
		return new UniqueCoordinateSequence(false, point.yNegated());
	}

	@Override
	public ICoordinates clone() {
		return new UniqueCoordinateSequence(new GamaPoint((Coordinate) point));
	}

	@Override
	public void visit(final IndexedVisitor v, final int max, final boolean reversed) {
		if (max == 0)
			return;
		v.process(0, point.x, point.y, point.z);
	}

	@Override
	public void visit(final PairVisitor v) {
		// Nothing to do here
	}

	@Override
	public void getNormal(final boolean clockwise, final double factor, final GamaPoint normal) {
		normal.setLocation(0, 0, clockwise ? -factor : factor);
	}

	@Override
	public double averageZ() {
		return point.z;
	}

	@Override
	public ICoordinates setTo(final GamaPoint... points) {
		if (points.length == 0)
			return this;
		final GamaPoint p = points[0];
		point.x = p.x;
		point.y = p.y;
		point.z = p.z;
		return this;
	}

	@Override
	public ICoordinates setTo(final double... points) {
		if (points.length < 3)
			return this;
		point.x = points[0];
		point.y = points[1];
		point.z = points[2];
		return this;
	}

	@Override
	public void addCenterTo(final GamaPoint other) {
		other.add(point);
	}

	@Override
	public Envelope3D getEnvelopeInto(final Envelope3D envelope) {
		envelope.setToNull();
		envelope.expandToInclude(point);
		return envelope;
	}

	@Override
	public GamaPoint directionBetweenLastPointAndOrigin() {
		return new GamaPoint();
	}

	@Override
	public void applyRotation(final Rotation3D rotation) {
		rotation.applyTo(point);
	}

	@Override
	public void replaceWith(final int i, final double x, final double y, final double z) {
		if (i != 0)
			return;
		point.setLocation(x, y, z);

	}

	@Override
	public boolean isHorizontal() {
		return true;
	}

	@Override
	public double getLength() {
		return 0;
	}

	@Override
	public void setAllZ(final double elevation) {
		point.z = elevation;

	}

	@Override
	public boolean isCoveredBy(final Envelope3D env) {
		return env.covers(point);
	}

	@Override
	public void visitClockwise(final VertexVisitor v) {
		v.process(point.x, point.y, point.z);

	}

	@Override
	public void visitCounterClockwise(final VertexVisitor v) {
		v.process(point.x, point.y, point.z);

	}

	@Override
	public void visitYNegatedCounterClockwise(final VertexVisitor v) {
		v.process(point.x, -point.y, point.z);

	}

	@Override
	public void visitYNegatedClockwise(final VertexVisitor v) {
		v.process(point.x, -point.y, point.z);
	}

	@Override
	public boolean isClockwise() {
		return true;
	}

	@Override
	public void completeRing() {}

	@Override
	public void translateBy(final double i, final double j, final double k) {
		point.add(i, j, k);
	}

	@Override
	public void ensureClockwiseness() {

	}

}
