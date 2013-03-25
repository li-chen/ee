package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.ee.corpora.POS;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.StructuredInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.uimafit.util.JCasUtil;

public abstract class AbstractInstances {

	private final static Logger logger = Logger
			.getLogger(AbstractInstances.class.getName());

	private String instancesName;
	private int annotationType;
	private File taeDescriptor;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected List<Instance> instances;
	protected List<String> featuresString;
	protected List<String> labelsString;

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

		featuresString = getFeaturesString();

		labelsString = getLabelsString();

		instances = new ArrayList<Instance>();

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

	protected abstract List<String> getLabelsString();

	protected abstract List<String> getFeaturesString();

	public List<Instance> getInstances(File dataDir) {

		if (null == featuresString || null == ae) {
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

		return getInstances();
	}

	protected JCas processSingleFile(File aFile, int annotationType) {

		logger.log(Level.INFO, "Processing file " + aFile.getName());

		if (null == featuresString || null == ae) {
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
			structuredInstances.addAll(getStructuredInstances(jcas, annoIter));

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

	protected abstract List<StructuredInstance> getStructuredInstances(
			JCas jcas, FSIterator<Annotation> annoIter);

	public List<StructuredInstance> getStructuredInstances() {

		return structuredInstances;
	}

	public List<Instance> getInstances() {

		for (StructuredInstance si : structuredInstances) {
			instances.addAll(si.getNodes());
		}

		instancesToNumerical();

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
	protected Token getTriggerToken(List<Token> tokens) {

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

	protected Token getTriggerToken(JCas jcas, Trigger trigger) {
		// get tokens
		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, trigger);

		if (tokens.size() == 0)
		// if trigger is within a token, then take
		// the nesting token. It
		// happens, e.g. in PMC-2065877-01-Introduction.
		{
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(Token.type)
					.iterator();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= trigger.getBegin()
						&& token.getEnd() >= trigger.getEnd()) {
					return token;
				}
			}

		} else
		// take one of the nested tokens.
		{
			return getTriggerToken(tokens);
		}
		return null;
	}

	private void instancesToNumerical() {

		List<List<String>> features = new ArrayList<List<String>>();

		int featureNum = instances.get(0).getFeaturesString().size();
		while (featureNum-- > 0) {
			features.add(new ArrayList<String>());
		}

		List<String> labels = new ArrayList<String>();
		for (Instance instance : instances) {

			if (!labels.contains(instance.getLabelString())) {
				labels.add(instance.getLabelString());
			}

			instance.setLabel(labels.indexOf(instance.getLabelString()));

			Iterator<List<String>> featuresIter = features.iterator();
			Iterator<String> featureStrIter = instance.getFeaturesString()
					.iterator();

			List<Double> featuresNumeric = new ArrayList<Double>();

			while (featuresIter.hasNext()) {

				List<String> feature = featuresIter.next();
				String featureStr = featureStrIter.next();

				if (!feature.contains(featureStr)) {
					feature.add(featureStr);
				}

				featuresNumeric.add((double) feature.indexOf(featureStr));
			}

			instance.setFeatures(featuresNumeric);

		}
	}
}
