package info.chenli.litway.bionlp13.ge;

import info.chenli.litway.bionlp13.EventTypes;

public enum EventType implements EventTypes {

	Non_trigger, Gene_expression, Transcription, Protein_catabolism, Binding, Localization, Protein_modification, Phosphorylation, Ubiquitination, Acetylation, Deacetylation, Regulation, Positive_regulation, Negative_regulation;

	public static boolean isSimpleEvent(String eventType) {

		return isSimpleEvent(EventType.valueOf(eventType));
	}

	public static boolean isSimpleEvent(EventType eventType) {

		if (eventType == EventType.Gene_expression
				|| eventType == EventType.Transcription
				|| eventType == EventType.Protein_catabolism
				|| eventType == EventType.Localization) {
			return true;
		}

		return false;
	}

	public static boolean isBindingEvent(String eventType) {

		return isBindingEvent(EventType.valueOf(eventType));
	}

	public static boolean isBindingEvent(EventType eventType) {

		if (eventType == EventType.Binding) {
			return true;
		}
		return false;
	}

	public static boolean isRegulatoryEvent(String eventType) {

		return isRegulatoryEvent(EventType.valueOf(eventType));
	}

	public static boolean isRegulatoryEvent(EventType eventType) {

		if (eventType == EventType.Regulation
				|| eventType == EventType.Positive_regulation
				|| eventType == EventType.Negative_regulation) {

			return true;
		}

		return false;
	}

	public static boolean isComplexEvent(String eventType) {

		return isComplexEvent(EventType.valueOf(eventType));
	}

	public static boolean isComplexEvent(EventType eventType) {

		if (eventType == EventType.Protein_modification
				|| eventType == EventType.Phosphorylation
				|| eventType == EventType.Ubiquitination
				|| eventType == EventType.Acetylation
				|| eventType == EventType.Deacetylation
				|| eventType == EventType.Regulation
				|| eventType == EventType.Positive_regulation
				|| eventType == EventType.Negative_regulation) {

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
