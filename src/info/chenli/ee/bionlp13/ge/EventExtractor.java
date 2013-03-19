package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.StructuredInstance;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import weka.core.Instance;

public class EventExtractor extends TokenInstances {

	Map<Integer, Protein> proteins = null;
	Map<Integer, Trigger> triggers = null;
	Map<Integer, String> events = null;

	void extract(File file) {

		// Initialise the file
		EventExtractor ee = new EventExtractor();
		JCas jcas = ee.processSingleFile(file, Token.type);

		// Initialise the classifiers
		TriggerRecogniser triggerRecogniser = new TriggerRecogniser();
		ThemeRecogniser themeRegconiser = new ThemeRecogniser();

		FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
				Sentence.type).iterator();

		int triggerIndex = proteins.size() + 1;
		while (sentenceIter.hasNext()) {

			//
			// trigger detection
			//
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentenceIter.next());

			for (Token token : tokens) {

				Instance tokenInstance = tokenToInstance(token, null);
				triggerRecogniser.classify(tokenInstance);

				if (tokenInstance.classIndex() != classes.indexOfValue(String
						.valueOf(EventType.Non_trigger))) {
					Trigger trigger = new Trigger(jcas, token.getBegin(),
							token.getEnd());
					trigger.setEventType(classes.value(tokenInstance
							.classIndex()));
					triggers.put(triggerIndex++, trigger);
				}
			}

			//
			// theme assignment
			//

			// 1. iterate through all proteins
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentenceIter.next());

			for (Protein protein : proteins) {
				
				Instance proteinInstance = proteinToInstance(protein, null);

			}
		}

		try {
			// files have to be preprocessed (sentencised, tokenized).
			for (Instance instance : tr.getInstances()) {
				System.out.println(instance == null);
				System.out.println(instance.hasMissingValue());
				System.out.println(null == classifier);
				classifier.classifyInstance(instance);
				instance.value(classes);
			}

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return null;

		// assign entity as theme for the simple events
		for (Trigger trigger : triggers.values()) {

		}

		// assign entity as theme for the complex events

		// assign event as theme for the complex events
	}

	@Override
	public File getTaeDescriptor() {

		return new File("./desc/TestSetAnnotator.xml");
	}
}
