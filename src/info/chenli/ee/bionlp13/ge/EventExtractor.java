package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.ee.corpora.Event;
import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.util.DependencyExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class EventExtractor extends TokenInstances {

	private List<Protein> proteins = new ArrayList<Protein>();
	private List<Trigger> triggers = new ArrayList<Trigger>();
	private List<Event> events = new ArrayList<Event>();

	/**
	 * Extract events from the given file.
	 * 
	 * @param file
	 */
	public void extract(File file) {

		// Initialize the file
		JCas jcas = this.processSingleFile(file, Token.type);
		FSIterator<Annotation> proteinIter = jcas.getAnnotationIndex(
				Protein.type).iterator();

		while (proteinIter.hasNext()) {
			Protein protein = (Protein) proteinIter.next();
			proteins.add(protein);
		}

		//
		// Initialize the classifiers
		//

		// trigger
		TriggerRecogniser triggerRecogniser = new TriggerRecogniser();
		triggerRecogniser.loadModel(new File(
				"./model/triggers.perceptron.model"));
		InstanceDictionary triggerDict = new InstanceDictionary();
		triggerDict.loadDictionary(new File("./model/triggers.dict"));

		// theme
		ThemeRecogniser themeRecogniser = new ThemeRecogniser();
		themeRecogniser.loadModel(new File("./model/themes.perceptron.model"));
		InstanceDictionary themeDict = new InstanceDictionary();
		themeDict.loadDictionary(new File("./model/themes.dict"));

		// cause
		CauseRecogniser causeRecogniser = new CauseRecogniser();
		causeRecogniser.loadModel(new File("./model/causes.perceptron.model"));
		InstanceDictionary causeDict = new InstanceDictionary();
		causeDict.loadDictionary(new File("./model/causes.dict"));

		// Initialize the iterator and counter
		FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
				Sentence.type).iterator();

		int eventIndex = 1;

		// It has to be LinkedList to avoid sorting.
		Queue<Event> newEvents = new LinkedBlockingQueue<Event>();

		while (sentenceIter.hasNext()) {

			//
			// trigger detection
			//
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentenceIter.next());
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					tokens);

			for (Token token : tokens) {

				Instance tokenInstance = tokenToInstance(token, null);
				double prediction = triggerRecogniser.predict(triggerDict
						.instanceToNumeric(tokenInstance));

				if (prediction != triggerDict.getLabelNumeric(String
						.valueOf(EventType.Non_trigger))) {

					Trigger trigger = new Trigger(jcas, token.getBegin(),
							token.getEnd());
					trigger.setEventType(triggerDict.getLabelString(prediction));
					triggers.add(trigger);
				}
			}

			//
			// theme assignment
			//

			// 1. iterate through all proteins
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentenceIter.next());

			for (Trigger trigger : triggers) {

				if (EventType.isSimpleEvent(trigger.getEventType())) {

					for (Protein protein : proteins) {

						Instance proteinInstance = themeToInstance(jcas,
								protein, trigger, dependencyExtractor, false);
						double prediction = themeRecogniser
								.predict(proteinInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.add(event);
							newEvents.add(event);
						}
					}

				} else if (EventType.isBindingEvent(trigger.getEventType())) {

					// Initialize event with 10 themes. It never exceed this
					// number in the training data. (I know, it is not elegant!)
					StringArray themes = new StringArray(jcas, 10);
					int themeIndex = 0;
					Event event = new Event(jcas);
					for (Protein protein : proteins) {

						Instance proteinInstance = themeToInstance(jcas,
								protein, trigger, dependencyExtractor, false);
						double prediction = themeRecogniser
								.predict(proteinInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							themes.set(themeIndex++, protein.getId());
						}

					}
					event.setThemes(themes);

					if (themeIndex > 0) {
						// Binding with several themes could be several events
						// or only one. E.g. binding, A, B could be
						// "BINDING Theme:A Theme:B" or "BINDING: Theme:A" and
						// "BINDING: Theme:B"
						// TODO
					}

					events.add(event);
					newEvents.add(event);

				} else if (EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Protein protein : proteins) {

						Instance proteinInstance = themeToInstance(jcas,
								protein, trigger, dependencyExtractor, false);
						double prediction = themeRecogniser
								.predict(proteinInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.add(event);
							newEvents.add(event);
						}
					}
				}
			}

			// 2. check all discovered events whether they can be themes
			for (Trigger trigger : triggers) {

				if (EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Event themeEvent : newEvents) {

						Instance triggerTokenInstance = tokenToInstance(
								getTriggerToken(jcas, themeEvent.getTrigger()),
								null);
						double prediction = themeRecogniser
								.predict(triggerTokenInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.add(event);
							newEvents.add(event);
						}

					}
				}
			}

			//
			// cause
			//
			for (Event event : events) {

				// protein
				for (Protein protein : proteins) {

					Instance proteinInstance = causeToInstance(jcas, protein,
							event, dependencyExtractor, false);
					double prediction = causeRecogniser
							.predict(proteinInstance);

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause(protein.getId());
					}
				}
				// event
				for (Event causeEvent : events) {

					Instance triggerTokenInstance = tokenToInstance(
							getTriggerToken(jcas, causeEvent.getTrigger()),
							null);
					double prediction = causeRecogniser
							.predict(triggerTokenInstance);

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause(causeEvent.getId());
					}
				}
			}
		}

	}

	public static void main(String[] args) {
		EventExtractor ee = new EventExtractor();
		ee.setTaeDescriptor(new File("./desc/TestSetAnnotator.xml"));

		ee.extract(new File(args[0]));

		for (Event event : ee.events) {

			if (!event.getTrigger().getEventType().equals("Non_trigger")) {
				System.out.println(event.getTrigger().getEventType()
						.concat("\t").concat(event.getThemes(0)));
			}
		}
	}
}
