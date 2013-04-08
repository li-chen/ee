package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.uimafit.util.JCasUtil;

public abstract class AbstractInstances {

	private final static Logger logger = Logger
			.getLogger(AbstractInstances.class.getName());

	private String instancesName;
	private int annotationType;
	private String taeDescriptor;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected List<Instance> instances;
	protected List<String> labelsString;

	protected void setTaeDescriptor(String taeDescriptor) {

		this.taeDescriptor = taeDescriptor;

	}

	protected XMLInputSource getXMLInputSource() throws IOException,
			URISyntaxException {

		URL url = this.getClass().getResource(taeDescriptor);
		return new XMLInputSource(url);
	};

	private AnalysisEngine ae = null;

	public AbstractInstances(String instancesName, int annotationType) {

		this.instancesName = instancesName;
		this.annotationType = annotationType;

	}

	private void init() {

		labelsString = getLabelsString();

		instances = new ArrayList<Instance>();

		try {

			XMLInputSource in = getXMLInputSource();
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

	public List<Instance> getInstances(File dataDir) {

		if (null == ae) {
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

		if (null == ae) {
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
			cas.createView("FilePath").setSofaDataURI(aFile.getAbsolutePath(), "text");

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

	public void saveInstances(File file) {

		StringBuffer sb = new StringBuffer();

		for (Instance instance : instances) {

			sb.append(instance.getLabelString());

			for (String feature : instance.getFeaturesString()) {
				sb.append("\t".concat(feature));
			}

			sb.append("\n");
		}

		String instancesStr = sb.toString();
		FileUtil.saveFile(instancesStr, file);
	}

	public void saveNumericInstances(File file) {

		StringBuffer sb = new StringBuffer();

		for (Instance instance : instances) {

			sb.append(String.valueOf(instance.getLabel()));

			for (int feature : instance.getFeaturesNumeric()) {
				sb.append("\t".concat(String.valueOf(feature)));
			}

			sb.append("\n");
		}

		String instancesStr = sb.toString();
		FileUtil.saveFile(instancesStr, file);
	}

	/**
	 * Convert a protein or event into an theme instance ready for training or
	 * predicting.
	 * 
	 * @param jcas
	 * @param anno
	 *            It could be a protein or an event trigger.
	 * @param event
	 * @param dependencyExtractor
	 * @return
	 */
	protected Instance themeToInstance(JCas jcas, Annotation anno,
			Trigger trigger, DependencyExtractor dependencyExtractor,
			boolean isTheme) {

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, anno);

		// if protein is within a token
		if (tokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(Token.type)
					.iterator();
			tokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= anno.getBegin()
						&& token.getEnd() >= anno.getEnd()) {
					tokens.add(token);
					break;
				}
			}
		}

		String tokenLemma = "", tokenPos = "";
		String leftToken = null == tokens.get(0).getLeftToken() ? "" : tokens
				.get(0).getLeftToken().getCoveredText();
		String rightToken = null == tokens.get(tokens.size() - 1)
				.getRightToken() ? "" : tokens.get(tokens.size() - 1)
				.getRightToken().getCoveredText();

		// Take the last non-digital token if protein is
		// multi-token.
		Token annoToken = tokens.get(0);
		for (Token token : tokens) {

			try {
				Double.parseDouble(token.getLemma());
			} catch (NumberFormatException e) {
				annoToken = token;
			}

		}

		tokenLemma = annoToken.getLemma();

		tokenPos = annoToken.getPos();

		Instance instance = new Instance();
		List<String> featuresString = new ArrayList<String>();
		instance.setFeaturesString(featuresString);
		featuresString.add(anno.getCoveredText());
		featuresString.add(tokenLemma);
		featuresString.add(tokenPos);
		featuresString.add(leftToken);
		featuresString.add(rightToken);
		featuresString.add(trigger.getEventType());
		featuresString.add(trigger.getCoveredText());
		Token triggerToken = getTriggerToken(jcas, trigger);
		featuresString.add(triggerToken.getCoveredText());
		featuresString.add(triggerToken.getLemma());
		try {
			featuresString.add(dependencyExtractor.getDijkstraShortestPath(
					annoToken, triggerToken));
		} catch (IllegalArgumentException e) {
			logger.warning("NoPathToken:\t".concat(annoToken.getCoveredText())
					.concat("\t").concat(triggerToken.getCoveredText()));
		}

		if (isTheme) {

			instance.setLabelString("Theme");

		} else {
			instance.setLabelString("Non_theme");
		}

		return instance;
	}

	/**
	 * 
	 * @param jcas
	 * @param anno
	 * @param trigger
	 * @param themeId
	 * @param dependencyExtractor
	 * @param isCause
	 * @return
	 */
	protected Instance causeToInstance(JCas jcas, Annotation anno, Event event,
			DependencyExtractor dependencyExtractor, boolean isCause) {

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, anno);

		// if protein is within a token
		if (tokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(Token.type)
					.iterator();
			tokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= anno.getBegin()
						&& token.getEnd() >= anno.getEnd()) {
					tokens.add(token);
					break;
				}
			}
		}

		String tokenLemma = "", tokenPos = "";
		String leftToken = null == tokens.get(0).getLeftToken() ? "" : tokens
				.get(0).getLeftToken().getCoveredText();
		String rightToken = null == tokens.get(tokens.size() - 1)
				.getRightToken() ? "" : tokens.get(tokens.size() - 1)
				.getRightToken().getCoveredText();

		// Take the last non-digital token if protein is
		// multi-token.
		Token annoToken = tokens.get(0);
		for (Token token : tokens) {

			try {
				Double.parseDouble(token.getLemma());
			} catch (NumberFormatException e) {
				annoToken = token;
			}

		}

		tokenLemma = annoToken.getLemma();

		tokenPos = annoToken.getPos();

		Instance instance = new Instance();
		List<String> featuresString = new ArrayList<String>();
		instance.setFeaturesString(featuresString);

		featuresString.add(anno.getCoveredText());
		featuresString.add(tokenLemma);
		featuresString.add(tokenPos);
		featuresString.add(leftToken);
		featuresString.add(rightToken);
		featuresString.add(event.getTrigger().getEventType());
		featuresString.add(event.getTrigger().getCoveredText());
		Token triggerToken = getTriggerToken(jcas, event.getTrigger());
		featuresString.add(triggerToken.getCoveredText());
		featuresString.add(triggerToken.getLemma());
		featuresString.add(dependencyExtractor.getDijkstraShortestPath(
				annoToken, triggerToken));
		// a regulatory event only has one theme
		featuresString.add(event.getThemes().get(0));
		featuresString.add(triggerToken.getCoveredText());
		featuresString.add(triggerToken.getLemma());
		featuresString.add(dependencyExtractor.getDijkstraShortestPath(
				annoToken, triggerToken));

		// TODO consider more themes. e.g. themes in binding.
		if (isCause) {

			instance.setLabelString("Cause");

		} else
		// protein that is not theme
		{
			instance.setLabelString("Non_cause");
		}

		return instance;
	}
}
