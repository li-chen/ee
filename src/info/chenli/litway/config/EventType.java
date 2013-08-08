package info.chenli.litway.config;

import java.util.List;
import java.util.Map;

public class EventType implements Argument {

	private String type;
	private Map<String, List<Argument>> arguments;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, List<Argument>> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, List<Argument>> arguments) {
		this.arguments = arguments;
	}

	@Override
	public boolean isEvent() {

		return true;
	}

}
