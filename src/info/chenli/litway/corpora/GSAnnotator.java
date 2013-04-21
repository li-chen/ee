package info.chenli.litway.corpora;

import info.chenli.litway.bionlp13.ge.EventType;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;

public class GSAnnotator extends JCasAnnotator_ImplBase {

	private final static Logger logger = Logger.getLogger(GSAnnotator.class
			.getName());

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(
					FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jCas)).concat(".a2"))));

			String line;
			Map<String, Trigger> triggerMap = new TreeMap<String, Trigger>();
			Map<String, List<Event>> triggerEventMap = new TreeMap<String, List<Event>>();

			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line);

				String id = st.nextToken();

				// trigger
				if (id.startsWith("T")) {

					String eventType = st.nextToken();
					// if it is site (site is ignored at the moment)
					if (!EventType.contains(eventType)) {
						continue;
					}
					EventType type = EventType.valueOf(eventType);
					int start = Integer.parseInt(st.nextToken());
					int end = Integer.parseInt(st.nextToken());
					Trigger trigger = new Trigger(jCas, start, end);
					trigger.setId(id);
					trigger.setEventType(String.valueOf(type));
					trigger.addToIndexes();
					triggerMap.put(id, trigger);

				} else if (id.startsWith("E")) {

					String token = st.nextToken();
					EventType type = EventType.valueOf(token.substring(0,
							token.indexOf(":")));
					Event event = new Event(jCas);
					event.setId(id);
					String triggerId = token.substring(token.indexOf(":") + 1);
					if (triggerEventMap.containsKey(triggerId)) {
						triggerEventMap.get(triggerId).add(event);
					} else {
						List<Event> events = new ArrayList<Event>();
						events.add(event);
						triggerEventMap.put(triggerId, events);
					}

					List<String> themes = new ArrayList<String>();
					while (st.hasMoreTokens()) {
						token = st.nextToken();

						if (token.startsWith("Theme")) {
							themes.add(token.substring(token.indexOf(":") + 1));
						} else if (token.startsWith("Cause")) {
							event.setCause(token.substring(token.indexOf(":") + 1));
						} else if (token.startsWith("Product")) {
							event.setProduct(token.substring(token.indexOf(":") + 1));
						}
					}
					if (themes.size() > 0) {
						event.setThemes(new StringArray(jCas, themes.size()));
						int i = 0;
						for (String theme : themes) {
							event.setThemes(i++, theme);
						}
					}
				}
			}

			for (String triggerId : triggerEventMap.keySet()) {
				Trigger trigger = triggerMap.get(triggerId);
				for (Event event : triggerEventMap.get(triggerId)) {

					event.setTrigger(trigger);
					event.setBegin(trigger.getBegin());
					event.setEnd(trigger.getEnd());
					event.addToIndexes();
				}
			}

			br.close();

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

		// GsonDocument document = GsonFacade.instance
		// .getDocument(new File(FileUtil.removeFileNameExtension(
		// UimaUtil.getJCasFilePath(jCas)).concat(".json")));
		//
		// EntityAnnotation[] entityAnnotations = document.getCatanns();
		// if (null == entityAnnotations || entityAnnotations.length == 0) {
		// return;
		// }
		//
		// //
		// // mark gold standard triggers
		// //
		// Map<String, Trigger> triggerMap = new HashMap<String, Trigger>();
		// for (EntityAnnotation entity : entityAnnotations) {
		// if (EventType.contains(entity.getCategory())) {
		//
		// Trigger trigger = new Trigger(jCas,
		// entity.getSpan().getBegin(), entity.getSpan().getEnd());
		// trigger.setId(entity.getId());
		// trigger.setEventType(entity.getCategory());
		// trigger.addToIndexes();
		// triggerMap.put(trigger.getId(), trigger);
		//
		// }
		// } // Anaphora is ignored at the moment
		//
		// //
		// // extract gold standard events
		// //
		// RelationAnnotation[] relationAnnotations = document.getRelanns();
		// if (null == relationAnnotations || relationAnnotations.length == 0) {
		// return;
		// }
		//
		// // instanceOf, which carries trigger information
		// InstanceOfAnnotation[] instanceOfAnnotations = document.getInsanns();
		// if (null == instanceOfAnnotations || instanceOfAnnotations.length ==
		// 0) {
		// return;
		// }
		// // event id, trigger id
		// Map<String, String> eventTriggerMap = new HashMap<String, String>();
		// for (InstanceOfAnnotation instanceOf : instanceOfAnnotations) {
		//
		// eventTriggerMap.put(instanceOf.getId(), instanceOf.getObject());
		// }
		//
		// // event id, theme list
		// Map<String, List<String>> eventThemeMap = new HashMap<String,
		// List<String>>();
		// // event id, cause list
		// Map<String, String> eventCauseMap = new HashMap<String, String>();
		//
		// for (RelationAnnotation relation : relationAnnotations) {
		//
		// if (relation.getType().equals("themeOf")) {
		//
		// if (eventThemeMap.containsKey(relation.getObject())) {
		//
		// eventThemeMap.get(relation.getObject()).add(
		// relation.getSubject());
		//
		// } else {
		//
		// List<String> themes = new ArrayList<String>();
		// themes.add(relation.getSubject());
		// eventThemeMap.put(relation.getObject(), themes);
		// }
		//
		// } else if (relation.getType().equals("causeOf")) {
		//
		// if (eventCauseMap.containsKey(relation.getObject())) {
		//
		// eventCauseMap.put(relation.getObject(),
		// relation.getSubject());
		//
		// } else {
		//
		// eventCauseMap.put(relation.getObject(),
		// relation.getSubject());
		// }
		//
		// }
		// }
		//
		// // an event must have at least one theme, but may not have cause.
		// When
		// // it has cause, it will only have one cause.
		// for (String eventId : eventThemeMap.keySet()) {
		//
		// Trigger trigger = triggerMap.get(eventTriggerMap.get(eventId));
		// if (trigger == null) {
		// System.out.println(eventId + document.getSource_id()
		// + document.getSection() + document.getDivistion_id());
		// System.out.println(UimaUtil.getJCasFileName(jCas));
		// }
		// Event event = new Event(jCas, trigger.getBegin(), trigger.getEnd());
		// event.setId(eventId);
		//
		// // trigger
		// event.setTrigger(trigger);
		//
		// // themes
		// StringArray themes = new StringArray(jCas, eventThemeMap.get(
		// eventId).size());
		//
		// int i = 0;
		// for (String themeId : eventThemeMap.get(eventId)) {
		//
		// themes.set(i++, themeId);
		//
		// }
		// event.setThemes(themes);
		//
		// // causes
		// if (null != eventCauseMap.get(eventId)
		// && !eventCauseMap.get(eventId).equals("")) {
		//
		// // In GE task, each event only has one cause, but it is kept
		// // like this for further flexibility.
		// event.setCause(eventCauseMap.get(eventId));
		// }
		//
		// event.addToIndexes();
		// }
	}
}
