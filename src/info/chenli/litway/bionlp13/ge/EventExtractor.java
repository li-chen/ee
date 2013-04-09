package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileFilterImpl;
import info.chenli.litway.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class EventExtractor extends TokenInstances {

	private final static Logger logger = Logger.getLogger(EventExtractor.class
			.getName());

	public void train(File dir) {

		if (!dir.isDirectory()) {
			logger.info(dir.getAbsolutePath().concat(" is not a directory."));
		}

		//
		// train trigger
		//

		//
		// train theme
		//

		//
		// train cause
		//
	}

	public void extract(File file) {

		if (file.isDirectory()) {

			for (File f : file.listFiles(new FileFilterImpl(".txt"))) {
				extract(f);
			}

		} else if (file.isFile()) {

			logger.info("Extracting from ".concat(file.getName()));
			String newFileName = "./result/".concat(
					file.getName()
							.substring(0, file.getName().lastIndexOf(".")))
					.concat(".a2");
			FileUtil.saveFile(extractFromSingleFile(file),
					new File(newFileName));
			logger.info("Result saved in ".concat(newFileName));
		}
	}

	/**
	 * Extract events from the given file.
	 * 
	 * @param file
	 */
	public String extractFromSingleFile(File file) {

		Map<Integer, List<Trigger>> triggers = new TreeMap<Integer, List<Trigger>>();
		Map<Integer, List<Event>> events = new TreeMap<Integer, List<Event>>();
		// Initialize the file
		JCas jcas = this.processSingleFile(file, Token.type);
		int proteinNum = jcas.getAnnotationIndex(Protein.type).size();
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

		while (sentenceIter.hasNext()) {

			Sentence sentence = (Sentence) sentenceIter.next();
			// The queue where newly generated events are put
			Queue<Event> newEvents = new LinkedBlockingQueue<Event>();

			// protein
			List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			//
			// trigger detection
			//
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentence);
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					tokens);

			if (null == triggers.get(sentence.getId())) {
				triggers.put(sentence.getId(), new ArrayList<Trigger>());
			}

			for (Token token : tokens) {

				Instance tokenInstance = tokenToInstance(token, null);
				// set the token filter here
				// if (!TriggerRecogniser.isConsidered(tokenInstance
				// .getFeaturesString().get(2))) {
				// continue;
				// }
				int prediction = triggerRecogniser.predict(triggerDict
						.instanceToNumeric(tokenInstance).getFeaturesNumeric());

				if (prediction != triggerDict.getLabelNumeric(String
						.valueOf(EventType.Non_trigger))) {

					Trigger trigger = new Trigger(jcas, token.getBegin(),
							token.getEnd());
					trigger.setEventType(triggerDict.getLabelString(prediction));
					trigger.setId("T".concat(String.valueOf(++proteinNum)));
					triggers.get(sentence.getId()).add(trigger);
				}
			}

			//
			// theme assignment
			//

			// 1. iterate through all proteins

			if (null == events.get(sentence.getId())) {
				events.put(sentence.getId(), new ArrayList<Event>());
			}

			for (Trigger trigger : triggers.get(sentence.getId())) {

				if (EventType.isSimpleEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								protein, trigger, dependencyExtractor, false);
						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents.add(event);
						}
					}

				} else if (EventType.isBindingEvent(trigger.getEventType())) {

					// Initialize event with 10 themes. It never exceed this
					// number in the training data. (I know, it is not elegant!)
					List<String> themes = new ArrayList<String>();

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								protein, trigger, dependencyExtractor, false);
						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							themes.add(protein.getId());
						}

					}
					if (themes.size() > 0) {
						// Binding with several themes could be several events
						// or only one. E.g. binding, A, B could be
						// "BINDING Theme:A Theme:B" or "BINDING: Theme:A" and
						// "BINDING: Theme:B"
						// TODO
					}

					if (themes.size() > 0) {

						Event event = new Event(jcas);
						event.setTrigger(trigger);
						event.setId(String.valueOf(eventIndex++));

						StringArray eventThemes = new StringArray(jcas,
								themes.size());

						for (String theme : themes) {
							eventThemes.set(themes.indexOf(theme), theme);
						}
						event.setThemes(eventThemes);

						events.get(sentence.getId()).add(event);
						newEvents.add(event);
					}

				} else if (EventType.isComplexEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeDict
								.instanceToNumeric(themeToInstance(jcas,
										protein, trigger, dependencyExtractor,
										false));

						double prediction = themeRecogniser
								.predict(proteinInstance.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							// TODO can a protein be a theme of multi-event?
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents.add(event);
						}
					}
				}
			}

			// 2. check all discovered events whether they can be themes
			for (Trigger trigger : triggers.get(sentence.getId())) {

				if (EventType.isComplexEvent(trigger.getEventType())) {

					for (Event themeEvent : newEvents) {

						if (themeEvent.getTrigger().getBegin() == trigger
								.getBegin()) {
							continue;
						}

						Instance triggerTokenInstance = themeToInstance(jcas,
								getTriggerToken(jcas, themeEvent.getTrigger()),
								trigger, dependencyExtractor, false);

						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(triggerTokenInstance)
								.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							// newEvents.add(event);
						}

					}
				}
			}

			//
			// cause
			//
			for (Event event : events.get(sentence.getId())) {

				if (!EventType
						.isComplexEvent(event.getTrigger().getEventType())) {
					continue;
				}

				// protein
				for (Protein protein : sentenceProteins) {

					Instance proteinInstance = causeToInstance(jcas, protein,
							event, dependencyExtractor, false);
					double prediction = causeRecogniser.predict(causeDict
							.instanceToNumeric(proteinInstance)
							.getFeaturesNumeric());

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause(protein.getId());
					}
				}

				if (!EventType.isRegulatoryEvent(event.getTrigger()
						.getEventType())) {
					continue;
				}

				// event
				for (Event causeEvent : events.get(sentence.getId())) {

					if (causeEvent.getTrigger().getBegin() == event
							.getTrigger().getBegin()) {
						continue;
					}

					Instance triggerTokenInstance = tokenToInstance(
							getTriggerToken(jcas, causeEvent.getTrigger()),
							null);
					double prediction = causeRecogniser.predict(causeDict
							.instanceToNumeric(triggerTokenInstance)
							.getFeaturesNumeric());

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause("E".concat(causeEvent.getId()));
					}
				}
			}
		}

		return resultToString(triggers, events);
	}

	private String resultToString(Map<Integer, List<Trigger>> triggers,
			Map<Integer, List<Event>> events) {

		StringBuffer sb = new StringBuffer();

		// print triggers
		for (List<Trigger> sentenceTriggers : triggers.values()) {
			for (Trigger trigger : sentenceTriggers) {
				sb.append(trigger.getId().concat("\t")
						.concat(trigger.getEventType()).concat(" ")
						.concat(String.valueOf(trigger.getBegin())).concat(" ")
						.concat(String.valueOf(trigger.getEnd())).concat("\t")
						.concat(trigger.getCoveredText()).concat("\n"));
			}
		}

		for (List<Event> sentenceEvents : events.values()) {
			for (Event event : sentenceEvents) {
				sb.append("E").append(event.getId()).append("\t")
						.append(event.getTrigger().getEventType()).append(":")
						.append(event.getTrigger().getId());

				for (int j = 0; j < event.getThemes().size(); j++) {
					sb.append(" Theme").append(j == 0 ? "" : j + 1).append(":")
							.append(event.getThemes(j));
				}

				if (null != event.getCause() && "" != event.getCause()) {
					sb.append(" Cause:").append(event.getCause());
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) {

		EventExtractor ee = new EventExtractor();
		ee.setTaeDescriptor("/desc/TestSetAnnotator.xml");

		File inputFile = new File(args[0]);
		ee.extract(inputFile);

	}
}
