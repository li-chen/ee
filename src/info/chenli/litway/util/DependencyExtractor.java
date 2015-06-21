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
				//System.out.println(token.getCoveredText());
			} else {
				//System.out.println(token.getCoveredText());
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
						//if (pair.getRelation().equalsIgnoreCase("conj_and")
								//|| pair.getRelation().equalsIgnoreCase("nsubj")
								//|| pair.getRelation().equalsIgnoreCase("nsubjpass")
								//|| pair.getRelation().equalsIgnoreCase("nn")
								//|| pair.getRelation().equalsIgnoreCase("amod")
								//) {
							network.addEdge(pair.getModifier(),
									token.getId());
							reversedNetwork.addEdge(token.getId(), pair.getModifier());
						//}
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
		/*if (null != edges && edges.size() > 5) {
			return dependencyPath;
		}*/
		//int a = startToken.getId();
		//int b = endToken.getId();
		if (null != edges && edges.size() > 0) {
			//int del = edges.size();
			for (DefaultEdge edge : edges) {
				for (Pair pair : dependencyPairs) {
					if ((theNetwork.getEdgeSource(edge) == pair.getHead()
									&& theNetwork.getEdgeTarget(edge) == pair.getModifier())  
								|| (theNetwork.getEdgeSource(edge) == pair.getModifier()
									&& theNetwork.getEdgeTarget(edge) == pair.getHead())) {
						String relation = pair.getRelation();
						/*if (del > 1 && (relation.equals("nn") || relation.equals("amod"))) {
							del--;
							break;
						}*/
						/*if (simplified) {
							if (relation.equals("nn")
									|| relation.equals("amod")) {
								relation = "nmod";
							} else if (relation.endsWith("subj")) {
								relation = "subj";
							}  else if (relation.endsWith("subjpass")) {
								relation = "subjpass";
							} //else if (relation.startsWith("prep")) {
								//relation = "prep";
							//}
						}*/
						if ((((theNetwork.getEdgeSource(edge) == pair.getModifier()
									&& theNetwork.getEdgeTarget(edge) == pair.getHead())
									//&& (!pair.getRelation().equalsIgnoreCase("conj_and")
									//	&& !pair.getRelation().equalsIgnoreCase("conj_or")
									//	&& !pair.getRelation().equalsIgnoreCase("appos"))
										))) {
							relation = "-".concat(relation);
						}
						if (null == dependencyPath) {
							dependencyPath = relation;
						} else {
							dependencyPath = dependencyPath.concat("_")
									.concat(relation);
						}
					}
				}
			}
			/*if (stage.equals(Stage.THEME)) {
				// create equal path for the tokens connected by "or","and"
				if (null != dependencyPath) {
					dependencyPath = dependencyPath.replaceAll("prep_of_conj_or", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-conj_or", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_appos", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-appos", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_nn", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-nn", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_amod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-amod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_nmod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-nmod", "prep_of");
					
					dependencyPath = dependencyPath.replaceAll("prep_for_conj_or", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-conj_or", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_conj_and", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-conj_and", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_appos", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-appos", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_nn", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-nn", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_amod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-amod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_nmod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-nmod", "prep_for");

					dependencyPath = dependencyPath.replaceAll("prep_between_conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_between_-conj_and", "prep_of");

					dependencyPath = dependencyPath.replaceAll("prep_with_conj_or", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-conj_or", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_conj_and", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-conj_and", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_appos", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-appos", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_nn", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-nn", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_amod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-amod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_nmod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-nmod", "prep_with");
					
					dependencyPath = dependencyPath.replaceAll("prep_through_conj_or", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-conj_or", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_conj_and", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-conj_and", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_appos", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-appos", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_nn", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-nn", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_amod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-amod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_nmod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-nmod", "prep_through");
					
					dependencyPath = dependencyPath.replaceAll("prep_in_conj_or", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-conj_or", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_conj_and", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-conj_and", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_appos", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-appos", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_nn", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-nn", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_amod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-amod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_nmod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-nmod", "prep_in");
				}
			}else if (stage.equals(Stage.BINDING)) {
				// create equal path for the tokens connected by "or"
				if (null != dependencyPath) {
					dependencyPath = dependencyPath.replaceAll("prep_of_conj_or", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-conj_or", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_appos", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-appos", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_nn", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-nn", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_amod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-amod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_nmod", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_of_-nmod", "prep_of");
					
					dependencyPath = dependencyPath.replaceAll("prep_for_conj_or", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-conj_or", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_conj_and", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-conj_and", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_appos", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-appos", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_nn", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-nn", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_amod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-amod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_nmod", "prep_for");
					dependencyPath = dependencyPath.replaceAll("prep_for_-nmod", "prep_for");

					dependencyPath = dependencyPath.replaceAll("prep_between_conj_and", "prep_of");
					dependencyPath = dependencyPath.replaceAll("prep_between_-conj_and", "prep_of");

					dependencyPath = dependencyPath.replaceAll("prep_with_conj_or", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-conj_or", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_conj_and", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-conj_and", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_appos", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-appos", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_nn", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-nn", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_amod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-amod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_nmod", "prep_with");
					dependencyPath = dependencyPath.replaceAll("prep_with_-nmod", "prep_with");
					
					dependencyPath = dependencyPath.replaceAll("prep_through_conj_or", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-conj_or", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_conj_and", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-conj_and", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_appos", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-appos", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_nn", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-nn", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_amod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-amod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_nmod", "prep_through");
					dependencyPath = dependencyPath.replaceAll("prep_through_-nmod", "prep_through");
					
					dependencyPath = dependencyPath.replaceAll("prep_in_conj_or", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-conj_or", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_conj_and", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-conj_and", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_appos", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-appos", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_nn", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-nn", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_amod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-amod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_nmod", "prep_in");
					dependencyPath = dependencyPath.replaceAll("prep_in_-nmod", "prep_in");
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					dependencyPath = dependencyPath.replaceAll("conj_or_", "");
					dependencyPath = dependencyPath.replaceAll("_conj_or", "");
					dependencyPath = dependencyPath.replaceAll("_-conj_or", "");
					dependencyPath = dependencyPath.replaceAll("-conj_or_", "");
					
					dependencyPath = dependencyPath.replaceAll("conj_and_", "");
					dependencyPath = dependencyPath.replaceAll("_conj_and", "");
					dependencyPath = dependencyPath.replaceAll("_-conj_and", "");
					dependencyPath = dependencyPath.replaceAll("-conj_and_", "");
					
					dependencyPath = dependencyPath.replaceAll("appos_", "");
					dependencyPath = dependencyPath.replaceAll("_appos", "");
					dependencyPath = dependencyPath.replaceAll("_-appos", "");
					dependencyPath = dependencyPath.replaceAll("-appos_", "");
					
					dependencyPath = dependencyPath.replaceAll("nn_", "");
					dependencyPath = dependencyPath.replaceAll("_nn", "");
					dependencyPath = dependencyPath.replaceAll("_-nn", "");
					dependencyPath = dependencyPath.replaceAll("-nn_", "");

					dependencyPath = dependencyPath.replaceAll("amod_", "");
					dependencyPath = dependencyPath.replaceAll("_amod", "");
					dependencyPath = dependencyPath.replaceAll("_-amod", "");
					dependencyPath = dependencyPath.replaceAll("-amod_", "");
					
					dependencyPath = dependencyPath.replaceAll("dep_", "");
					dependencyPath = dependencyPath.replaceAll("_dep", "");
					dependencyPath = dependencyPath.replaceAll("-dep_", "");
					dependencyPath = dependencyPath.replaceAll("_-dep", "");
					 
					dependencyPath = dependencyPath.replaceAll("nmod_", "");
					dependencyPath = dependencyPath.replaceAll("_nmod", "");
					dependencyPath = dependencyPath.replaceAll("_-nmod", "");
					dependencyPath = dependencyPath.replaceAll("-nmod_", "");
				}
			}*/
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
	public int getDijkstraShortestPathLength(Token startToken, Token endToken) {

		if (null == tokenMap || null == startToken
				|| null == endToken // TODO check later
				|| !tokenMap.get(startToken.getId()).equals(startToken)
				|| !tokenMap.get(endToken.getId()).equals(endToken)) {
			return 0;
			// throw new
			// RuntimeException("Tokens are not from the same sentence.");
		}

		List<DefaultEdge> edges = DijkstraShortestPath.findPathBetween(
				network, startToken.getId(), endToken.getId());
		if (null != edges) {
			return edges.size();
		}else {
			return 0;
		}
	}
}