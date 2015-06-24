package info.chenli.litway.bionlp13.pc;

import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Trigger;
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

/**
 * Read event from a2 file. It is used for fetching training instances.
 * 
 * @author Chen Li
 * 
 */
public class EventAnnotator extends JCasAnnotator_ImplBase {

	private final static Logger logger = Logger.getLogger(EventAnnotator.class
			.getName());

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(
					FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jCas)).concat(".a2"))));

			String line;
			Map<String, Trigger> triggerMap = new TreeMap<String, Trigger>();
			Map<String, Event> triggerEventMap = new TreeMap<String, Event>();

			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line);

				String id = st.nextToken();

				// trigger
				if (id.startsWith("T")) {

					EventType type = EventType.valueOf(st.nextToken());
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
					triggerEventMap.put(
							token.substring(token.indexOf(":") + 1), event);

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
				Event event = triggerEventMap.get(triggerId);
				event.setTrigger(trigger);
				event.setBegin(trigger.getBegin());
				event.setEnd(trigger.getEnd());
				event.addToIndexes();
			}

			br.close();

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}
}
