/*********************************************************************************************
 *
 * 'GridMooreNeighborhood.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.metamodel.topology.grid;

import gnu.trove.set.hash.TIntHashSet;

/**
 * Written by drogoul Modified on 8 mars 2011
 *
 * @todo Description
 *
 */
public class GridMooreNeighborhood extends GridNeighborhood {


	public GridMooreNeighborhood(final GamaSpatialMatrix gamaSpatialMatrix) {
		super(gamaSpatialMatrix);
	}

	@Override
	protected TIntHashSet getNeighborsAtRadius(final int placeIndex, final int radius) {
		final int y = placeIndex / this.matrix.numCols;
		final int x = placeIndex - y * this.matrix.numCols;
		final TIntHashSet v = new TIntHashSet(radius + 1 * radius + 1);
		int p;
		for ( int i = 1 - radius; i < radius; i++ ) {
			p = this.matrix.getPlaceIndexAt(x + i, y - radius);
			if ( p != -1 ) {
				v.add(p);
			}
			p = this.matrix.getPlaceIndexAt(x - i, y + radius);
			if ( p != -1 ) {
				v.add(p);
			}
		}
		for ( int i = -radius; i < radius + 1; i++ ) {
			p = this.matrix.getPlaceIndexAt(x - radius, y - i);
			if ( p != -1 ) {
				v.add(p);
			}
			p = this.matrix.getPlaceIndexAt(x + radius, y + i);
			if ( p != -1 ) {
				v.add(p);
			}
		}
		return v;
	}

	@Override
	public boolean isVN() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.metamodel.topology.grid.INeighborhood#clear()
	 */
	@Override
	public void clear() {}
}