/*********************************************************************************************
 *
 * '_Vertex.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.util.graph;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.WeightedGraph;
import org.jgrapht.util.ArrayUnenforcedSet;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class _Vertex<E, V> extends GraphObject<GamaGraph<E, V>, E, V> {

	Set inEdges = new ArrayUnenforcedSet(1);
	Set outEdges = new ArrayUnenforcedSet(1);
	// Double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;
	int edgesCount = 0;
	int index = -1;

	/**
	 * The graph to which this vertex belongs
	 */
	// protected final GamaGraph<?, V> graph;

	/**
	 * @param gamaGraph
	 */
	protected _Vertex(final GamaGraph<E, V> gamaGraph) {
		super(gamaGraph, WeightedGraph.DEFAULT_EDGE_WEIGHT);
	}

	@Override
	public double getWeight() {
		return weight;
	}

	public void addOutEdge(final Object e) {
		outEdges.add(e);
		edgesCount++;
	}

	public void removeInEdge(final Object e) {
		inEdges.remove(e);
		edgesCount--;
	}

	public void removeOutEdge(final Object e) {
		outEdges.remove(e);
		edgesCount--;
	}

	public void addInEdge(final Object e) {
		inEdges.add(e);
		edgesCount++;
	}

	public Object edgeTo(final Object v2) {
		for (final Object e : outEdges) {
			final _Edge<V, E> edge = (_Edge<V, E>) graph.edgeMap.get(e);
			if (edge.getTarget().equals(v2)) {
				return e;
			}
		}
		return null;
	}

	public Set edgesTo(final Object v2) {
		final Set result = new HashSet();
		for (final Object e : outEdges) {
			final _Edge<V, E> edge = (_Edge<V, E>) graph.edgeMap.get(e);
			if (edge.getTarget().equals(v2)) {
				result.add(e);
			}
		}
		return result;
	}

	public Set getEdges() {
		final Set result = new HashSet(inEdges);
		result.addAll(outEdges);
		return result;
	}

	/**
	 * @return
	 */
	public int getEdgesCount() {
		return edgesCount;
	}

	/**
	 * @param i
	 */
	public void setIndex(final int i) {
		index = i;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public boolean isNode() {
		return true;
	}

	public Set getInEdges() {
		return inEdges;
	}

	public Set getOutEdges() {
		return outEdges;
	}

}