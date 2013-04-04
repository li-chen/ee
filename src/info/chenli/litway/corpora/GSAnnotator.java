package info.chenli.litway.corpora;

import info.chenli.litway.bionlp13.ge.EventType;
import info.chenli.litway.gson.EntityAnnotation;
import info.chenli.litway.gson.GsonDocument;
import info.chenli.litway.gson.GsonFacade;
import info.chenli.litway.gson.InstanceOfAnnotation;
import info.chenli.litway.gson.RelationAnnotation;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;

public class GSAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		GsonDocument document = GsonFacade.instance.getDocument(new File(
				"./resources/training/ge/".concat(
						FileUtil.removeFileNameExtension(UimaUtil
								.getJCasFileName(jCas))).concat(".json")));

		EntityAnnotation[] entityAnnotations = document.getCatanns();
		if (null == entityAnnotations || entityAnnotations.length == 0) {
			return;
		}

		//
		// mark gold standard triggers
		//
		Map<String, Trigger> triggerMap = new HashMap<String, Trigger>();
		for (EntityAnnotation entity : entityAnnotations) {
			if (EventType.contains(entity.getCategory())) {

				Trigger trigger = new Trigger(jCas,
						entity.getSpan().getBegin(), entity.getSpan().getEnd());
				trigger.setId(entity.getId());
				trigger.setEventType(entity.getCategory());
				trigger.addToIndexes();
				triggerMap.put(trigger.getId(), trigger);

			}
		} // Anaphora is ignored at the moment

		//
		// extract gold standard events
		//
		RelationAnnotation[] relationAnnotations = document.getRelanns();
		if (null == relationAnnotations || relationAnnotations.length == 0) {
			return;
		}

		// instanceOf, which carries trigger information
		InstanceOfAnnotation[] instanceOfAnnotations = document.getInsanns();
		if (null == instanceOfAnnotations || instanceOfAnnotations.length == 0) {
			return;
		}
		// event id, trigger id
		Map<String, String> eventTriggerMap = new HashMap<String, String>();
		for (InstanceOfAnnotation instanceOf : instanceOfAnnotations) {

			eventTriggerMap.put(instanceOf.getId(), instanceOf.getObject());
		}

		// event id, theme list
		Map<String, List<String>> eventThemeMap = new HashMap<String, List<String>>();
		// event id, cause list
		Map<String, List<String>> eventCauseMap = new HashMap<String, List<String>>();

		for (RelationAnnotation relation : relationAnnotations) {

			if (relation.getType().equals("themeOf")) {

				if (eventThemeMap.containsKey(relation.getObject())) {

					eventThemeMap.get(relation.getObject()).add(
							relation.getSubject());

				} else {

					List<String> themes = new ArrayList<String>();
					themes.add(relation.getSubject());
					eventThemeMap.put(relation.getObject(), themes);
				}

			} else if (relation.getType().equals("causeOf")) {

				if (eventCauseMap.containsKey(relation.getObject())) {

					eventCauseMap.get(relation.getObject()).add(
							relation.getSubject());

				} else {

					List<String> causes = new ArrayList<String>();
					causes.add(relation.getSubject());
					eventCauseMap.put(relation.getObject(), causes);
				}

			}
		}

		// an event must have at least one theme, but may not have cause. When
		// it has cause, it will only have one cause.
		for (String eventId : eventThemeMap.keySet()) {

			Trigger trigger = triggerMap.get(eventTriggerMap.get(eventId));
			if (trigger == null) {
				System.out.println(eventId + document.getSource_id()
						+ document.getSection() + document.getDivistion_id());
				System.out.println(UimaUtil.getJCasFileName(jCas));
			}
			Event event = new Event(jCas, trigger.getBegin(), trigger.getEnd());
			event.setId(eventId);

			// trigger
			event.setTrigger(trigger);

			// themes
			StringArray themes = new StringArray(jCas, eventThemeMap.get(
					eventId).size());

			int i = 0;
			for (String themeId : eventThemeMap.get(eventId)) {

				themes.set(i++, themeId);

			}
			event.setThemes(themes);

			// causes
			if (null != eventCauseMap.get(eventId)
					&& eventCauseMap.get(eventId).size() > 0) {
				StringArray causes = new StringArray(jCas, eventCauseMap.get(
						eventId).size());

				i = 0;
				for (String causeId : eventCauseMap.get(eventId)) {

					causes.set(i++, causeId);

				}
				// In GE task, each event only has one cause, but it is kept
				// like this for further flexibility.
				event.setCause(causes.get(0));
			}

			event.addToIndexes();
		}
	}
}
