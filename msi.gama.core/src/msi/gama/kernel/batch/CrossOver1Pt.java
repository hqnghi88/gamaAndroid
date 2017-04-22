/*********************************************************************************************
 *
 * 'CrossOver1Pt.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.kernel.batch;

import gnu.trove.set.hash.THashSet;
import java.util.Set;
import msi.gama.runtime.IScope;

public class CrossOver1Pt implements CrossOver {

	public CrossOver1Pt() {}

	@Override
	public Set<Chromosome> crossOver(final IScope scope, final Chromosome parent1, final Chromosome parent2) {
		final Set<Chromosome> children = new THashSet<Chromosome>();
		final int nbGenes = parent2.getGenes().length;
		if ( nbGenes == 1 ) { return children; }
		int cutPt = 0;
		if ( nbGenes > 2 ) {
			cutPt = scope.getRandom().between(0, nbGenes - 2);
		}
		final Chromosome child1 = new Chromosome(parent1);
		final Chromosome child2 = new Chromosome(parent2);
		for ( int i = 0; i < cutPt; i++ ) {
			final double val1 = child1.getGenes()[i];
			child1.getGenes()[i] = child2.getGenes()[i];
			child2.getGenes()[i] = val1;
		}
		children.add(child1);
		children.add(child2);
		return children;
	}

}
