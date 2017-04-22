package msi.gama.util.graph;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import msi.gama.TestUtils;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.PostponedWarningList;
import msi.gama.util.graph.loader.*;
import msi.gama.util.graph.writer.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests graph writing and loading: take a graph, write it;
 * if a loader enables its reading, also read it, and compare results.
 * Ensures that declared formats are coherent: if i can save a graph in "GML",
 * and a "GML" reader is available, they should work and apply an identity.
 * In the same way, if "gephi.gml" is available both as a reader and writer,
 * they should work together.
 * 
 * @author Samuel Thiriot
 */
@RunWith(value = Parameterized.class)
public class TestWriteGraphAndLoadIfPossible {

	@Parameters
	public static Collection data() {

		LinkedList params = new LinkedList();
		for ( String format : AvailableGraphWriters.getAvailableWriters() ) {
			for ( GamaGraph g : TestUtilsGraphs.getGamaGraphsForTest() ) {
				Object[] p = new Object[2];
				p[0] = format;
				p[1] = g;
				params.add(p);
			}
		}
		return params;

	}

	// initialized by the testing framework
	private final String format;
	private final GamaGraph graph;

	// initialized before test
	private File fileGraph;
	private GamaGraph readen = null;

	public TestWriteGraphAndLoadIfPossible(final String writingFormat, final GamaGraph graph) {
		this.format = writingFormat;
		this.graph = graph;
		System.out.println("*** created test for format:" + writingFormat + " and graph:");
		System.out.println(graph.toString());

	}

	@BeforeClass
	public static void setUpOnce() {

		// view graph loader & writers warnings
		PostponedWarningList.writeSystemOut = true;

	}

	@Before
	public void setUp() {

		// write with neutral ext
		fileGraph = testWriteInDefinedFormat(graph, "tmp");

		if ( AvailableGraphParsers.getAvailableLoaders().contains(format) ) {
			System.out.println("this format is supported for reading, attempting to re-read this graph");

			readen = GraphLoader.loadGraph(null, fileGraph.getAbsolutePath(), null, null, null, null, format, false);
			assertNotNull(readen);
			// TestUtilsGraphs.compareGamaGraphs(format, graph, readen, 0); // TODO

		} else {
			System.out.println("this format is not supported for reading, and error should be thrown !");
			try {
				readen =
					GraphLoader.loadGraph(null, fileGraph.getAbsolutePath(), null, null, null, null, format, false);
				fail("expected a GamaRuntimeException");
			} catch (GamaRuntimeException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@After
	public void tearDown() {
		fileGraph.delete();
	}

	/**
	 * Attempts to write the graph in the current format.
	 * ensures a file was created and returns its path.
	 * @param graph
	 * @return
	 */
	private File testWriteInDefinedFormat(final GamaGraph graph, final String extension) {

		System.out.println("testing the writing in this format: " + format);

		File file = TestUtils.getTmpFile("emptyGraph", extension);

		IGraphWriter writer = AvailableGraphWriters.getGraphWriter(format);
		System.out.println("will use writer: " + writer.getClass().getCanonicalName());
		writer.writeGraph(null, graph, null, file.getAbsolutePath());

		assertTrue(file.exists());
		assertFalse(file.isDirectory());

		System.out.println("(file was created)");
		return file;
	}

	/**
	 * Attempts to read a file by specifying the current format as a format.
	 * If successful, returns the created graph as a result.
	 * @return
	 */
	private void testReadInDefaultFormat(final String filename) {

		if ( AvailableGraphParsers.getAvailableLoaders().contains(format) ) {
			System.out.println("this format is supported for reading, attempting to re-read this graph");

			GamaGraph readen = GraphLoader.loadGraph(null, filename, null, null, null, null, format, false);
			assertFalse(graph == readen);
			TestUtilsGraphs.compareGamaGraphs(format, graph, readen, 0); // TODO

		} else {
			System.out.println("this format is not supported for reading, and error should be thrown !");
			try {
				GamaGraph readen = GraphLoader.loadGraph(null, filename, null, null, null, null, format, false);
				fail("expected a GamaRuntimeException");
			} catch (GamaRuntimeException e) {
				System.out.println(e.getMessage());
			}
		}

	}

	/*
	 * @Test
	 * public void canAndReadFormatDefined() {
	 * 
	 * // read with define format if possible
	 * testReadInDefaultFormat(filename);
	 * }
	 */

	@Test
	public void sameNumberEdges() {
		if ( readen == null ) { return; }

		assertEquals(graph.getEdges().size(), readen.getEdges().size());
	}

	@Test
	public void sameNumberNodes() {
		if ( readen == null ) { return; }
		assertEquals(graph.getVertices().size(), readen.getVertices().size());
	}

	@Test
	public void sameDirectionality() {
		if ( readen == null ) { return; }
		assertEquals(graph.isDirected(), readen.isDirected());
	}

	@Test
	public void sameVertices() {
		if ( readen == null ) { return; }
		for ( Object v : graph.getVertices() ) {
			assertTrue(format + ": node not found " + v, readen.containsVertex(v));
		}
	}

	@Test
	public void sameNodes() {
		if ( readen == null ) { return; }
		for ( Object e : graph.getEdges() ) {
			Object source = graph.getEdgeSource(e);
			Object target = graph.getEdgeTarget(e);
			assertTrue(format + ": edge " + source + "->" + target + " not found", readen.containsEdge(source, target));
		}
	}

	@Test
	public void notSameObjects() {
		if ( readen == null ) { return; }
		assertNotSame(graph, readen);
	}

}
