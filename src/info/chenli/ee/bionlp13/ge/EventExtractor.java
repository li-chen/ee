package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Event;
import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.StructuredInstance;
import info.chenli.ee.util.DependencyExtractor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import weka.core.Instance;

public class EventExtractor extends TokenInstances {

	private Map<Integer, Protein> proteins = null;
	private Map<Integer, Trigger> triggers = null;
	private Map<Integer, Event> events = null;

	/**
	 * Extract events from the given file.
	 * 
	 * @param file
	 */
	public void extract(File file) {

		// Initialise the file
		EventExtractor ee = new EventExtractor();
		JCas jcas = ee.processSingleFile(file, Token.type);

		// Initialise the classifiers
		TriggerRecogniser triggerRecogniser = new TriggerRecogniser();
		ThemeRecogniser themeRegconiser = new ThemeRecogniser();
		CauseRecogniser causeRegconiser = new CauseRecogniser();

		FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
				Sentence.type).iterator();

		int triggerIndex = proteins.size() + 1;
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

			for (Trigger trigger : triggers.values()) {

				if (EventType.isSimpleEvent(trigger.getEventType())) {

					for (Protein protein : proteins) {

						Instance proteinInstance = proteinToInstance(jcas,
								protein, trigger, null, dependencyExtractor);
						themeRegconiser.classify(proteinInstance);

						if (proteinInstance.classIndex() != classes
								.indexOfValue(String.valueOf("Theme"))) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.put(eventIndex++, event);
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

						Instance proteinInstance = proteinToInstance(jcas,
								protein, trigger, null, dependencyExtractor);
						themeRegconiser.classify(proteinInstance);

						if (proteinInstance.classIndex() != classes
								.indexOfValue(String.valueOf("Theme"))) {
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

					events.put(eventIndex++, event);
					newEvents.add(event);

				} else if (EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Protein protein : proteins) {

						Instance proteinInstance = proteinToInstance(jcas,
								protein, trigger, null, dependencyExtractor);
						themeRegconiser.classify(proteinInstance);

						if (proteinInstance.classIndex() != classes
								.indexOfValue(String.valueOf("Theme"))) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.put(eventIndex++, event);
							newEvents.add(event);
						}
					}
				}
			}

			// 2. check all discovered events whether they can be themes
			for (Trigger trigger : triggers.values()) {

				if (EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Event themeEvent : newEvents) {

						Instance triggerTokenInstance = tokenToInstance(
								getTriggerToken(jcas, themeEvent.getTrigger()),
								null);
						themeRegconiser.classify(triggerTokenInstance);

						if (triggerTokenInstance.classIndex() != classes
								.indexOfValue(String
										.valueOf(EventType.Non_trigger))) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.put(eventIndex++, event);
							newEvents.add(event);
						}

					}
				}
			}

			//
			// cause
			//
			for (Event event : events.values()) {

				// protein
				for (Protein protein : proteins) {
					
					Instance proteinInstance = proteinToInstance(jcas,
							protein, trigger, null, dependencyExtractor);
					themeRegconiser.classify(proteinInstance);

					if (proteinInstance.classIndex() != classes
							.indexOfValue(String.valueOf("Cause"))) {
						event.setCause(protein.getId());
				}
				// event
					for (Event causeEvent : events.values()) {
						
						Instance triggerInstance = triggerToInstance(jcas,
								protein, event, null, dependencyExtractor);
						themeRegconiser.classify(triggerInstance);

						if (triggerInstance.classIndex() != classes
								.indexOfValue(String.valueOf("Cause"))) {
							event.setCause(causeEvent.getId());
					}
			}
		}
	}

	@Override
	public File getTaeDescriptor() {

		return new File("./desc/TestSetAnnotator.xml");
	}
}
