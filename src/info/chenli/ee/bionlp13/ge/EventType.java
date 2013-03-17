package info.chenli.ee.bionlp13.ge;

public enum EventType {

	Non_trigger, Gene_expression, Localization, Phosphorylation, Protein_catabolism, Transcription, Ubiquitination, Binding, Regulation, Positive_regulation, Negative_regulation;

	public static boolean contains(String eventTypeStr) {

		for (EventType et : EventType.values()) {
			if (et.name().equals(eventTypeStr)) {
				return true;
			}
		}

		return false;

	}
}
