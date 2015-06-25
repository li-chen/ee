package info.chenli.litway.config;

import java.util.logging.Logger;

public enum EventTypes {

	INSTANCE;

	private final static Logger logger = Logger.getLogger(EventTypes.class
			.getName());

	private EventType[] eventTypes;

	public void init(EventType[] eventTypes) {

		if (null != this.eventTypes) {
			logger.warning("The event types have been initialised and can't be updated.");
			return;
		}

		this.eventTypes = eventTypes;
	}

	public EventType[] getEventTypes() {
		return this.eventTypes;
	}

	public static boolean isAnEventType(String eventType) {

		boolean isAnEventType = false;
		for (EventType et : INSTANCE.eventTypes) {
			if (et.getType().equals(eventType)) {
				isAnEventType = true;
				break;
			}
		}

		return isAnEventType;
	}

	public static boolean isSimpleEvent(String eventType) {

		for (EventType et : INSTANCE.eventTypes) {
			if (et.getType().equals(eventType)) {
				isAnEventType = true;
				break;
			}
		}

		return false;
	}
}
