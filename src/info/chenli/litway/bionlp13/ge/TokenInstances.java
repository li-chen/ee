package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
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

		super("tokens", new int[] { Token.type });

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

			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentence);

			for (Token token : tokens) {

				if (!isWord(token.getCoveredText().toLowerCase())
						|| !POS.isPos(token.getPos())
				// || StopWords.isAStopWordShort(token.getCoveredText()
				// .toLowerCase())
				) {
					continue;
				}

				nodes.add(tokenToInstance(token, triggerTokens, tokens,
						pairsOfSentence));
			}

			results.add(si);
		}

		return results;
	}

	/**
	 * 
	 * @param token
	 * @param triggerTokens
	 *            Used for extracting instances from training set. null when
	 *            being used for testing set.
	 * @return
	 */
	protected Instance tokenToInstance(Token token,
			Map<Integer, String> triggerTokens, List<Token> tokens,
			Set<Pair> pairsOfSentence) {

		// only consider the tokens, which are words.
		Instance instance = new Instance();

		List<String[]> featureString = new ArrayList<String[]>();
		instance.setFeaturesString(featureString);

		featureString
				.add(new String[] { token.getCoveredText().toLowerCase() });
		String lemma = token.getLemma().toLowerCase();
		featureString.add(new String[] { lemma });
		featureString.add(new String[] { token.getStem().toLowerCase() });
		String pos = token.getPos();
		featureString.add(new String[] { lemma.concat("_").concat(pos) });

		List<String> modifiers = new ArrayList<String>();
		List<String> heads = new ArrayList<String>();
		for (Pair pair : pairsOfSentence) {
			if (pair.getRelation().equalsIgnoreCase("punct")) {
				continue;
			}
			if (pair.getHead() == token.getId()) {
				for (Token aToken : tokens) {
					if (aToken.getId() == pair.getModifier()) {
						modifiers.add(lemma.concat("_")
								.concat(pair.getRelation()).concat("_")
								.concat(aToken.getLemma().toLowerCase()));
					}
				}
			} else if (pair.getModifier() == token.getId()) {
				for (Token aToken : tokens) {
					if (aToken.getId() == pair.getHead()) {
						heads.add(lemma.concat("_-").concat(pair.getRelation())
								.concat("_")
								.concat(aToken.getLemma().toLowerCase()));
					}
				}
			}
		}
		String[] modifiersFeature = new String[modifiers.size()];
		modifiersFeature = modifiers.toArray(modifiersFeature);
		String[] headsFeature = new String[heads.size()];
		headsFeature = heads.toArray(headsFeature);

		featureString.add(modifiersFeature);
		featureString.add(headsFeature);

		featureString
				.add(new String[] { token.getSubLemma().equals("") ? aStopWord
						: token.getSubLemma() });
		featureString
				.add(new String[] { token.getSubStem().equals("") ? aStopWord
						: token.getSubStem() });

		if (null != triggerTokens) {

			instance.setLabelString(triggerTokens.containsKey(token.getId()) ? triggerTokens
					.get(token.getId()) : String.valueOf(EventType.Non_trigger));
		}

		return instance;
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
