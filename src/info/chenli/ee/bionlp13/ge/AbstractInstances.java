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
import org.apache.uima.resource.ResourceInitializationException;
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
	private File taeDescriptor;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected Instances instances;
	protected ArrayList<Attribute> attributes;
	protected Attribute classes;

	protected void setTaeDescriptor(File taeDescriptor) {

		this.taeDescriptor = taeDescriptor;

	}

	protected File getTaeDescriptor() {
		return taeDescriptor;
	};

	private AnalysisEngine ae = null;

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

		try {
			XMLInputSource in = new XMLInputSource(getTaeDescriptor());
			ResourceSpecifier specifier = UIMAFramework.getXMLParser()
					.parseResourceSpecifier(in);

			logger.info(specifier.getSourceUrlString());

			// create Analysis Engine
			ae = UIMAFramework.produceAnalysisEngine(specifier);

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		}
	}

	protected abstract void initAttributes();

	protected abstract Attribute getClasses();

	public void fetchInstances(File dataDir) {

		if (null == attributes || null == ae) {
			init();
		}

		if (dataDir.isFile()) {

			processSingleFile(dataDir, annotationType);

		} else {
			// get all files in the input directory
			File[] files = dataDir.listFiles();
			if (files == null) {

				logger.log(Level.WARNING, "Empty directory.");

				instances = null;

			} else {
				// process documents
				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {

						processSingleFile(files[i], annotationType);
					}
				}
			}
		}
		ae.destroy();

	}

	protected JCas processSingleFile(File aFile, int annotationType) {

		logger.log(Level.INFO, "Processing file " + aFile.getName());

		if (null == attributes || null == ae) {
			init();
		}

		String document = null;

		try {

			document = FileUtils.file2String(aFile);

		} catch (IOException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		}

		document = document.trim();

		try {
			// create a CAS
			CAS cas = ae.newCAS();

			// put document text in CAS
			cas.setDocumentText(document);

			// set the path of resource file
			cas.createView("FileName").setSofaDataURI(aFile.getName(), "text");

			// process
			ae.process(cas);

			FSIterator<Annotation> annoIter = null;
			JCas jcas = null;
			jcas = cas.getJCas();
			annoIter = jcas.getAnnotationIndex(annotationType).iterator();
			structuredInstances
					.addAll(fetchStructuredInstances(jcas, annoIter));

			return jcas;

		} catch (AnalysisEngineProcessException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		} catch (CASRuntimeException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		} catch (CASException e) {

			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);

		} catch (ResourceInitializationException e) {
			logger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		}

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
