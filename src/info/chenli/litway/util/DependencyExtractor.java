package info.chenli.litway.util;

import info.chenli.litway.corpora.Token;

import java.util.List;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/***
 * This class is a demonstration program for creating a depencency chart,
 * directed graph, then locating and outputting any implicit loops, cycles.
 **/
public class DependencyExtractor {

	private CycleDetector<String, DefaultEdge> cycleDetector;
	private DefaultDirectedGraph<Token, DefaultEdge> network = new DefaultDirectedGraph<Token, DefaultEdge>(
			DefaultEdge.class);

	/**
	 * Constructor initializes the network
	 * 
	 * @param tokens
	 */
	public DependencyExtractor(List<Token> tokens) {

		// Add tokens as vertices.
		for (Token token : tokens) {
			network.addVertex(token);
		}

		// Add dependencies as edges.
		for (Token token : tokens) {
			if (null != token.getDependent()) {
				network.addEdge(token, token.getDependent());
			}
		}

	}

	public String getDijkstraShortestPath(Token startToken, Token endToken) {

		String dependencyPath = "";
		List<DefaultEdge> edges = DijkstraShortestPath.findPathBetween(network,
				startToken, endToken);

		if (null != edges && edges.size() > 0) {
			for (DefaultEdge edge : edges) {
				dependencyPath = dependencyPath.concat("_").concat(
						network.getEdgeSource(edge).getRelation());
			}
		}

		return dependencyPath;
	}

	/**
	 * Generate two cases, one with cycles, this is depencencies and one
	 * without.
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {

	}
}