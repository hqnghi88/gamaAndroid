/*********************************************************************************************
 *
 * 'AStar.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.metamodel.topology.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.TOrderedHashMap;
import msi.gama.util.graph.GamaGraph;
import msi.gama.util.graph._Edge;
import msi.gama.util.graph._Vertex;

public class AStar<V, E> {

	protected GamaGraph<V, E> graph;
	protected V source;
	protected V target;
	protected Map<V, ASNode> openMap = new TOrderedHashMap<V, ASNode>();
	protected Map<V, ASNode> closedMap = new TOrderedHashMap<V, ASNode>();
	protected List<E> result;
	protected boolean isSpatialGraph;
	protected boolean isPathFound = false;

	public AStar() {
	}

	public AStar(final GamaGraph<V, E> graph) {
		init(graph);
	}

	public AStar(final GamaGraph<V, E> graph, final V src, final V trg) {
		this(graph);
		setSource(src);
		setTarget(trg);
	}

	public void setSource(final V node) {
		cleanAll();
		source = node;
	}

	public void setTarget(final V node) {
		cleanAll();
		target = node;
	}

	public void init(final GamaGraph<V, E> graph) {
		cleanAll();
		this.graph = graph;
		isSpatialGraph = graph instanceof GamaSpatialGraph;
	}

	public void compute() {
		if (source != null && target != null) {
			aStar(source, target);
		}
	}

	public List<E> getShortestPath() {
		return result;
	}

	public boolean noPathFound() {
		return !isPathFound;
	}

	public IList<E> buildPath(final ASNode target) {
		final IList<E> path = GamaListFactory.create();

		final IList<ASNode> thePath = GamaListFactory.create();
		ASNode node = target;

		while (node != null) {
			thePath.add(node);
			node = node.parent;
		}

		final int n = thePath.size();

		if (n > 1) {
			ASNode follow = thePath.get(n - 2);
			path.add(follow.edge);
			for (int i = n - 3; i >= 0; i--) {
				follow = thePath.get(i);
				path.add(follow.edge);
			}
		}

		return path;
	}

	protected void cleanAll() {
		openMap.clear();
		closedMap.clear();

		result = null;
		isPathFound = false;
	}

	@SuppressWarnings("unchecked")
	protected void aStar(final V sourceNode, final V targetNode) {
		cleanAll();
		openMap.put(sourceNode, new ASNode(sourceNode, null, null, 0, heuristic(sourceNode, targetNode)));

		isPathFound = false;

		while (!openMap.isEmpty()) {
			final ASNode current = getNextBetterNode();
			assert current != null;
			if (current.node.equals(targetNode)) {
				assert current.edge != null;
				isPathFound = true;
				result = buildPath(current);
				return;
			}
			openMap.remove(current.node);
			closedMap.put(current.node, current);
			final _Vertex<V, E> node = graph.getVertex(current.node);
			final Set<E> edges = new HashSet<E>(node.getOutEdges());
			if (!graph.isDirected()) {
				edges.addAll(node.getInEdges());

			}
			for (final E edge : edges) {
				final _Edge<V, E> eg = graph.getEdge(edge);
				final V next = (V) (eg.getTarget().equals(current.node) ? eg.getSource() : eg.getTarget());
				if (closedMap.containsKey(next))
					continue;

				final double h = heuristic(next, targetNode);
				final double g = current.g + eg.getWeight();
				final ASNode openNode = openMap.get(next);
				if (openNode == null)
					openMap.put(next, new ASNode(next, edge, current, g, h));
				else if (g >= openNode.rank) {
					continue;
				}
			}
		}
	}

	protected double heuristic(final Object node1, final Object node2) {
		if (isSpatialGraph) {
			final GamaPoint pt1 = (GamaPoint) ((IShape) node1).getLocation();
			final GamaPoint pt2 = (GamaPoint) ((IShape) node2).getLocation();
			return pt1.distance(pt2);

		}
		return 0;
	}

	protected ASNode getNextBetterNode() {
		double min = Float.MAX_VALUE;
		ASNode theChosenOne = null;

		for (final ASNode node : openMap.values()) {
			if (node.rank < min) {
				theChosenOne = node;
				min = node.rank;
			}
		}

		return theChosenOne;
	}

	public void compute(final V source, final V target) {
		setSource(source);
		setTarget(target);
		compute();
	}

	protected class ASNode {

		public V node;
		public ASNode parent;
		public E edge;
		public double g;
		public double h;

		public double rank;

		public ASNode(final V node, final E edge, final ASNode parent, final double g, final double h) {
			this.node = node;
			this.edge = edge;
			this.parent = parent;
			this.g = g;
			this.h = h;
			this.rank = g + h;
		}
	}
}