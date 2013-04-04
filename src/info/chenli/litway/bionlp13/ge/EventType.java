package info.chenli.litway.bionlp13.ge;

public enum EventType {

	Non_trigger, Gene_expression, Transcription, Binding, Localization, Protein_catabolism, Protein_modification, Phosphorylation, Ubiquitination, Acetylation, Deacetylation, Regulation, Positive_regulation, Negative_regulation;

	public static boolean isSimpleEvent(String eventType) {

		return isSimpleEvent(EventType.valueOf(eventType));
	}

	public static boolean isSimpleEvent(EventType eventType) {

		if (eventType == EventType.Gene_expression
				|| eventType == EventType.Protein_catabolism
				|| eventType == EventType.Localization
				|| eventType == EventType.Transcription) {
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

	public static boolean isComplexEvent(String eventType) {

		return isComplexEvent(EventType.valueOf(eventType));
	}

	public static boolean isComplexEvent(EventType eventType) {

		if (eventType == EventType.Regulation
				|| eventType == EventType.Positive_regulation
				|| eventType == EventType.Negative_regulation
				|| eventType == EventType.Protein_modification
				|| eventType == EventType.Phosphorylation
				|| eventType == EventType.Ubiquitination
				|| eventType == EventType.Acetylation
				|| eventType == EventType.Deacetylation) {

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
