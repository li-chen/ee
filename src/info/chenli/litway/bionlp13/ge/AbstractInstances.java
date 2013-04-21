package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
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

	private int[] annotationTypes; // the annotation types need consideration
	private String taeDescriptor;
	protected List<StructuredInstance> structuredInstances = new LinkedList<StructuredInstance>();
	protected List<Instance> instances = null;
	protected List<String> labelsString;

	// public static final String aStopWord = "!AStopWord!";

	protected void setTaeDescriptor(String taeDescriptor) {

		this.taeDescriptor = taeDescriptor;

	}

	protected XMLInputSource getXMLInputSource() throws IOException,
			URISyntaxException {

		URL url = this.getClass().getResource(taeDescriptor);
		return new XMLInputSource(url);
	};

	private AnalysisEngine ae = null;

	public AbstractInstances(int[] annotationTypes) {

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

			processSingleFile(dataDir);

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

						processSingleFile(files[i]);
					}
				}
			}
		}
		ae.destroy();

		return getInstances();
	}

	protected JCas processSingleFile(File aFile) {

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
					if (null == feature) {
						continue;
					}
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
				if (feature > previousIndex) {
					sb.append(" ".concat(String.valueOf(feature)).concat(":1"));
				}
				previousIndex = feature;
			}

			sb.append("\n");
		}

		String instancesStr = sb.toString();
		FileUtil.saveFile(instancesStr, file);
	}

	protected Instance themeToInstance(JCas jcas, Sentence sentence,
			Annotation anno, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTruepositive) {
		return themeCauseToInstance(jcas, sentence, anno, trigger,
				pairsOfSentence, dependencyExtractor, isTruepositive, "Theme",
				null);
	}

	protected Instance causeToInstance(JCas jcas, Sentence sentence,
			Annotation anno, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTruepositive,
			Token themeToken) {
		return themeCauseToInstance(jcas, sentence, anno, trigger,
				pairsOfSentence, dependencyExtractor, isTruepositive, "Cause",
				themeToken);
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
	 * @param themeOrCause
	 * @param themeToken
	 * @return
	 */
	private Instance themeCauseToInstance(JCas jcas, Sentence sentence,
			Annotation anno, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTruepositive,
			String themeOrCause, Token themeToken) {

		if (!(anno instanceof Trigger) && !(anno instanceof Protein)) {
			throw new IllegalArgumentException(
					"The theme/cause has to be a protein or trigger.");
		}

		List<Token> annoTokens = JCasUtil
				.selectCovered(jcas, Token.class, anno);

		// if protein/trigger is within a token
		if (annoTokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(Token.type)
					.iterator();
			annoTokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= anno.getBegin()
						&& token.getEnd() >= anno.getEnd()) {
					annoTokens.add(token);
					break;
				}
			}
		}

		Token token = null;
		if (anno instanceof Protein)
		// Take the last non-digital token if protein is
		// multi-token.
		{
			token = annoTokens.get(annoTokens.size() - 1);
			// for (Token aToken : annoTokens) {
			//
			// try {
			// Double.parseDouble(aToken.getLemma());
			// break;
			// } catch (NumberFormatException e) {
			// token = aToken;
			// }
			//
			// }
		} else if (anno instanceof Trigger) {
			token = getTriggerToken(jcas, (Trigger) anno);
		}

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);

		// parser : dependency path between trigger-argument
		String dependencyPath = dependencyExtractor.getShortestPath(
				triggerToken, token);
		String featurePath = dependencyPath;

		if (null == dependencyPath) {
			featurePath = dependencyExtractor.getReversedShortestPath(
					triggerToken, token);
		}
		featurePath = (null == featurePath ? null : "dep_".concat(featurePath));
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { featurePath });

		// parser refined?

		// parser_simple: grouping of dpendency type;
		// amod, nn --> nmod
		// anything ending in subj --> subj
		// anything ending in subjpass --> subjpass
		String simplifiedFeaturePath = null;
		if (null != dependencyPath) {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedShortestPath(triggerToken, token);
		} else {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedReversedShortestPath(triggerToken, token);
		}
		simplifiedFeaturePath = (null == simplifiedFeaturePath ? null
				: "dep_simple_".concat(simplifiedFeaturePath));
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { simplifiedFeaturePath });

		// trigger class
		String triggerClassString;
		if (EventType.isSimpleEvent(trigger.getEventType())) {
			triggerClassString = "class_Simple";
		} else if (EventType.isBindingEvent(trigger.getEventType())) {
			triggerClassString = "class_Binding";
		} else if (EventType.isRegulatoryEvent(trigger.getEventType())) {
			triggerClassString = "class_Regulation";
		} else {
			triggerClassString = "class_Complex";
		}
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_").concat(
						featurePath) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { triggerClassString.concat("_").concat(
						simplifiedFeaturePath) });

		// trigger token & trigger type
		String triggerText = "text_".concat(token.getCoveredText()
				.toLowerCase());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath) });
		String eventType = "eventType_".concat(trigger.getEventType());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { eventType.concat("_").concat(featurePath) });

		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						simplifiedFeaturePath) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { eventType.concat("_").concat(
						simplifiedFeaturePath) });

		// trigger lemma
		String triggerLemma = "triggerLemma_".concat(triggerToken.getLemma()
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath) });

		// trigger sublemma
		String triggerSubLemma = (null == triggerToken.getSubLemma() ? triggerToken
				.getLemma() : "triggerSubLemma_".concat(triggerToken
				.getSubLemma().toLowerCase()));
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath) });

		// trigger POS
		String triggerPos = "triggerPos_".concat(triggerToken.getPos());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPos.concat("_").concat(featurePath) });
		String triggerPosShort = "triggerShortPos_".concat(triggerToken
				.getPos().substring(0, 1));
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPosShort.concat("_")
						.concat(featurePath) });

		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPosShort) });
		featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPosShort) });

		// argument type
		String argType = null;
		if (anno instanceof Protein) {
			argType = "argType_Protein";
		} else if (anno instanceof Trigger) {
			argType = "argType_".concat(((Trigger) anno).getEventType());
		}
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath)
						.concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });

		// text string from trigger to theme/cause: compensate when parsing
		// fails
		List<Token> tokensBetween = JCasUtil.selectCovered(jcas, Token.class,
				sentence);
		List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
				Protein.class, sentence);
		int start = Math.min(token.getBegin(), triggerToken.getBegin());
		int end = Math.max(token.getEnd(), triggerToken.getEnd());
		boolean reversed = (start != triggerToken.getBegin());

		List<String> tokensTextBetween = new ArrayList<String>();
		List<String> tokensAbsTextBetween = new ArrayList<String>();

		tokensLoop: for (Token aToken : tokensBetween) {

			if (aToken.getBegin() < start || !POS.isPos(aToken.getPos())) {
				continue tokensLoop;
			} else if (aToken.getEnd() >= end) {
				break tokensLoop;
			}

			// if it is a protein
			for (Protein aProtein : proteinsBetween) {
				if (aToken.getBegin() == aProtein.getBegin()) {
					tokensTextBetween.add("PROTEIN");
					tokensAbsTextBetween.add("PROTEIN");
					continue tokensLoop;
				} else if (aToken.getBegin() > aProtein.getBegin()
						&& aToken.getEnd() <= aProtein.getEnd()) {
					continue tokensLoop;
				}
			}
			if (aToken.getBegin() == trigger.getBegin()) {
				tokensAbsTextBetween.add(trigger.getEventType());
				continue tokensLoop;
			} else if (aToken.getBegin() > trigger.getBegin()
					&& aToken.getEnd() <= trigger.getEnd()) {
				continue tokensLoop;
			}

			tokensTextBetween.add(aToken.getLemma().toLowerCase());
			tokensAbsTextBetween.add(aToken.getLemma().toLowerCase());

		}

		String textBetween = "", textAbsBetween = "";
		for (String aText : tokensTextBetween) {
			if (reversed) {
				textBetween = aText.concat(textBetween.equals("") ? "" : "_"
						.concat(textBetween));
			} else {
				textBetween = textBetween.equals("") ? aText : textBetween
						.concat("_").concat(aText);
			}
		}
		for (String aText : tokensAbsTextBetween) {
			if (reversed) {
				textAbsBetween = aText.concat(textAbsBetween.equals("") ? ""
						: "_".concat(textAbsBetween));
			} else {
				textAbsBetween = textAbsBetween.equals("") ? aText
						: textAbsBetween.concat("_").concat(aText);
			}
		}

		// concatenate text between trigger and theme/cause with the previous
		// features.
		textBetween = textBetween.equals("") ? null : "textString_".concat(
				reversed ? "reversed_" : "").concat(textBetween);
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { textBetween });
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(textBetween) });
		featuresString
				.add(null != textBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textBetween) } : new String[0]);

		textAbsBetween = textAbsBetween.equals("") ? null : "textStringAbs_"
				.concat(reversed ? "reversed_" : "").concat(textAbsBetween);
		featuresString.add(null == textAbsBetween ? new String[0]
				: new String[] { textAbsBetween });
		featuresString
				.add(null == textAbsBetween ? new String[0]
						: new String[] { triggerText.concat("_").concat(
								textAbsBetween) });

		String textShortBetween = "";
		for (int i = 1; i < tokensAbsTextBetween.size() - 1; i++) {
			if (reversed) {
				textShortBetween = tokensAbsTextBetween.get(i).concat(
						textShortBetween.equals("") ? "" : "_"
								.concat(textShortBetween));
			} else {
				textShortBetween = textShortBetween.equals("") ? tokensAbsTextBetween
						.get(i) : textShortBetween.concat("_").concat(
						tokensAbsTextBetween.get(i));
			}
		}
		textShortBetween = textShortBetween.equals("") ? null
				: "textStringShort_".concat(reversed ? "reversed_" : "")
						.concat(textShortBetween);
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { textShortBetween });
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						textShortBetween) });
		featuresString
				.add(null != textShortBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textShortBetween) } : new String[0]);

		if (themeOrCause.equals("Cause")) {
			String pathToTheme = null;
			if (null != themeToken) {
				pathToTheme = dependencyExtractor.getShortestPath(token,
						themeToken);
				if (null == pathToTheme) {
					pathToTheme = dependencyExtractor.getReversedShortestPath(
							token, themeToken);
				}
			}
			featuresString
					.add(null != pathToTheme && themeToken != null ? new String[] { pathToTheme }
							: new String[0]);
		}

		if (isTruepositive) {

			instance.setLabelString(themeOrCause);

		} else {
			instance.setLabelString("Non_".concat(themeOrCause.toLowerCase()));
		}

		return instance;
	}

	protected Token getProteinToken(JCas jcas, List<Protein> proteins,
			String themeId) {

		Token themeToken = null;

		for (Protein protein : proteins) {
			if (protein.getId().equals(themeId)) {

				List<Token> annoTokens = JCasUtil.selectCovered(jcas,
						Token.class, protein);

				// if protein/trigger is within a token
				if (annoTokens.size() == 0) {
					FSIterator<Annotation> iter = jcas.getAnnotationIndex(
							Token.type).iterator();
					annoTokens = new ArrayList<Token>();
					while (iter.hasNext()) {
						Token token = (Token) iter.next();
						if (token.getBegin() <= protein.getBegin()
								&& token.getEnd() >= protein.getEnd()) {
							annoTokens.add(token);
							break;
						}
					}
				}

				themeToken = annoTokens.get(annoTokens.size() - 1);
				// for (Token aToken : annoTokens) {
				//
				// try {
				// Double.parseDouble(aToken.getLemma());
				// break;
				// } catch (NumberFormatException e) {
				// themeToken = aToken;
				// }
				//
				// }
			}
		}
		return themeToken;
	}

	protected Instance bindingEventToInstance(JCas jcas, Sentence sentence,
			Event bindingEvent, List<Protein> themes,
			DependencyExtractor dependencyExtractor) {

		boolean truepositive = true;
		if (themes.size() == bindingEvent.getThemes().size()) {
			themeSearchingLoop: for (Protein protein : themes) {
				boolean foundTheProtein = false;
				for (int i = 0; i < bindingEvent.getThemes().size(); i++) {
					if (protein.getId().equals(bindingEvent.getThemes(i))) {
						foundTheProtein = true;
						break;
					}
				}
				if (foundTheProtein == false) {
					truepositive = false;
					break themeSearchingLoop;
				}
			}
		} else {
			truepositive = false;
		}

		Instance instance = new Instance();

		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		Trigger trigger = bindingEvent.getTrigger();
		Token triggerToken = getTriggerToken(jcas, trigger);

		List<Token> themeTokens = new ArrayList<Token>();
		for (Protein aProtein : themes) {
			List<Token> annoTokens = JCasUtil.selectCovered(jcas, Token.class,
					aProtein);

			// if protein/trigger is within a token
			if (annoTokens.size() == 0) {
				FSIterator<Annotation> iter = jcas.getAnnotationIndex(
						Token.type).iterator();
				annoTokens = new ArrayList<Token>();
				while (iter.hasNext()) {
					Token token = (Token) iter.next();
					if (token.getBegin() <= aProtein.getBegin()
							&& token.getEnd() >= aProtein.getEnd()) {
						annoTokens.add(token);
						break;
					}
				}
			}

			Token token = null;
			token = annoTokens.get(0);
			for (Token aToken : annoTokens) {

				try {
					Double.parseDouble(aToken.getLemma());
					break;
				} catch (NumberFormatException e) {
					token = aToken;
				}
			}
			themeTokens.add(token);
		}

		if (themeTokens.size() == 0) {
			throw new RuntimeException("Theme number is zero. Please check.");
		}
		String triggerText = "text_".concat(triggerToken.getCoveredText()
				.toLowerCase());
		String triggerLemma = "triggerLemma_".concat(triggerToken.getLemma()
				.toLowerCase());
		String triggerSubLemma = (null == triggerToken.getSubLemma() ? triggerToken
				.getLemma() : "triggerSubLemma_".concat(triggerToken
				.getSubLemma().toLowerCase()));
		String triggerPos = "triggerPos_".concat(triggerToken.getPos());
		String triggerPosShort = "triggerShortPos_".concat(triggerToken
				.getPos().substring(0, 1));

		// parser : dependency path between trigger-argument
		int i = 0;
		String[] dependencyPaths = new String[themeTokens.size()];
		String[] simplifiedFeaturePaths = new String[themeTokens.size()];
		String[] triggerTextPaths = new String[themeTokens.size()];
		String[] triggerTextSimplifiedPaths = new String[themeTokens.size()];
		String[] triggerLemmaPaths = new String[themeTokens.size()];
		String[] triggerSubLemmaPaths = new String[themeTokens.size()];
		String[] triggerPosPaths = new String[themeTokens.size()];
		String[] triggerPosShortPaths = new String[themeTokens.size()];
		String[] textBetweens = new String[themeTokens.size()];
		String[] triggerTextBetweens = new String[themeTokens.size()];
		String[] textBetweenDependencies = new String[themeTokens.size()];
		String[] textAbsBetweenDependencies = new String[themeTokens.size()];
		String[] textShortBetweens = new String[themeTokens.size()];
		String[] textShortBetweenDependencyPaths = new String[themeTokens
				.size()];
		for (Token aThemeToken : themeTokens) {
			String dependencyPath = dependencyExtractor.getShortestPath(
					triggerToken, aThemeToken);
			String featurePath = dependencyPath;

			if (null == dependencyPath) {
				featurePath = dependencyExtractor.getReversedShortestPath(
						triggerToken, aThemeToken);
			}
			featurePath = (null == featurePath ? null : "dep_"
					.concat(featurePath));
			dependencyPaths[i] = featurePath;

			String simplifiedFeaturePath = null;
			// parser refined?

			// parser_simple: grouping of dpendency type;
			// amod, nn --> nmod
			// anything ending in subj --> subj
			// anything ending in subjpass --> subjpass
			if (null != dependencyPath) {
				simplifiedFeaturePath = dependencyExtractor
						.getSimplifiedShortestPath(triggerToken, aThemeToken);
			} else {
				simplifiedFeaturePath = dependencyExtractor
						.getSimplifiedReversedShortestPath(triggerToken,
								aThemeToken);
			}
			simplifiedFeaturePath = (null == simplifiedFeaturePath ? null
					: "dep_simple_".concat(simplifiedFeaturePath));
			simplifiedFeaturePaths[i] = simplifiedFeaturePath;

			triggerTextPaths[i] = null == featurePath ? null : triggerText
					.concat("_").concat(featurePath);
			triggerTextSimplifiedPaths[i] = null == simplifiedFeaturePath ? null
					: triggerText.concat("_").concat(simplifiedFeaturePath);

			triggerLemmaPaths[i] = null == featurePath ? null : triggerLemma
					.concat("_").concat(featurePath);

			triggerSubLemmaPaths[i] = null == featurePath ? null
					: triggerSubLemma.concat("_").concat(featurePath);

			triggerPosPaths[i] = null == featurePath ? null : triggerPos
					.concat("_").concat(featurePath);
			triggerPosShortPaths[i] = null == featurePath ? null
					: triggerPosShort.concat("_").concat(featurePath);

			// text string from trigger to theme/cause: compensate when parsing
			// fails
			List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			int start = Math.min(aThemeToken.getBegin(),
					triggerToken.getBegin());
			int end = Math.max(aThemeToken.getEnd(), triggerToken.getEnd());
			boolean reversed = (start != triggerToken.getBegin());

			List<String> tokensTextBetween = new ArrayList<String>();
			List<String> tokensAbsTextBetween = new ArrayList<String>();

			tokensLoop: for (Token aToken : tokensBetween) {

				if (aToken.getBegin() < start || !POS.isPos(aToken.getPos())) {
					continue tokensLoop;
				} else if (aToken.getEnd() >= end) {
					break tokensLoop;
				}

				// if it is a protein
				for (Protein aProtein : proteinsBetween) {
					if (aToken.getBegin() == aProtein.getBegin()) {
						tokensTextBetween.add("PROTEIN");
						tokensAbsTextBetween.add("PROTEIN");
						continue tokensLoop;
					} else if (aToken.getBegin() > aProtein.getBegin()
							&& aToken.getEnd() <= aProtein.getEnd()) {
						continue tokensLoop;
					}
				}
				if (aToken.getBegin() == trigger.getBegin()) {
					tokensAbsTextBetween.add(trigger.getEventType());
					continue tokensLoop;
				} else if (aToken.getBegin() > trigger.getBegin()
						&& aToken.getEnd() <= trigger.getEnd()) {
					continue tokensLoop;
				}

				tokensTextBetween.add(aToken.getLemma().toLowerCase());
				tokensAbsTextBetween.add(aToken.getLemma().toLowerCase());

			}

			String textBetween = "", textAbsBetween = "";
			for (String aText : tokensTextBetween) {
				if (reversed) {
					textBetween = aText.concat(textBetween.equals("") ? ""
							: "_".concat(textBetween));
				} else {
					textBetween = textBetween.equals("") ? aText : textBetween
							.concat("_").concat(aText);
				}
			}
			for (String aText : tokensAbsTextBetween) {
				if (reversed) {
					textAbsBetween = aText
							.concat(textAbsBetween.equals("") ? "" : "_"
									.concat(textAbsBetween));
				} else {
					textAbsBetween = textAbsBetween.equals("") ? aText
							: textAbsBetween.concat("_").concat(aText);
				}
			}

			textBetweens[i] = textBetween.equals("") ? null : "textString_"
					.concat(reversed ? "reversed_" : "").concat(textBetween);

			triggerTextBetweens[i] = null == textBetween ? null : triggerText
					.concat("_").concat(textBetween);
			textBetweenDependencies[i] = null != textBetween
					&& null != dependencyPath ? dependencyPath.concat("_")
					.concat(textBetween) : null;
			textAbsBetweenDependencies[i] = textAbsBetween.equals("") ? null
					: "textStringAbs_".concat(reversed ? "reversed_" : "")
							.concat(textAbsBetween);

			String textShortBetween = "";
			for (int j = 1; j < tokensAbsTextBetween.size() - 1; j++) {
				if (reversed) {
					textShortBetween = tokensAbsTextBetween.get(j).concat(
							textShortBetween.equals("") ? "" : "_"
									.concat(textShortBetween));
				} else {
					textShortBetween = textShortBetween.equals("") ? tokensAbsTextBetween
							.get(j) : textShortBetween.concat("_").concat(
							tokensAbsTextBetween.get(j));
				}
			}

			textShortBetweens[i] = textShortBetween.equals("") ? null
					: "textStringShort_".concat(reversed ? "reversed_" : "")
							.concat(textShortBetween);
			textShortBetweenDependencyPaths[i] = null != textShortBetween
					&& null != dependencyPath ? dependencyPath.concat("_")
					.concat(textShortBetween) : null;
			i++;
		}

		featuresString.add(dependencyPaths);
		featuresString.add(simplifiedFeaturePaths);

		// trigger token & trigger type
		featuresString.add(triggerTextPaths);
		featuresString.add(triggerTextSimplifiedPaths);

		// trigger lemma
		featuresString.add(triggerLemmaPaths);

		// trigger sublemma
		featuresString.add(triggerSubLemmaPaths);

		// trigger POS
		featuresString.add(triggerPosPaths);
		featuresString.add(triggerPosShortPaths);

		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPosShort) });
		featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPosShort) });

		// concatenate text between trigger and theme/cause with the previous
		// features.
		featuresString.add(textBetweens);
		featuresString.add(triggerTextBetweens);
		featuresString.add(textBetweenDependencies);
		featuresString.add(textAbsBetweenDependencies);

		featuresString.add(textShortBetweens);
		featuresString.add(textShortBetweenDependencyPaths);

		if (truepositive) {

			instance.setLabelString("Binding");

		} else {
			instance.setLabelString("Non_binding");
		}

		return instance;
	}

	protected boolean isWord(String text) {

		return text.matches("^[a-zA-Z].+");
	}

}
