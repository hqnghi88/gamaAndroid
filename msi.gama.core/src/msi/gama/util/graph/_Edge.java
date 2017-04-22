/*********************************************************************************************
 *
 * '_Edge.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform. (c)
 * 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.util.graph;

import org.jgrapht.WeightedGraph;

import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

public class _Edge<V, E> extends GraphObject<GamaGraph<V, E>, V, E> {

	/**
	 * 
	 */
	// protected final GamaGraph<V, ?> graph;
	// private double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;
	private Object source, target;

	public _Edge(final GamaGraph<V, E> gamaGraph, final Object edge, final Object source, final Object target)
			throws GamaRuntimeException {
		this(gamaGraph, edge, source, target, WeightedGraph.DEFAULT_EDGE_WEIGHT);
	}

	public _Edge(final GamaGraph<V, E> gamaGraph, final Object edge, final Object source, final Object target,
			final double weight) throws GamaRuntimeException {
		super(gamaGraph, weight);
		init(graph.getScope(), edge, source, target);
	}

	protected void init(final IScope scope, final Object edge, final Object source, final Object target)
			throws GamaRuntimeException {
		buildSource(edge, source);
		buildTarget(edge, target);
	}

	protected void buildSource(final Object edge, final Object source) {
		this.source = source;
		graph.getVertex(source).addOutEdge(edge);
	}

	protected void buildTarget(final Object edge, final Object target) {
		this.target = target;
		graph.getVertex(target).addInEdge(edge);
	}

	public void removeFromVerticesAs(final Object edge) {
		graph.getVertex(source).removeOutEdge(edge);
		graph.getVertex(target).removeInEdge(edge);
	}

	@Override
	public double getWeight() {
		// Syst�matique ??
		// Double na = graph.getVertexWeight(source);
		// Double nb = graph.getVertexWeight(target);
		return weight;// * (na + nb) / 2;
	}

	public Object getSource() {
		return source;
	}

	public Object getOther(final Object extremity) {
		return extremity == source ? target : source;
	}

	public Object getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return new StringBuffer().append(source.toString()).append(" -> ").append(target.toString()).toString();
	}

	@Override
	public boolean isEdge() {
		return true;
	}
}