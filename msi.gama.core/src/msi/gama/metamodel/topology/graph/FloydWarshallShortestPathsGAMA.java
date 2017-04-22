/*********************************************************************************************
 *
 * 'FloydWarshallShortestPathsGAMA.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.metamodel.topology.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.util.VertexPair;

import gnu.trove.map.hash.THashMap;
import msi.gama.runtime.GAMA;
import msi.gama.util.graph.GamaGraph;
import msi.gama.util.matrix.GamaIntMatrix;

// Copy of the jgrapht algorithm: just make it usable with GAMA undirected graph
public class FloydWarshallShortestPathsGAMA<V, E> {

	// ~ Instance fields --------------------------------------------------------

	private final GamaGraph<V, E> graph;
	private final List<V> vertices;
	private int nShortestPaths = 0;
	private double diameter = 0.0;
	private double[][] d = null;
	private int[][] backtrace = null;
	private GamaIntMatrix matrix = null;
	private THashMap<VertexPair<V>, GraphPath<V, E>> paths = null;

	// ~ Constructors -----------------------------------------------------------

	public FloydWarshallShortestPathsGAMA(final GamaGraph<V, E> graph) {
		this.graph = graph;
		this.vertices = new ArrayList<V>(graph.getVertexMap().keySet());

	}

	// ~ Methods ----------------------------------------------------------------

	public FloydWarshallShortestPathsGAMA(final GamaGraph<V, E> graph, final GamaIntMatrix matrix) {
		this.graph = graph;
		this.vertices = new ArrayList<V>(graph.getVertexMap().keySet());
		this.paths = new THashMap<VertexPair<V>, GraphPath<V, E>>();
		nShortestPaths = 0;
		this.matrix = matrix;
	}

	/**
	 * @return the graph on which this algorithm operates
	 */
	public Graph<V, E> getGraph() {
		return graph;
	}

	/**
	 * @return total number of shortest paths
	 */
	public int getShortestPathsCount() {
		lazyCalculatePaths();
		return nShortestPaths;
	}

	public double[][] getD() {
		return d;
	}

	public int[][] getBacktrace() {
		return backtrace;
	}

	/**
	 * Calculates the matrix of all shortest paths, along with the diameter, but does not populate the paths map.
	 */
	@SuppressWarnings ("unchecked")
	public void lazyCalculateMatrix() {
		matrix = null;
		if (d != null) {
			// already done
			return;
		}

		final int n = vertices.size();

		// init the backtrace matrix
		backtrace = new int[n][n];
		for (int i = 0; i < n; i++) {
			Arrays.fill(backtrace[i], -1);
		}

		// initialize matrix, 0
		d = new double[n][n];
		for (int i = 0; i < n; i++) {
			Arrays.fill(d[i], Double.POSITIVE_INFINITY);
		}

		// initialize matrix, 1
		for (int i = 0; i < n; i++) {
			d[i][i] = 0.0;
		}

		// initialize matrix, 2
		final Set<E> edges = graph.edgeSet();

		for (final E edge : edges) {
			final V v1 = (V) graph.getEdgeSource(edge);
			final V v2 = (V) graph.getEdgeTarget(edge);

			final int v_1 = vertices.indexOf(v1);
			final int v_2 = vertices.indexOf(v2);

			d[v_1][v_2] = graph.getEdgeWeight(edge);
			if (!graph.isDirected()) {
				d[v_2][v_1] = graph.getEdgeWeight(edge);
			}
		}

		// run fw alg
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					final double ik_kj = d[i][k] + d[k][j];
					if (ik_kj < d[i][j]) {
						d[i][j] = ik_kj;
						backtrace[i][j] = k;
						diameter = diameter > d[i][j] ? diameter : d[i][j];
					}
				}
			}
		}
	}

	/**
	 * @return the diameter (longest of all the shortest paths) computed for the graph
	 */
	public double getDiameter() {
		lazyCalculateMatrix();
		return diameter;
	}

	public void shortestPathRecur(final List<E> edges, final int v_a, final int v_b) {
		final int k = backtrace[v_a][v_b];

		if (k == -1) {
			final Set<E> edgs = graph.getAllEdges(vertices.get(v_a), vertices.get(v_b));
			double minW = Double.MAX_VALUE;
			E edge = null;
			for (final E e : edgs) {
				final double ew = graph.getEdgeWeight(e);
				if (ew < minW) {
					minW = ew;
					edge = e;
				}
			}
			if (edge != null) {
				edges.add(edge);
			}
		} else {
			shortestPathRecur(edges, v_a, k);
			shortestPathRecur(edges, k, v_b);
		}
	}

	public int succRecur(final int v_a, final int v_b) {
		final int k = backtrace[v_a][v_b];
		if (k == -1) {
			if (!graph.containsEdge(vertices.get(v_a), vertices.get(v_b))) { return -1; }
			return v_b;
		} else {
			return succRecur(v_a, k);
		}
	}

	/**
	 * Get the shortest path between two vertices. Note: The paths are calculated using a recursive algorithm. It *will*
	 * give problems on paths longer than the stack allows.
	 * 
	 * @param a
	 *            From vertice
	 * @param b
	 *            To vertice
	 * 
	 * @return the path, or null if none found
	 */
	public GraphPath<V, E> getShortestPath(final V a, final V b) {
		lazyCalculatePaths();
		return getShortestPathImpl(a, b);
	}

	private GraphPath<V, E> getShortestPathImpl(final V a, final V b) {
		int v_a = vertices.indexOf(a);
		final int v_b = vertices.indexOf(b);
		int prev = v_a;
		final List<E> edges = new ArrayList<E>();
		if (matrix != null) {
			v_a = matrix.get(GAMA.getRuntimeScope(), v_b, v_a);
			if (v_a != -1) {
				while (prev != v_b) {
					final Set<E> eds = graph.getAllEdges(vertices.get(prev), vertices.get(v_a));
					if (!eds.isEmpty()) {
						double minW = Double.MAX_VALUE;
						E ed = null;
						for (final E e : eds) {
							final double w = graph.getEdgeWeight(e);
							if (w < minW) {
								minW = w;
								ed = e;
							}
						}
						edges.add(ed);
					} else {
						return null;
					}
					if (prev != v_b) {
						prev = v_a;
						v_a = matrix.get(GAMA.getRuntimeScope(), v_b, v_a);
					}
				}
			}
		} else {
			shortestPathRecur(edges, v_a, v_b);
		}

		// no path, return null
		if (edges.size() < 1) { return null; }
		final GraphPathImpl<V, E> path = new GraphPathImpl<V, E>(graph, a, b, edges, edges.size());
		if (graph.isSaveComputedShortestPaths()) {
			final V v_i = vertices.get(v_a);
			final V v_j = vertices.get(v_b);

			paths.put(new VertexPair<V>(v_i, v_j), path);
			nShortestPaths++;
		}
		return path;
	}

	/**
	 * Calculate the shortest paths (not done per default)
	 */
	private void lazyCalculatePaths() {
		// already we have calculated it once.
		if (paths != null || matrix != null) { return; }

		lazyCalculateMatrix();

		this.paths = new THashMap<VertexPair<V>, GraphPath<V, E>>();

		nShortestPaths = 0;

	}
}

// End FloydWarshallShortestPaths.java