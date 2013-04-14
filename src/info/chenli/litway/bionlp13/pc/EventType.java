package info.chenli.litway.bionlp13.pc;

import info.chenli.litway.bionlp13.EventTypes;

public enum EventType implements EventTypes {

	Non_trigger, Acetylation, Activation, Binding, Conversion, Deacetylation, Degradation, Demethylation, Dephosphorylation, Deubiquitination, Dissociation, Gene_expression, Hydroxylation, Inactivation, Localization, Methylation, Negative_regulation, Pathway, Phosphorylation, Positive_regulation, Regulation, Transcription, Translation, Transport, Ubiquitination;

	public static boolean isMultiThemeEvent(String eventType) {

		return isMultiThemeEvent(EventType.valueOf(eventType));
	}

	public static boolean isMultiThemeEvent(EventType eventType) {

		if (eventType == EventType.Gene_expression
				|| eventType == EventType.Transcription
				|| eventType == EventType.Localization) {
			return true;
		}

		return false;
	}

	public static boolean contains(String eventTypeStr) {

		for (EventType et : EventType.values()) {
			if (et.name().equals(eventTypeStr)) {
				return true;
			}
		}

		return false;

	}
}
