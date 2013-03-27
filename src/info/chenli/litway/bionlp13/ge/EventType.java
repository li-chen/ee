package info.chenli.litway.bionlp13.ge;

public enum EventType {

	Non_trigger, Gene_expression, Localization, Phosphorylation, Protein_catabolism, Transcription, Ubiquitination, Binding, Regulation, Positive_regulation, Negative_regulation;

	public static boolean isSimpleEvent(String eventType) {

		return isSimpleEvent(EventType.valueOf(eventType));
	}

	public static boolean isSimpleEvent(EventType eventType) {

		if (eventType == EventType.Gene_expression
				|| eventType == EventType.Localization
				|| eventType == EventType.Phosphorylation
				|| eventType == EventType.Protein_catabolism
				|| eventType == EventType.Transcription
				|| eventType == EventType.Ubiquitination) {
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

	public static boolean contains(String eventTypeStr) {

		for (EventType et : EventType.values()) {
			if (et.name().equals(eventTypeStr)) {
				return true;
			}
		}

		return false;

	}
}
