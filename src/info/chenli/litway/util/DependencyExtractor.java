package info.chenli.litway.util;

import info.chenli.litway.bionlp13.ge.Stage;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/***
 * This class is a demonstration program for creating a depencency chart,
 * directed graph, then locating and outputting any implicit loops, cycles.
 **/
public class DependencyExtractor {

	private final static Logger logger = Logger
			.getLogger(DependencyExtractor.class.getName());

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
				logger.warning("Duplicated token error.");
			}
		}

		// Add dependencies as edges.
		for (Token token : tokens) {
			for (Pair pair : pairsOfSentence) {
				if (pair.getRelation().equalsIgnoreCase("punct")
						|| pair.getRelation().equalsIgnoreCase("root")) {
					continue;
				}
				if (token.getId() == pair.getHead()) {
					try {
						network.addEdge(token.getId(), pair.getModifier());
						reversedNetwork.addEdge(pair.getModifier(),
								token.getId());

					} catch (IllegalArgumentException e) {
						logger.severe("The token couldn't be found.");
					}
				}
			}
		}

	}

	public String getSimplifiedShortestPath(Token startToken, Token endToken,
			Stage stage) {
		return getDijkstraShortestPath(startToken, endToken, network, false,
				true, stage);
	}

	public String getSimplifiedReversedShortestPath(Token startToken,
			Token endToken, Stage stage) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, true, stage);
	}

	public String getShortestPath(Token startToken, Token endToken, Stage stage) {
		return getDijkstraShortestPath(startToken, endToken, network, false,
				false, stage);
	}

	public String getReversedShortestPath(Token startToken, Token endToken,
			Stage stage) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, false, stage);
	}

	public String getShortestPathText(Token startToken, Token endToken,
			Stage stage) {
		return getDijkstraShortestPath(startToken, endToken, reversedNetwork,
				true, false, stage);
	}

	private String getDijkstraShortestPath(Token startToken, Token endToken,
			DefaultDirectedGraph<Integer, DefaultEdge> theNetwork,
			boolean reversedNetwork, boolean simplified, Stage stage) {

		if (null == tokenMap || null == startToken
				|| null == endToken // TODO check later
				|| !tokenMap.get(startToken.getId()).equals(startToken)
				|| !tokenMap.get(endToken.getId()).equals(endToken)) {
			return null;
			// throw new
			// RuntimeException("Tokens are not from the same sentence.");
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
							relation = pair.getSimpleRelation();
						}
						if (null == dependencyPath) {
							dependencyPath = relation;
						} else {
							dependencyPath = dependencyPath.concat("_")
									.concat(reversedNetwork ? "-" : "")
									.concat(relation);
						}
					}
				}
			}

			//
			// Need to find a way to have constrainsts on the process level
			// rather than dependency path extraction level, as it jeopardize
			// the application flexibility
			//
			if (null != stage && stage.equals(Stage.THEME)) {
				// create equal path for the tokens connected by "or"
				if (null != dependencyPath
						&& dependencyPath.indexOf("prep_of") > -1) {
					if (dependencyPath.indexOf("conj_or_") > -1) {
						dependencyPath = dependencyPath.replaceAll("conj_or_",
								"");
					} else if (dependencyPath.indexOf("_conj_or") > -1) {
						dependencyPath = dependencyPath.replaceAll("_conj_or",
								"");
					}
				}
				// } else if (stage.equals(Stage.CAUSE)) {
				// if (null != dependencyPath
				// && dependencyPath.indexOf("dep") > -1) {
				// if (dependencyPath.indexOf("dep_") > -1) {
				// dependencyPath = dependencyPath.replaceAll("dep_", "");
				// } else if (dependencyPath.indexOf("_dep") > -1) {
				// dependencyPath = dependencyPath.replaceAll("_dep", "");
				// }
				// }
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