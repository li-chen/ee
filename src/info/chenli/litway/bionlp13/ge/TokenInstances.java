package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class TokenInstances extends AbstractInstances {

	private final static Logger logger = Logger.getLogger(TokenInstances.class
			.getName());

	public TokenInstances() {

		super(new int[] { Token.type });

	}

	@Override
	protected List<String> getLabelsString() {

		ArrayList<String> tokenTypes = new ArrayList<String>();

		for (EventType eventType : EventType.values()) {
			tokenTypes.add(String.valueOf(eventType));
		}

		return tokenTypes;
	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> tokenIter) {

		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		AnnotationIndex<Annotation> sentenceIndex = jcas
				.getAnnotationIndex(Sentence.type);

		FSIterator<Annotation> sentenceIter = sentenceIndex.iterator();
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			LinkedList<Instance> nodes = new LinkedList<Instance>();
			si.setNodes(nodes);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			List<Trigger> triggers = JCasUtil.selectCovered(jcas,
					Trigger.class, sentence);

			// token id and event type
			Map<Integer, String> triggerTokens = new HashMap<Integer, String>();

			// mark trigger tokens
			for (Trigger trigger : triggers) {
				triggerTokens.put(getTriggerToken(jcas, trigger).getId(),
						trigger.getEventType());
			}

			// List<Token> originalTokens = JCasUtil.selectCovered(jcas,
			// Token.class, sentence);
			List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			// // print proteins which are within a token
			// for (Token token : originalTokens) {
			//
			// for (Protein protein : sentenceProteins) {
			// if (token.getBegin() == protein.getBegin()
			// && token.getEnd() == protein.getEnd()) {
			// continue;
			// }
			// if (token.getBegin() <= protein.getBegin()
			// && token.getEnd() >= protein.getEnd()) {
			// System.out.println(token.getCoveredText().concat("\t")
			// .concat(protein.getCoveredText()));
			// }
			// }
			// }
			//
			// if (true) {
			// continue;
			// }
			// postProcessSentenceTokens(jcas, originalTokens, sentenceProteins,
			// pairsOfSentence);
			List<Token> tokensOfSentence = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);

			creatingInstanceLoop: for (Token token : tokensOfSentence) {

				// the tokens with protein have to be considered, as they may
				// have trigger
				// for (Protein protein : sentenceProteins) {
				// if (token.getBegin() == protein.getBegin()
				// && token.getEnd() == protein.getEnd()) {
				// continue creatingInstanceLoop;
				// }
				// }

				nodes.add(tokenToInstance(jcas, token, triggerTokens,
						tokensOfSentence, sentenceProteins, pairsOfSentence,
						dependencyExtractor));
			}

			results.add(si);
		}

		return results;
	}

	/**
	 * 
	 * @param jcas
	 * @param token
	 * @param triggerTokens
	 *            Used for extracting instances from training set. null when
	 *            being used for testing set.
	 * @param sentenceProteins
	 * @param dependencyExtractor
	 * @return
	 */
	protected Instance tokenToInstance(JCas jcas, Token token,
			Map<Integer, String> triggerTokens, List<Token> tokensOfSentence,
			List<Protein> sentenceProteins, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor) {

		// only consider the tokens, which are words.
		Instance instance = new Instance();

		List<String[]> featureString = new ArrayList<String[]>();
		instance.setFeaturesString(featureString);

		featureString.add(new String[] { "text_".concat(token.getCoveredText()
				.toLowerCase()) });
		String lemma = "lemma_".concat(token.getLemma().toLowerCase());
		featureString.add(new String[] { lemma });
		String stem = "stem_".concat(token.getStem().toLowerCase());
		featureString.add(new String[] { stem });
		String pos = "pos_".concat(token.getPos());
		featureString.add(new String[] { lemma.concat("_").concat(pos) });

		List<String> modifiers = new ArrayList<String>();
		List<String> heads = new ArrayList<String>();
		List<String> simpleModifiers = new ArrayList<String>();
		List<String> simpleHeads = new ArrayList<String>();
		List<String> noLemmaModifiers = new ArrayList<String>();
		List<String> noLemmaHeads = new ArrayList<String>();
		List<String> noDepModifiers = new ArrayList<String>();
		List<String> noDepHeads = new ArrayList<String>();
		List<String> nsubjList = new ArrayList<String>();
		List<String> dobjList = new ArrayList<String>();
		List<String> iobjList = new ArrayList<String>();
		for (Pair pair : pairsOfSentence) {
			if (pair.getRelation().equalsIgnoreCase("punct")) {
				continue;
			}
			if (pair.getHead() == token.getId()) {
				for (Token aToken : tokensOfSentence) {
					if (aToken.getId() == pair.getModifier()) {
						String tokenLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
								: aToken.getLemma().toLowerCase();
						modifiers.add(lemma.concat("_")
								.concat(pair.getRelation()).concat("_lemma_")
								.concat(tokenLemma));
						simpleModifiers.add(lemma.concat("_")
								.concat(pair.getSimpleRelation())
								.concat("_lemma_").concat(tokenLemma));
						noLemmaModifiers.add(pair.getRelation()
								.concat("_lemma_").concat(tokenLemma));
						noDepModifiers.add(lemma.concat("_lemma_").concat(
								tokenLemma));

						// if (pair.getRelation().equalsIgnoreCase("nsubj")) {
						// nsubjList.add("nsubj_lemma_".concat(aToken
						// .getLemma()));
						// }
						// if (pair.getRelation().equalsIgnoreCase("dobj")) {
						// dobjList.add("dobj_lemma_".concat(aToken.getLemma()));
						// }
						// if (pair.getRelation().equalsIgnoreCase("iobj")) {
						// iobjList.add("iobj_lemma_".concat(aToken.getLemma()));
						// }
					}
				}
			} else if (pair.getModifier() == token.getId()) {
				for (Token aToken : tokensOfSentence) {
					if (aToken.getId() == pair.getHead()) {
						String tokenLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
								: aToken.getLemma().toLowerCase();
						heads.add(lemma.concat("_-").concat(pair.getRelation())
								.concat("_lemma_").concat(tokenLemma));
						simpleHeads.add(lemma.concat("_-")
								.concat(pair.getSimpleRelation())
								.concat("_lemma_").concat(tokenLemma));
						noLemmaHeads.add(pair.getRelation().concat("_lemma_")
								.concat(tokenLemma));
						noDepHeads.add(lemma.concat("_-_lemma_").concat(
								tokenLemma));
					}
				}
			}
		}
		String[] modifiersFeature = new String[modifiers.size()];
		modifiersFeature = modifiers.toArray(modifiersFeature);
		String[] headsFeature = new String[heads.size()];
		headsFeature = heads.toArray(headsFeature);
		String[] simpleModifiersFeature = new String[simpleModifiers.size()];
		simpleModifiersFeature = simpleModifiers
				.toArray(simpleModifiersFeature);
		String[] simpleHeadsFeature = new String[simpleHeads.size()];
		simpleHeadsFeature = simpleHeads.toArray(simpleHeadsFeature);
		String[] noLemmaModifiersFeature = new String[noLemmaModifiers.size()];
		noLemmaModifiersFeature = noLemmaModifiers
				.toArray(noLemmaModifiersFeature);
		String[] noLemmaHeadsFeature = new String[noLemmaHeads.size()];
		noLemmaHeadsFeature = noLemmaHeads.toArray(noLemmaHeadsFeature);
		String[] noDepModifiersFeature = new String[noDepModifiers.size()];
		noDepModifiersFeature = noDepModifiers.toArray(noDepModifiersFeature);
		String[] noDepHeadsFeature = new String[noDepHeads.size()];
		noDepHeadsFeature = noDepHeads.toArray(noDepHeadsFeature);
		String[] nsubjFeature = new String[nsubjList.size()];
		nsubjFeature = nsubjList.toArray(nsubjFeature);
		String[] dobjFeature = new String[dobjList.size()];
		dobjFeature = dobjList.toArray(dobjFeature);
		String[] iobjFeature = new String[iobjList.size()];
		iobjFeature = iobjList.toArray(iobjFeature);

		featureString.add(modifiersFeature);
		featureString.add(headsFeature);
		// featureString.add(simpleModifiersFeature);
		// featureString.add(simpleHeadsFeature);
		// featureString.add(noLemmaModifiersFeature);
		// featureString.add(noLemmaHeadsFeature);
		featureString.add(noDepModifiersFeature);
		featureString.add(noDepHeadsFeature);
		// featureString.add(nsubjFeature);
		// featureString.add(dobjFeature);
		// featureString.add(iobjFeature);

		String subLemma = "sublemma_"
				.concat(null == token.getSubLemma() ? token.getLemma()
						.toLowerCase() : token.getSubLemma().toLowerCase());
		featureString.add(new String[] { subLemma });
		String subStem = "substem_".concat(null == token.getSubStem() ? token
				.getStem().toLowerCase() : token.getSubStem().toLowerCase());
		featureString.add(new String[] { subStem });

		//
		// ngram
		// previous word
		String leftTokenStr = token.getLeftToken() == null ? null : (POS
				.isPos(token.getLeftToken().getPos()) ? "previousWord_"
				.concat(token.getLeftToken().getLemma()) : null);
		// featureString.add(null == leftTokenStr ? new String[0]
		// : new String[] { leftTokenStr });
		// featureString.add(null == leftTokenStr ? new String[0]
		// : new String[] { lemma.concat("_").concat(leftTokenStr) });
		String posLeftTokenStr = token.getLeftToken() == null ? null : ((token
				.getLeftToken().getPos().indexOf("NN") > -1
				|| token.getLeftToken().getPos().indexOf("JJ") > -1 || token
				.getLeftToken().getPos().indexOf("V") > -1) ? lemma
				+ "_previousWord_".concat(token.getLeftToken().getLemma())
				: null);
		// featureString.add(null == posLeftTokenStr ? new String[0]
		// : new String[] { posLeftTokenStr });
		// after word
		String rightTokenStr = token.getRightToken() == null ? null : (POS
				.isPos(token.getRightToken().getPos()) ? "afterWord_"
				.concat(token.getRightToken().getLemma()) : null);
		// featureString.add(null == rightTokenStr ? new String[0]
		// : new String[] { rightTokenStr });
		// featureString.add(null == rightTokenStr ? new String[0]
		// : new String[] { lemma.concat("_").concat(rightTokenStr) });
		String posRightTokenStr = token.getRightToken() == null ? null : (token
				.getRightToken().getPos().indexOf("NN") > -1) ? lemma
				+ "_afterWord_".concat(token.getLeftToken().getLemma()) : null;
		// featureString.add(null == posRightTokenStr ? new String[0]
		// : new String[] { posRightTokenStr });

		// protein in the sentence
		String[] proteins = new String[sentenceProteins.size()];
		String[] proteinsDummy = sentenceProteins.size() > 0 ? new String[] { "PROTEIN" }
				: new String[0];
		String[] proteinsLemma = new String[sentenceProteins.size()];
		String[] proteinsDep = new String[sentenceProteins.size()];

		int i = 0;
		for (Protein protein : sentenceProteins) {

			Token aProteinToken = getProteinToken(jcas, protein);

			proteins[i] = "protein_"
					+ protein.getCoveredText().toLowerCase()
							.replaceAll(" ", "_");
			proteinsLemma[i] = lemma.concat("_").concat(
					protein.getCoveredText().toLowerCase());

			proteinsDep[i] = dependencyExtractor.getShortestPath(token,
					aProteinToken, Stage.TRIGGER);

			if (null == proteinsDep[i]) {
				proteinsDep[i] = dependencyExtractor.getReversedShortestPath(
						token, aProteinToken, Stage.TRIGGER);
			}
		}
		// featureString.add(proteins);
		featureString.add(proteinsDummy);
		// featureString.add(proteinsLemma);
		boolean isDepNull = true;
		for (String dep : proteinsDep) {
			if (null != dep) {
				isDepNull = false;
				break;
			}
		}
		// featureString.add(isDepNull ? new String[0] : proteinsDep);

		if (null != triggerTokens) {

			instance.setLabelString(triggerTokens.containsKey(token.getId()) ? triggerTokens
					.get(token.getId()) : String.valueOf(EventType.Non_trigger));
		} else {
			instance.setLabelString(String.valueOf(EventType.Non_trigger));
		}

		return instance;
	}

	// public void postProcessSentenceTokens(JCas jcas, List<Token> tokens,
	// List<Protein> sentenceProteins, Set<Pair> pairsOfSentence) {
	//
	// int i = tokens.size() + 1;
	//
	// tokenCollectingLoop: for (Token token : tokens) {
	//
	// if (!POS.isPos(token.getPos())) {
	// token.removeFromIndexes();
	// continue;
	// }
	//
	// for (Protein protein : sentenceProteins) {
	// // token is a protein
	// if (protein.getBegin() == token.getBegin()
	// && protein.getEnd() == token.getEnd()) {
	// continue tokenCollectingLoop;
	// }
	// // token is within a protein
	// if ((token.getBegin() >= protein.getBegin() && token.getBegin() < protein
	// .getEnd())
	// || (token.getBegin() > protein.getBegin() && token
	// .getBegin() <= protein.getEnd())) {
	// continue tokenCollectingLoop;
	// }
	// // protein is within a token (tricky part)
	// if ((token.getBegin() <= protein.getBegin() && token.getEnd() > protein
	// .getEnd())
	// || (token.getBegin() < protein.getBegin() && token
	// .getEnd() >= protein.getEnd())) {
	// Token proteinToken = createNewToken(jcas,
	// protein.getBegin(), protein.getEnd(), protein
	// .getCoveredText().toLowerCase(),
	// String.valueOf(POS.NN));
	// if (protein.getBegin() != token.getBegin()) {
	// Token leftToken = createNewToken(
	// jcas,
	// token.getBegin(),
	// protein.getBegin(),
	// token.getCoveredText()
	// .substring(
	// 0,
	// protein.getBegin()
	// - token.getBegin())
	// .toLowerCase(), String.valueOf(POS.NN));
	// leftToken.setId(i++);
	// leftToken.setLeftToken(token.getLeftToken());
	// leftToken.setRightToken(proteinToken);
	// proteinToken.setLeftToken(leftToken);
	// } else {
	// proteinToken.setLeftToken(token.getLeftToken());
	// }
	//
	// if (protein.getEnd() != token.getEnd()) {
	// Token rightToken = createNewToken(
	// jcas,
	// protein.getEnd(),
	// token.getEnd(),
	// token.getCoveredText()
	// .substring(
	// 0,
	// token.getEnd()
	// - protein.getEnd())
	// .toLowerCase(), String.valueOf(POS.NN));
	//
	// // use the original id of the token for the last token
	// rightToken.setId(token.getId());
	// proteinToken.setId(i++);
	// rightToken.setLeftToken(proteinToken);
	// proteinToken.setRightToken(rightToken);
	// rightToken.setRightToken(token.getRightToken());
	//
	// } else {
	// proteinToken.setRightToken(token.getRightToken());
	// proteinToken.setId(token.getId());
	// }
	// token.removeFromIndexes();
	// continue tokenCollectingLoop;
	// }
	// }
	// }
	// }

	private boolean isProtein(Token token, List<Protein> proteinsOfSentence) {
		for (Protein protein : proteinsOfSentence) {
			if ((token.getBegin() >= protein.getBegin() && token.getEnd() <= protein
					.getEnd())
					|| (protein.getBegin() >= token.getBegin() && protein
							.getEnd() <= token.getEnd())) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = ti.getInstances(new File(args[0]));

		for (Instance instance : instances) {
			System.out.print(instance.getLabelString());
			for (String[] features : instance.getFeaturesString()) {
				for (String feature : features) {
					System.out.print("\t".concat(feature));
				}
			}
			System.out.println();
		}
	}

}
