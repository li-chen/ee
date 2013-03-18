package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Trigger;

import java.io.File;
import java.util.Map;

public class EventExtractor {

	Map<Integer, Protein> proteins;
	Map<Integer, Trigger> triggers;
	Map<Integer, String> events;

	void extract() {

		// get triggers
		triggers = new TriggerRecogniser().getTriggers(new File(""), new File(
				""));

		// assign entity as theme for the simple events
		for (Trigger trigger : triggers.values()) {

		}

		// assign entity as theme for the complex events

		// assign event as theme for the complex events
	}
}
