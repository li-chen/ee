package info.chenli.litway.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLUtil {

	private final static Logger logger = Logger.getLogger(XMLUtil.class
			.getName());

	public static Document getDocument(File file) {

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			return docBuilder.parse(file);
		} catch (SAXException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
