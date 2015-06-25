package info.chenli.litway.config;

import info.chenli.litway.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration {

	private Set<String> entityTypes = new HashSet<String>();
	private Set<String> argTypes = new HashSet<String>();
	private Set<EventType> eventTypes = new HashSet<EventType>();

	public Configuration(String file) {
		this(new File(file));
	}

	public Configuration(File file) {

		Document document = XMLUtil.getDocument(file);

		//
		// Entity types
		//
		NodeList entities = document.getElementsByTagName("entity");

		for (int i = 0; i < entities.getLength(); i++) {
			Node entityType = entities.item(i);
			if (entityType.getNodeType() == Node.ELEMENT_NODE) {

				entityTypes.add(entityType.getNodeValue());
			}
		}

		//
		// Event types
		//
		NodeList events = document.getElementsByTagName("event");

		for (int i = 0; i < events.getLength(); i++) {

			Node event = events.item(i);
			EventType et = new EventType();

			NodeList eventNodes = event.getChildNodes();
			for (int j = 0; j < eventNodes.getLength(); j++) {

				Node eventNode = eventNodes.item(j);

				if (eventNode.getNodeType() == Node.ELEMENT_NODE
						&& eventNode.getNodeName().equals("type")) {
					et.setType(eventNode.getNodeValue());
					if (!eventTypes.contains(et)) {
						eventTypes.add(et);
					}
				}

				if (eventNode.getNodeType() == Node.ELEMENT_NODE
						&& eventNode.getNodeName().equals("arguments")) {

					Map<String, List<Argument>> argumentsMap = new HashMap<String, List<Argument>>();
					NodeList arguments = eventNode.getChildNodes();
					for (int k = 0; k < arguments.getLength(); k++) {
						Node argument = arguments.item(k);
						if (argument.getNodeType() == Node.ELEMENT_NODE) {
							String argumentName = argument.getNodeName();
							if (!argTypes.contains(argumentName)) {
								argTypes.add(argumentName);
							}
							List<Argument> valueTypeList = new LinkedList<Argument>();
							argumentsMap.put(argumentName, valueTypeList);
							NodeList valueTypes = argument.getChildNodes();
							for (int l = 0; l < valueTypes.getLength(); l++) {
								Node valueType = valueTypes.item(l);
								if (valueType.getNodeType() == Node.ELEMENT_NODE) {
									valueTypeList.add(valueType.getNodeValue());
								}
							}
						}
					}

					et.setArguments(argumentsMap);
				}
			}

		}
	}

	public List<String> getEntityTypes() {
		return entityTypes;
	}

	public List<String> getArgTypes() {
		return argTypes;
	}

	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public List<String> getEventTypeNames() {

		List<String> eventTypeNames = new LinkedList<String>();
		for (EventType et : eventTypes) {
			eventTypeNames.add(et.getType());
		}

		return eventTypeNames;
	}

	public void validate() {
		throw new UnsupportedOperationException("Unimplemented method.");
	}

	public static void main(String[] args) {
		new Configuration("../data/ge/config.xml");
	}
}
