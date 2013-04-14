package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.StopWords;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Prefix;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Suffix;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileFilterImpl;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.apache.uima.cas.text.AnnotationIndex;
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
	private int[] annotationTypes; // the annotation types need consideration
	private String taeDescriptor;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected List<Instance> instances = null;
	protected List<String> labelsString;

	public static final String aStopWord = "!AStopWord!";

	protected void setTaeDescriptor(String taeDescriptor) {

		this.taeDescriptor = taeDescriptor;

	}

	protected XMLInputSource getXMLInputSource() throws IOException,
			URISyntaxException {

		URL url = this.getClass().getResource(taeDescriptor);
		return new XMLInputSource(url);
	};

	private AnalysisEngine ae = null;

	public AbstractInstances(String instancesName, int[] annotationTypes) {

		this.instancesName = instancesName;
		this.annotationTypes = annotationTypes;

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

			processSingleFile(dataDir, annotationTypes);

		} else {
			// get all files in the input directory
			File[] files = dataDir.listFiles(new FileFilterImpl(".txt"));
			if (files == null) {

				logger.log(Level.WARNING, "Empty directory.");

				instances = null;

			} else {
				// process documents
				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {

						processSingleFile(files[i], annotationTypes);
					}
				}
			}
		}
		ae.destroy();

		return getInstances();
	}

	protected JCas processSingleFile(File aFile, int[] annotationTypes) {

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
			cas.createView("FilePath").setSofaDataURI(aFile.getAbsolutePath(),
					"text");

			// process
			ae.process(cas);

			FSIterator<Annotation> annoIter = null;
			JCas jcas = null;
			jcas = cas.getJCas();
			for (int annotationType : annotationTypes) {
				annoIter = jcas.getAnnotationIndex(annotationType).iterator();
				structuredInstances.addAll(getStructuredInstances(jcas,
						annoIter));
			}

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

		if (instances.size() == 0) {
			for (StructuredInstance si : structuredInstances) {
				instances.addAll(si.getNodes());
			}
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
				if (TriggerWord.isATriggerWord(token.getCoveredText()) != null) {
					return token;
				}
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

			for (String[] features : instance.getFeaturesString()) {
				for (String feature : features) {
					sb.append("\t".concat(feature));
				}
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

	public void saveSvmLightInstances(File file) {

		StringBuffer sb = new StringBuffer();

		for (Instance instance : instances) {

			sb.append(String.valueOf(instance.getLabel()));

			int previousIndex = 0;
			for (int feature : instance.getFeaturesNumeric()) {
				if (feature + 1 > previousIndex) {
					sb.append(" ".concat(String.valueOf(feature + 1)).concat(
							":1"));
				}
				previousIndex = feature + 1;
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
			Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTheme) {

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, anno);

		// if protein/trigger is within a token
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

		Token token = null;
		if (anno instanceof Protein)
		// Take the last non-digital token if protein is
		// multi-token.
		{
			token = tokens.get(0);
			for (Token aToken : tokens) {

				try {
					Double.parseDouble(aToken.getLemma());
				} catch (NumberFormatException e) {
					token = aToken;
				}

			}
		} else if (anno instanceof Trigger) {
			token = getTriggerToken(jcas, (Trigger) anno);
		}

		String tokenLemma = token.getLemma().toLowerCase();
		String tokenPos = token.getPos();

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);

		// parser : dependency path between trigger-argument
		String dependencyPath = dependencyExtractor.getShortestPath(token,
				triggerToken);
		String featurePath = dependencyPath;

		if (null == dependencyPath) {
			featurePath = dependencyExtractor.getReversedShortestPath(token,
					triggerToken);
		}
		featuresString.add(new String[] { featurePath });

		// parser refined?

		// parser_simple: grouping of dpendency type;
		// amod, nn --> nmod
		// anything ending in subj --> subj
		// anything ending in subjpass --> subjpass
		String simplifiedFeaturePath = null;
		if (null != dependencyPath) {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedShortestPath(token, triggerToken);
		} else {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedReversedShortestPath(token, triggerToken);
		}
		featuresString.add(new String[] { simplifiedFeaturePath });

		// trigger class
		String triggerClassString;
		if (EventType.isSimpleEvent(trigger.getEventType())) {
			triggerClassString = "Simple";
		} else if (EventType.isBindingEvent(trigger.getEventType())) {
			triggerClassString = "Binding";
		} else if (EventType.isRegulatoryEvent(trigger.getEventType())) {
			triggerClassString = "Regulation";
		} else {
			triggerClassString = "Complex";
		}
		featuresString.add(new String[] { triggerClassString
				.concat(null == featurePath ? "" : "_".concat(featurePath)) });
		featuresString.add(new String[] { triggerClassString
				.concat(null == simplifiedFeaturePath ? "" : "_"
						.concat(simplifiedFeaturePath)) });

		// trigger token & trigger type
		featuresString.add(new String[] { token.getCoveredText().toLowerCase()
				.concat(null == featurePath ? "" : "_".concat(featurePath)) });
		featuresString.add(new String[] { trigger.getEventType().concat(
				null == featurePath ? "" : "_".concat(featurePath)) });

		featuresString.add(new String[] { token
				.getCoveredText()
				.toLowerCase()
				.concat(null == simplifiedFeaturePath ? "" : "_"
						.concat(simplifiedFeaturePath)) });
		featuresString.add(new String[] { trigger.getEventType().concat(
				null == simplifiedFeaturePath ? "" : "_"
						.concat(simplifiedFeaturePath)) });

		// trigger lemma
		featuresString.add(new String[] { triggerToken.getLemma().toLowerCase()
				.concat(null == featurePath ? "" : "_".concat(featurePath)) });

		// trigger sublemma
		featuresString.add(new String[] { triggerToken.getSubLemma()
				.toLowerCase()
				.concat(null == featurePath ? "" : "_".concat(featurePath)) });

		// trigger POS
		featuresString.add(new String[] { triggerToken.getPos().concat(
				null == featurePath ? "" : "_".concat(featurePath)) });
		featuresString.add(new String[] { triggerToken.getPos().substring(0, 1)
				.concat(null == featurePath ? "" : "_".concat(featurePath)) });

		featuresString.add(new String[] { triggerToken.getLemma().toLowerCase()
				.concat(triggerToken.getPos()) });
		featuresString.add(new String[] { triggerToken.getLemma().toLowerCase()
				.concat(triggerToken.getPos().substring(0, 1)) });
		featuresString.add(new String[] { triggerToken.getSubLemma()
				.toLowerCase().concat(triggerToken.getPos()) });
		featuresString.add(new String[] { triggerToken.getSubLemma()
				.toLowerCase().concat(triggerToken.getPos().substring(0, 1)) });

		// argument type
		String argType = null;
		if (anno instanceof Protein) {
			argType = "Protein";
		} else if (anno instanceof Trigger) {
			argType = ((Trigger) anno).getEventType();
		}
		featuresString.add(new String[] { triggerToken.getCoveredText()
				.toLowerCase()
				.concat(null == featurePath ? "" : "_".concat(featurePath))
				.concat("_").concat(argType) });
		featuresString.add(new String[] { triggerClassString
				.concat(null == featurePath ? "" : "_".concat(featurePath))
				.concat("_").concat(argType) });

		// text string: compensate when parsing fails
		// String tokensTextString = null;
		// Annotation frontAnno, endAnno;
		// FSIterator<Annotation> annoIter =
		// jcas.getAnnotationIndex().iterator();
		// boolean start = false, reversed = false;
		// while (annoIter.hasNext()) {
		// Annotation anAnnotation = annoIter.next();
		// if (anAnnotation.getBegin() == anno.getBegin()) {
		// reversed = true;
		// start = !start;
		// }
		// if (anAnnotation.getBegin() == triggerToken.getBegin()) {
		// start = !start;
		// }
		// }

		// concat dependency with other features.

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
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		featuresString
				.add(new String[] { anno.getCoveredText().toLowerCase() });
		featuresString.add(new String[] { tokenLemma.toLowerCase() });
		featuresString.add(new String[] { tokenPos });
		String leftTokenText = null == annoToken.getLeftToken() ? ""
				: annoToken.getLeftToken().getCoveredText().toLowerCase();
		featuresString
				.add(new String[] { isWord(leftTokenText)
						&& !StopWords.isAStopWordShortForNgram(leftTokenText) ? tokenLemma
						.concat("_").concat(leftTokenText) : aStopWord });
		featuresString
				.add(new String[] { isWord(leftTokenText)
						&& !StopWords.isAStopWordShortForNgram(leftTokenText) ? tokenLemma
						.concat("_")
						.concat(annoToken.getLeftToken().getLemma())
						: aStopWord });

		String rightTokenText = null == annoToken.getRightToken() ? ""
				: annoToken.getRightToken().getCoveredText().toLowerCase();
		featuresString
				.add(new String[] { isWord(rightTokenText)
						&& StopWords.isAStopWordShortForNgram(leftTokenText) ? tokenLemma
						.concat("_").concat(rightTokenText) : aStopWord });
		featuresString
				.add(new String[] { isWord(rightTokenText)
						&& StopWords.isAStopWordShortForNgram(leftTokenText) ? tokenLemma
						.concat("_").concat(
								annoToken.getRightToken().getLemma())
						: aStopWord });

		// String dependentText = null == annoToken.getDependent() ? ""
		// : annoToken.getDependent().getCoveredText().toLowerCase();
		// featuresString
		// .add(isWord(dependentText)
		// && !StopWords.isAStopWordShortForNgram(dependentText) ? tokenLemma
		// .concat("_").concat(
		// annoToken.getDependent().getLemma()
		// .toLowerCase()) : aStopWord);
		// featuresString
		// .add(isWord(dependentText)
		// && !StopWords.isAStopWordShortForNgram(dependentText) ? tokenPos
		// .concat("_").concat(annoToken.getDependent().getPos())
		// : aStopWord);
		//
		// String governorText = null == annoToken.getGovernor() ? "" :
		// annoToken
		// .getGovernor().getCoveredText().toLowerCase();
		// featuresString
		// .add(isWord(governorText)
		// && !StopWords.isAStopWordShortForNgram(governorText) ? tokenLemma
		// .concat("_").concat(
		// annoToken.getGovernor().getLemma()
		// .toLowerCase()) : aStopWord);
		// featuresString.add(isWord(governorText)
		// && !StopWords.isAStopWordShortForNgram(governorText) ? tokenPos
		// .concat("_").concat(annoToken.getGovernor().getPos())
		// : aStopWord);
		//
		// featuresString.add(annoToken.getSubLemma().equals("") ? aStopWord
		// : annoToken.getSubLemma());
		// featuresString.add(annoToken.getSubStem().equals("") ? aStopWord
		// : annoToken.getSubStem());
		// featuresString.add(tokenLemma.concat("_").concat(
		// Prefix.getPrefix(annoToken.getCoveredText())));
		// featuresString.add(tokenLemma.concat("_").concat(
		// Suffix.getSuffix(annoToken.getCoveredText())));
		//
		// featuresString.add(event.getTrigger().getEventType());
		//
		// Token triggerToken = getTriggerToken(jcas, event.getTrigger());
		// featuresString.add(triggerToken.getCoveredText().toLowerCase());
		// featuresString.add(triggerToken.getLemma().toLowerCase());
		// featuresString.add(triggerToken.getPos());
		// featuresString.add(triggerToken.getStem().toLowerCase());
		// featuresString.add(annoToken.getPos().concat("_")
		// .concat(triggerToken.getPos()));
		//
		// featuresString.add(dependencyExtractor.getDijkstraShortestPath(
		// annoToken, triggerToken));

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

	protected boolean isWord(String text) {

		return text.matches("^[a-zA-Z].+");
	}

}
