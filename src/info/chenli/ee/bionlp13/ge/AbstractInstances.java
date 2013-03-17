package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.POS;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.searn.StructuredInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.XMLInputSource;

import weka.core.Attribute;
import weka.core.Instances;

public abstract class AbstractInstances {

	private final static Logger logger = Logger
			.getLogger(AbstractInstances.class.getName());

	private String instancesName;
	private int annotationType;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected Instances instances;
	protected ArrayList<Attribute> attributes;
	protected Attribute classes;

	public abstract File getTaeDescriptor();

	public AbstractInstances(String instancesName, int annotationType) {

		this.instancesName = instancesName;
		this.annotationType = annotationType;

	}

	private void init() {

		initAttributes();

		classes = getClasses();

		attributes.add(classes);

		instances = new Instances(instancesName, attributes, 0);

		instances.setClass(classes);

	}

	protected abstract void initAttributes();

	protected abstract Attribute getClasses();

	public void fetchInstances(File dataDir) {

		if (!dataDir.isDirectory()) {

			logger.log(Level.SEVERE,
					dataDir.getName().concat(" is not a directory."));
			throw new RuntimeException(dataDir.getName().concat(
					" is not a directory."));
		}

		if (null == attributes) {
			init();
		}

		try {

			XMLInputSource in = new XMLInputSource(getTaeDescriptor());
			ResourceSpecifier specifier = UIMAFramework.getXMLParser()
					.parseResourceSpecifier(in);

			logger.info(specifier.getSourceUrlString());

			// create Analysis Engine
			AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);

			// create a CAS
			CAS cas = ae.newCAS();

			// get all files in the input directory
			File[] files = dataDir.listFiles();
			if (files == null) {

				logger.log(Level.WARNING, "Empty directory.");

				instances = null;

			} else {
				// process documents
				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {

						processSingleFile(files[i], ae, cas, annotationType);
					}
				}
			}
			ae.destroy();

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		}

	}

	private void processSingleFile(File aFile, AnalysisEngine aAE, CAS aCAS,
			int annotationType) throws IOException,
			AnalysisEngineProcessException {

		logger.log(Level.INFO, "Processing file " + aFile.getName());

		String document = FileUtils.file2String(aFile);

		document = document.trim();

		// put document text in CAS
		aCAS.setDocumentText(document);

		// set the path of resource file
		aCAS.createView("FileName").setSofaDataURI(aFile.getName(), "text");

		// process
		aAE.process(aCAS);

		FSIterator<Annotation> annoIter = null;
		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
			annoIter = jcas.getAnnotationIndex(annotationType).iterator();
		} catch (CASRuntimeException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		} catch (CASException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		}

		structuredInstances.addAll(fetchStructuredInstances(jcas, annoIter));

		// reset the CAS to prepare it for processing the next document
		aCAS.reset();

	}

	protected abstract List<StructuredInstance> fetchStructuredInstances(
			JCas jcas, FSIterator<Annotation> annoIter);

	public List<StructuredInstance> getStructuredInstances() {

		return structuredInstances;
	}

	public Instances getInstances() {

		for (StructuredInstance si : structuredInstances) {
			instances.addAll(si.getNodes());
		}

		return instances;
	}

	/**
	 * if the trigger is multi-token, then it takes the order of noun,
	 * adjective, adverb and verb. It is based on the observation of training.
	 * See detail in {@link info.chenli.ee.bionlp13.POSPrioritizer}
	 * 
	 * TODO the multi-token policy can be put in a configuration file.
	 * Alternatively, it can be dynamically learned.
	 */
	protected static Token getTriggerToken(List<Token> tokens) {

		logger.info("Processing multi-token...");

		if (tokens.size() > 1) {

			TreeMap<POS, Token> sortedTokens = new TreeMap<POS, Token>(
					new POSPrioritizer());

			for (Token token : tokens) {

				sortedTokens.put(POS.valueOf(token.getPos()), token);
			}

			return sortedTokens.firstEntry().getValue();

		} else if (tokens.size() == 1) {

			return tokens.get(0);
		}

		return null;
	}
}
