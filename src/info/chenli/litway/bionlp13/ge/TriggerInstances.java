package info.chenli.litway.bionlp13.ge;

import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class TriggerInstances extends AbstractInstances {

	private final static Logger logger = Logger
			.getLogger(TriggerInstances.class.getName());

	public TriggerInstances() {

		super("triggers", Trigger.type);

	}

	@Override
	protected List<String> getFeaturesString() {

		return null;
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
			FSIterator<Annotation> annoIter) {

		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		// set annotations to the instance
		while (annoIter.isValid()) {

			Trigger trigger = (Trigger) annoIter.get();

			// get tokens
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					trigger);
			Token triggerToken = null;

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
						triggerToken = token;
						break;
					}
				}

			} else
			// take one of the nested tokens.
			{

				triggerToken = getTriggerToken(tokens);
			}

//			double[] values = new double[instances.numAttributes()];
//
//			values[0] = instances.attribute(0).addStringValue(
//					triggerToken.getCoveredText());
//			values[1] = instances.attribute(1).addStringValue(
//					triggerToken.getLemma());
//			values[2] = instances.attribute(2).addStringValue(
//					triggerToken.getPos());
//			values[3] = instances.attribute(3).addStringValue(
//					null == triggerToken.getLeftToken() ? "" : triggerToken
//							.getLeftToken().getCoveredText());
//			values[4] = instances.attribute(4).addStringValue(
//					null == triggerToken.getRightToken() ? "" : triggerToken
//							.getRightToken().getCoveredText());
//			values[5] = classes.indexOfValue(trigger.getEventType());
//
//			StructuredInstance si = new StructuredInstance();
//			// TODO this part is wrong and doesn't work properly due to the
//			// change in the upper class. need to be updated.
//			new DenseInstance(1.0, values);
//			results.add(si);

			annoIter.moveToNext();
		}

		return results;
	}

	@Override
	public File getTaeDescriptor() {
		return new File("./desc/Annotator.xml");
	}

	public static void main(String[] args) {

		TriggerInstances ti = new TriggerInstances();
		ti.getInstances(new File(args[0]));
		System.out.println(ti.getInstances());
	}

}
