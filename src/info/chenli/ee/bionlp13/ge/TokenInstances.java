package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.StructuredInstance;

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

		super("tokens", Token.type);

	}

	@Override
	protected List<String> getFeaturesString() {

		featuresString = new ArrayList<String>();
		featuresString.add("text");
		featuresString.add("lemma");
		featuresString.add("stem");
		featuresString.add("pos");
		featuresString.add("leftToken");
		featuresString.add("rightToken");

		return featuresString;
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

		Instance instance = new Instance();

		List<String> featureString = new ArrayList<String>();
		instance.setFeaturesString(featureString);

		featureString.add(token.getCoveredText());
		featureString.add(token.getLemma());
		featureString.add(token.getPos());
		featureString.add(token.getStem());
		featureString.add(null == token.getLeftToken() ? "" : token
				.getLeftToken().getCoveredText());
		featureString.add(null == token.getRightToken() ? "" : token
				.getRightToken().getCoveredText());

		if (null != triggerTokens) {

			instance.setLabelString(triggerTokens.containsKey(token.getId()) ? triggerTokens
					.get(token.getId()) : String.valueOf(EventType.Non_trigger));
		}

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
