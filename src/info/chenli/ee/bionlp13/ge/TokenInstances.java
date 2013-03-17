package info.chenli.ee.bionlp13.ge;

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

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class TokenInstances extends
		info.chenli.ee.bionlp13.ge.AbstractInstances {

	private final static Logger logger = Logger.getLogger(TokenInstances.class
			.getName());

	public TokenInstances() {

		super("tokens", Token.type);

	}

	@Override
	protected void initAttributes() {

		Attribute textAttr = new Attribute("text", (ArrayList<String>) null);
		Attribute lemmaAttr = new Attribute("lemma", (ArrayList<String>) null);
		Attribute posAttr = new Attribute("pos", (ArrayList<String>) null);
		Attribute leftTokenAttr = new Attribute("leftToken",
				(ArrayList<String>) null);
		Attribute rightTokenAttr = new Attribute("rightToken",
				(ArrayList<String>) null);

		attributes = new ArrayList<Attribute>();
		attributes.add(textAttr);
		attributes.add(lemmaAttr);
		attributes.add(posAttr);
		attributes.add(leftTokenAttr);
		attributes.add(rightTokenAttr);

	}

	@Override
	protected Attribute getClasses() {

		ArrayList<String> tokenTypes = new ArrayList<String>();
		for (EventType eventType : EventType.values()) {
			tokenTypes.add(String.valueOf(eventType));
		}

		return new Attribute("class", tokenTypes);

	}

	@Override
	protected List<StructuredInstance> fetchStructuredInstances(JCas jcas,
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
				// get tokens
				List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
						trigger);

				if (tokens.size() == 0)
				// if trigger is within a token, then take
				// the nesting token. It
				// happens, e.g. in PMC-2065877-01-Introduction.
				{
					FSIterator<Annotation> iter = jcas.getAnnotationIndex(
							Token.type).iterator();
					while (iter.hasNext()) {
						Token token = (Token) iter.next();
						if (token.getBegin() <= trigger.getBegin()
								&& token.getEnd() >= trigger.getEnd()) {
							triggerTokens.put(token.getId(),
									trigger.getEventType());
							break;
						}
					}

				} else
				// take one of the nested tokens.
				{
					triggerTokens.put(getTriggerToken(tokens).getId(),
							trigger.getEventType());
				}
			}

			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentence);

			for (Token token : tokens) {

				double[] values = new double[instances.numAttributes()];

				values[0] = instances.attribute(0).addStringValue(
						token.getCoveredText());
				values[1] = instances.attribute(1).addStringValue(
						token.getLemma());
				values[2] = instances.attribute(2).addStringValue(
						token.getPos());
				values[3] = instances.attribute(3).addStringValue(
						null == token.getLeftToken() ? "" : token
								.getLeftToken().getCoveredText());
				values[4] = instances.attribute(4).addStringValue(
						null == token.getRightToken() ? "" : token
								.getRightToken().getCoveredText());
				String eventType = triggerTokens.containsKey(token.getId()) ? triggerTokens
						.get(token.getId()) : String
						.valueOf(EventType.Non_trigger);
				values[5] = classes.indexOfValue(eventType);

				nodes.add(new DenseInstance(1.0, values));
			}

			results.add(si);
		}

		return results;
	}

	@Override
	public File getTaeDescriptor() {

		return new File("./desc/Annotator.xml");
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.fetchInstances(new File(args[0]));
		System.out.println(ti.getInstances());
	}
}
