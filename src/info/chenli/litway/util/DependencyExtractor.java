package info.chenli.litway.util;

import info.chenli.litway.corpora.Token;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	private DefaultDirectedGraph<Integer, DefaultEdge> network = new DefaultDirectedGraph<Integer, DefaultEdge>(
			DefaultEdge.class);
	private DefaultDirectedGraph<Integer, DefaultEdge> reversedNetwork = new DefaultDirectedGraph<Integer, DefaultEdge>(
			DefaultEdge.class);
	private Map<Integer, Token> tokenMap = new TreeMap<Integer, Token>();

	private Set<Pair> dependencyPairs;

	/**
	 * Constructor initializes the network
	 * 
	 * @param tokens
	 */
	public DependencyExtractor(List<Token> tokens, Set<Pair> pairsOfSentence) {

		this.dependencyPairs = pairsOfSentence;

		// Add tokens as vertices.
		for (Token token : tokens) {
			network.addVertex(token.getId());
			reversedNetwork.addVertex(token.getId());

			if (!tokenMap.containsKey(token.getId())) {
				tokenMap.put(token.getId(), token);
			} else {
				throw new RuntimeException("Duplicated token error.");
			}
		}

		// Add dependencies as edges.
		for (Token token : tokens) {
			for (Pair pair : pairsOfSentence) {
				if (token.getId() == pair.getHead()) {
					network.addEdge(token.getId(), pair.getModifier());
					reversedNetwork.addEdge(pair.getModifier(), token.getId());
				}
			}
		}

	}

	public String getSimplifiedShortestPath(Token startToken, Token endToken) {
		return getDijkstraShortestPath(startToken, endToken, network, false,
				true);
	}

	public String getSimplifiedReversedShortestPath(Token startToken,
			Token endToken) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, true);
	}

	public String getShortestPath(Token startToken, Token endToken) {
		return getDijkstraShortestPath(startToken, endToken, network, false,
				false);
	}

	public String getReversedShortestPath(Token startToken, Token endToken) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, false);
	}

	public String getShortestPathText(Token startToken, Token endToken) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, false);
	}

	private String getDijkstraShortestPath(Token startToken, Token endToken,
			DefaultDirectedGraph<Integer, DefaultEdge> theNetwork,
			boolean reversedNetwork, boolean simplified) {

		if (!tokenMap.get(startToken.getId()).equals(startToken)
				|| !tokenMap.get(endToken.getId()).equals(endToken)) {
			throw new RuntimeException("Tokens are not from the same sentence.");
		}

		String dependencyPath = null;
		List<DefaultEdge> edges = DijkstraShortestPath.findPathBetween(
				theNetwork, startToken.getId(), endToken.getId());

		if (null != edges && edges.size() > 0) {
			for (DefaultEdge edge : edges) {
				for (Pair pair : dependencyPairs) {
					if (theNetwork.getEdgeSource(edge) == pair.getHead()
							&& theNetwork.getEdgeTarget(edge) == pair
									.getModifier()) {
						String relation = pair.getRelation();
						if (simplified) {
							if (relation.equals("nn")
									|| relation.equals("amond")) {
								relation = "nmod";
							} else if (relation.endsWith("subj")) {
								relation = "subj";
							} else if (relation.endsWith("subjpass")) {
								relation = "subjpass";
							}
						}
						dependencyPath = dependencyPath.concat("_")
								.concat(reversedNetwork ? "-" : "")
								.concat(relation).concat("_");
					}
				}
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