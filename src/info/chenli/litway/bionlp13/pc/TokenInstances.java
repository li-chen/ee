package info.chenli.litway.bionlp13.pc;

import info.chenli.classifier.Instance;
import info.chenli.litway.StopWords;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Prefix;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Suffix;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			LinkedList<Instance> nodes = new LinkedList<Instance>();
			si.setNodes(nodes);

			Sentence sentence = (Sentence) sentenceIter.next();

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

				nodes.add(tokenToInstance(token, triggerTokens));
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
			Map<Integer, String> triggerTokens) {

		// only consider the tokens, which are words.
		Instance instance = new Instance();

//		List<String> featureString = new ArrayList<String>();
//		instance.setFeaturesString(featureString);
//
//		featureString.add(token.getCoveredText().toLowerCase());
//		String lemma = token.getLemma().toLowerCase();
//		featureString.add(lemma);
//		String pos = token.getPos();
//		featureString.add(pos);
//		featureString.add(token.getStem().toLowerCase());
//		featureString.add(lemma.concat("_").concat(pos));
//
//		String leftTokenText = null == token.getLeftToken() ? "" : token
//				.getLeftToken().getCoveredText().toLowerCase();
//		featureString.add(isWord(leftTokenText)
//				&& !StopWords.isAStopWordShortForNgram(leftTokenText) ? lemma
//				.concat("_").concat(leftTokenText) : aStopWord);
//		featureString.add(isWord(leftTokenText)
//				&& !StopWords.isAStopWordShortForNgram(leftTokenText) ? lemma
//				.concat("_").concat(token.getLeftToken().getLemma())
//				: aStopWord);
//
//		String rightTokenText = null == token.getRightToken() ? "" : token
//				.getRightToken().getCoveredText().toLowerCase();
//		featureString.add(isWord(rightTokenText)
//				&& StopWords.isAStopWordShortForNgram(leftTokenText) ? lemma
//				.concat("_").concat(rightTokenText) : aStopWord);
//		featureString.add(isWord(rightTokenText)
//				&& StopWords.isAStopWordShortForNgram(leftTokenText) ? lemma
//				.concat("_").concat(token.getRightToken().getLemma())
//				: aStopWord);
//
//		String dependentText = null == token.getDependent() ? "" : token
//				.getDependent().getCoveredText().toLowerCase();
//		featureString.add(isWord(dependentText)
//				&& !StopWords.isAStopWordShortForNgram(dependentText) ? lemma
//				.concat("_").concat(pos).concat("_")
//				.concat(token.getDependent().getLemma().toLowerCase())
//				.concat("_").concat(token.getDependent().getPos()) : aStopWord);
////		featureString.add(isWord(dependentText)
////				&& !StopWords.isAStopWordShortForNgram(dependentText) ? pos
////				.concat("_").concat(token.getDependent().getPos()) : aStopWord);
//
//		String governorText = null == token.getGovernor() ? "" : token
//				.getGovernor().getCoveredText().toLowerCase();
//		featureString.add(isWord(governorText)
//				&& !StopWords.isAStopWordShortForNgram(governorText) ? lemma
//				.concat("_").concat(pos).concat("_")
//				.concat(token.getGovernor().getLemma().toLowerCase())
//				.concat("_").concat(token.getGovernor().getPos()) : aStopWord);
////		featureString.add(isWord(governorText)
////				&& !StopWords.isAStopWordShortForNgram(governorText) ? pos
////				.concat("_").concat(token.getGovernor().getPos()) : aStopWord);
//
//		featureString.add(token.getSubLemma().equals("") ? aStopWord : token
//				.getSubLemma());
//		featureString.add(token.getSubStem().equals("") ? aStopWord : token
//				.getSubStem());
//		// String prefix = Prefix.getPrefix(token.getCoveredText());
//		// featureString.add(prefix.equals("") ? aStopWord : lemma.concat("_")
//		// .concat(prefix));
//		// String suffix = Suffix.getSuffix(token.getCoveredText());
//		// featureString.add(suffix.equals("") ? aStopWord : lemma.concat("_")
//		// .concat(suffix));
//		featureString.add(lemma.concat("_").concat(
//				Prefix.getPrefix(token.getCoveredText())));
//		featureString.add(lemma.concat("_").concat(
//				Suffix.getSuffix(token.getCoveredText())));
//
//		if (null != triggerTokens) {
//
//			instance.setLabelString(triggerTokens.containsKey(token.getId()) ? triggerTokens
//					.get(token.getId()) : String.valueOf(EventType.Non_trigger));
//		}
//
		return instance;
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.getInstances(new File(args[0]));

		// for (Instance instance : ti.getInstances()) {
		// System.out.print(instance.getLabelString());
		// for (String feature : instance.getFeaturesString()) {
		// System.out.print("\t".concat(feature));
		// }
		// System.out.println();
		// }
	}

}
