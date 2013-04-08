package info.chenli.litway.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;

public class UimaUtil {

	private final static Logger logger = Logger.getLogger(UimaUtil.class
			.getName());

	public static String getJCasFileName(JCas jCas) {

		String filePath = getJCasFilePath(jCas);

		return filePath.substring(filePath.lastIndexOf(File.separator));
	}

	public static String getJCasFilePath(JCas jCas) {

		try {
			return jCas.getView("FilePath").getSofaDataURI();

		} catch (CASException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}

	public static AnalysisEngine getAnalysisEngine(File descriptor) {

		try {

			XMLInputSource in = new XMLInputSource(descriptor);
			ResourceSpecifier specifier = UIMAFramework.getXMLParser()
					.parseResourceSpecifier(in);

			logger.info(specifier.getSourceUrlString());

			// create Analysis Engine
			AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);

			return ae;
		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new RuntimeException(e);
		}

	}
}
