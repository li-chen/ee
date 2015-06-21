package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileFilterImpl;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;
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

import de.bwaldvogel.liblinear.FeatureNode;

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
			
			
			//System.out.println(UimaUtil.getJCasFilePath(jcas));
					
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
				//System.out.println(token.getStem());
				if (!POS.isPos(token.getPos())) {
					continue;
				}
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
			
			double[] fs = instance.getFeaturesNumericWord2vec();
			if (null != fs) {
				for (int m=0; m<fs.length; m++) {
					sb.append(" ".concat(String.valueOf(m + 1)).concat(":" + String.valueOf(fs[m])));
				}
			}

			int previousIndex = 0;
			for (int feature : instance.getFeaturesNumeric()) {
				if (feature > previousIndex) {
					if (null != fs) {
						sb.append(" ".concat(String.valueOf(fs.length + feature)).concat(":1"));
					}else {
						sb.append(" ".concat(String.valueOf(feature)).concat(":1"));
					}
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
				pairsOfSentence, dependencyExtractor, isTruepositive,
				Stage.THEME, null);
	}

	protected Instance causeToInstance(JCas jcas, Sentence sentence,
			Annotation anno, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTruepositive,
			Token themeToken) {
		return themeCauseToInstance(jcas, sentence, anno, trigger,
				pairsOfSentence, dependencyExtractor, isTruepositive,
				Stage.CAUSE, themeToken);
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
	protected Instance argumentToInstance(JCas jcas, Sentence sentence,
			Annotation annotation, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTheme,
			boolean isCause, Stage stage) {

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);
		//System.out.println(annotation.getCoveredText());
		Token annotationToken = getToken(jcas, annotation);
		if (annotation instanceof Trigger) {
			annotationToken = getTriggerToken(jcas, (Trigger)annotation);
		}
		// parser : dependency path between trigger-argument
		//int dependencyPathLength = dependencyExtractor.getDijkstraShortestPathLength(
		//		triggerToken, annotationToken);
		//featuresString.add(new String[] { "dependencyPathLength_" + String.valueOf(dependencyPathLength) });
		String featurePath = dependencyExtractor.getShortestPath(
				triggerToken, annotationToken, stage);
/*		if ( isTruepositive && null == featurePath && !areSameTokens) {
			int i = sentence.getId();
			String s = triggerToken.getCoveredText();
			String s2 = annoToken.getCoveredText();
			return null;
		}*/
		boolean areSameTokens = (annotationToken.getId() == triggerToken.getId());
		featurePath = areSameTokens ? "SAMETOKEN" : featurePath;
		featurePath = (null == featurePath ? null : "featurePath_".concat(featurePath));
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { featurePath });
		//instance.setId(featurePath);
		//instance.setFileId(trigger.getEventType());
		/*if (areSameTokens && isTheme) {
			System.out.println("theme" + "\t" + trigger.getEventType());
		} else if (areSameTokens && !isTheme && !isCause) {
			System.out.println("not" + "\t" + trigger.getEventType());
		} else if (areSameTokens && isCause) {
			System.out.println("isCause" + "\t" + trigger.getEventType());
		} */
		// parser refined?

		// parser_simple: grouping of dpendency type;
		// amod, nn --> nmod
		// anything ending in subj --> subj
		// anything ending in subjpass --> subjpass
		/*String simplifiedFeaturePath = dependencyExtractor
				.getSimplifiedShortestPath(triggerToken, annotationToken, stage);
		simplifiedFeaturePath = areSameTokens ? "SAMETOKEN"
				: simplifiedFeaturePath;
		simplifiedFeaturePath = (null == simplifiedFeaturePath ? null
				: "simplifiedFeaturePath_".concat(simplifiedFeaturePath));
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { simplifiedFeaturePath });*/

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
		//featuresString.add(new String[] { triggerClassString });
		/*featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_").concat(
						featurePath) });*/
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString
				//.concat("_").concat(featurePath) 
				});

		// trigger token & trigger type
		/*String triggerText = "text_".concat(trigger.getCoveredText()
				.toLowerCase());
		featuresString.add(new String[] { triggerText });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						simplifiedFeaturePath) });*/
		
		/*String eventType = "eventType_".concat(trigger.getEventType());
		//featuresString.add(new String[] { eventType });
		//featuresString.add(null == featurePath ? new String[0]
		//		: new String[] { eventType.concat("_").concat(featurePath) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { eventType
				//.concat("_").concat(featurePath)
				});
*/
		// trigger lemma (using the token's POS, which may be inaccurate)
		String triggerLemma = "triggerLemma_".concat(triggerToken.getLemma());
		triggerLemma = (null == triggerToken.getSubLemma() ? triggerLemma
				: "triggerLemma_".concat(triggerToken.getSubLemma()
						.toLowerCase()));
		featuresString.add(new String[] { triggerLemma });
		/*featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_")
						.concat(featurePath) });*/

		// trigger POS
		/*String triggerPos = "triggerPos_".concat(triggerToken.getPos());
		//featuresString.add(new String[] { triggerPos });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPos.concat("_").concat(featurePath) });*/
		String triggerPosShort = "triggerShortPos_".concat(triggerToken
				.getPos().substring(0, 1));
		//featuresString.add(new String[] { triggerPosShort });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPosShort.concat("_")
		     			.concat(featurePath) });

		/*featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPosShort) });*/

		// argument type
		String argClassString, argType, argLemma;
		if (annotation instanceof Trigger) {
			argType = ((Trigger)annotation).getEventType();
			argLemma = "argLemma_".concat(annotationToken.getLemma());
			argLemma = (null == annotationToken.getSubLemma() ? argLemma
					: "argLemma_".concat(annotationToken.getSubLemma()
							.toLowerCase()));
			if (EventType.isSimpleEvent(((Trigger)annotation).getEventType())) {
				argClassString = "arg_class_Simple";
			} else if (EventType.isBindingEvent(((Trigger)annotation).getEventType())) {
				argClassString = "arg_class_Binding";
			} else if (EventType.isRegulatoryEvent(((Trigger)annotation).getEventType())) {
				argClassString = "arg_class_Regulation";
			} else {
				argClassString = "arg_class_Complex";
			}
		} else {
			argClassString = "arg_class_Protein";
			argType = "arg_class_Protein";
			argLemma = "arg_class_Protein";
		}
		//featuresString.add(new String[] { argClassString + triggerClassString});
		//featuresString.add(new String[] { argClassString + triggerLemma});
		//featuresString.add(new String[] { argType });
		//featuresString.add(new String[] { argLemma });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argClassString) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_")
						.concat(featurePath).concat("_").concat(argClassString) });
		/*featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_")
						.concat(simplifiedFeaturePath).concat("_").concat(argLemma) });*/
/*		String argText = "text_Protein";
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath).
				concat("_").concat(argText) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
		: new String[] { triggerText.concat("_").concat(
				simplifiedFeaturePath).concat("_").concat(
						argText) });

		String argLemma = "argLemma_Protein";
		featuresString.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath).concat("_").concat(
										argLemma) });

		String argPos = "argPos_Protein";
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPos
				.concat("_").concat(argPos) });

		String argType = "argType_Protein";
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(featurePath)
						.concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath).concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });*/

		// text string from trigger to theme/cause: compensate when parsing
		// fails
		String textBetween = "", textAbsBetween = "", textShortBetween = "";

		List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
				Token.class, sentence);
		List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
				Protein.class, sentence);
		int start = Math.min(annotationToken.getBegin(), triggerToken.getBegin());
		int end = Math.max(annotationToken.getEnd(), triggerToken.getEnd());
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

		/*int tokensTextBetweenLength = tokensTextBetween.size();
		String[] tokensTextBetweenString = new String[tokensTextBetweenLength];
		featuresString.add(new String[] { "tokensTextBetweenLength_" + String.valueOf(tokensTextBetweenLength) });
		int  j= 0;
		for (String aText : tokensTextBetween) {			
			tokensTextBetweenString[j] = aText;
			j++;
		}
		featuresString.add(tokensTextBetweenString);*/
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
		// concatenate text between trigger and theme/cause with the
		// previous
		// features.
		textBetween = textBetween.equals("") ? null : "textString_".concat(
				reversed ? "reversed_" : "").concat(textBetween);
		textAbsBetween = textAbsBetween.equals("") ? null
				: "textStringAbs_".concat(reversed ? "reversed_" : "")
						.concat(textAbsBetween);
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
		if (areSameTokens) {
			textBetween = "SAMETOKEN";
			textAbsBetween = "SAMETOKEN";
			textShortBetween = "SAMETOKEN";
		}

		featuresString.add(null == textBetween ? new String[0]
				: new String[] { textBetween });
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(textBetween) });
		featuresString
				.add(null != textBetween && null != featurePath ? new String[] { featurePath
						.concat("_").concat(textBetween) } : new String[0]);

		/**/featuresString.add(null == textAbsBetween ? new String[0]
				: new String[] { textAbsBetween });
		featuresString
				.add(null == textAbsBetween ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								textAbsBetween) });
		/*featuresString
		.add(null != textAbsBetween && null != featurePath ? new String[] { featurePath
				.concat("_").concat(textAbsBetween) } : new String[0]);*/
		
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { textShortBetween });
		/*featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(
						textShortBetween) });*/
		/*featuresString
				.add(null != textShortBetween && null != featurePath ? new String[] { featurePath
						.concat("_").concat(textShortBetween) } : new String[0]);*/

		if (isTheme) {
			instance.setLabelString("Theme");
		} else if (isCause){
			instance.setLabelString("Cause");
		} else {
			instance.setLabelString("Non_Argument");
		}

		return instance;
	}

	protected Instance triggerArgumentToInstance(JCas jcas, Sentence sentence,
			Trigger arguTrigger, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTheme,
			boolean isCause, Stage stage) {

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);
		Token arguToken = getTriggerToken(jcas, arguTrigger);
		// parser : dependency path between trigger-argument
		String dependencyPath = dependencyExtractor.getShortestPath(
				triggerToken, arguToken, stage);
		String featurePath = dependencyPath;

		if (null == dependencyPath) {
			featurePath = dependencyExtractor.getReversedShortestPath(
					triggerToken, arguToken, stage);
		}
				
		boolean areSameTokens = (arguToken.getBegin() == triggerToken
				.getBegin() && arguToken.getEnd() == triggerToken.getEnd());
		
/*		if ( isTruepositive && null == featurePath && !areSameTokens) {
			int i = sentence.getId();
			String s = triggerToken.getCoveredText();
			String s2 = annoToken.getCoveredText();
			return null;
		}*/
		featurePath = areSameTokens ? "SAMETOKEN" : featurePath;
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
					.getSimplifiedShortestPath(triggerToken, arguToken, stage);
		} else {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedReversedShortestPath(triggerToken, arguToken,
							stage);
		}
		simplifiedFeaturePath = areSameTokens ? "SAMETOKEN"
				: simplifiedFeaturePath;
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
		String triggerText = "text_".concat(trigger.getCoveredText()
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

		// trigger lemma (using the token's POS, which may be inaccurate)
		String triggerLemma = "triggerLemma_".concat(BioLemmatizerUtil
				.lemmatizeWord(trigger.getCoveredText(), triggerToken.getPos())
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath) });
		

		// trigger sublemma
		String triggerSubLemma = (null == triggerToken.getSubLemma() ? triggerLemma
				: "triggerSubLemma_".concat(triggerToken.getSubLemma()
						.toLowerCase()));
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
		String argClassString;
		if (EventType.isSimpleEvent(arguTrigger.getEventType())) {
			argClassString = "class_Simple";
		} else if (EventType.isBindingEvent(arguTrigger.getEventType())) {
			argClassString = "class_Binding";
		} else if (EventType.isRegulatoryEvent(arguTrigger.getEventType())) {
			argClassString = "class_Regulation";
		} else {
			argClassString = "class_Complex";
		}		
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(featurePath)
						.concat("_").concat(argClassString) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath).concat("_").concat(argClassString) });
		String argType = "argType_".concat(arguTrigger.getEventType());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });
/*		String argText = "text_".concat(arguTrigger.getCoveredText()
				.toLowerCase());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath).
				concat("_").concat(argText) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
		: new String[] { triggerText.concat("_").concat(
				simplifiedFeaturePath).concat("_").concat(
						argText) });

		String argLemma = "argLemma_".concat(BioLemmatizerUtil
				.lemmatizeWord(arguTrigger.getCoveredText(), arguToken.getPos())
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath).concat("_").concat(
										argLemma) });

    	String argPos = "argPos_".concat(arguToken.getPos());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPos
				.concat("_").concat(argPos) });

		String argType = "argType_".concat(arguTrigger.getEventType());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(featurePath)
						.concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath).concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });*/
		// text string from trigger to theme/cause: compensate when parsing
		// fails
		String textBetween = "", textAbsBetween = "", textShortBetween = "";

		if (!areSameTokens) {
			List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			int start = Math.min(arguToken.getBegin(), triggerToken.getBegin());
			int end = Math.max(arguToken.getEnd(), triggerToken.getEnd());
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
			// concatenate text between trigger and theme/cause with the
			// previous
			// features.
			textBetween = textBetween.equals("") ? null : "textString_".concat(
					reversed ? "reversed_" : "").concat(textBetween);
			textAbsBetween = textAbsBetween.equals("") ? null
					: "textStringAbs_".concat(reversed ? "reversed_" : "")
							.concat(textAbsBetween);
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
		} else {
			textBetween = "SAMETOKEN";
			textAbsBetween = "SAMETOKEN";
			textShortBetween = "SAMETOKEN";
		}

		featuresString.add(null == textBetween ? new String[0]
				: new String[] { textBetween });
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(textBetween) });
		featuresString
				.add(null != textBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textBetween) } : new String[0]);

		featuresString.add(null == textAbsBetween ? new String[0]
				: new String[] { textAbsBetween });
		featuresString
				.add(null == textAbsBetween ? new String[0]
						: new String[] { triggerText.concat("_").concat(
								textAbsBetween) });

		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { textShortBetween });
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						textShortBetween) });
		featuresString
				.add(null != textShortBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textShortBetween) } : new String[0]);


		if (isTheme) {
			instance.setLabelString("Theme");
		} else if (isCause){
			instance.setLabelString("Cause");
		} else {
			instance.setLabelString("Non_Argument");
		}

		return instance;
	}

	protected Instance triggerSpeicalArgumentToInstance(JCas jcas, Sentence sentence,
			Trigger arguTrigger, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTheme,
			boolean isCause, Stage stage) {

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);
		Token arguToken = getTriggerToken(jcas, arguTrigger);
		// parser : dependency path between trigger-argument
		String dependencyPath = dependencyExtractor.getShortestPath(
				triggerToken, arguToken, stage);
		String featurePath = dependencyPath;

		if (null == dependencyPath) {
			featurePath = dependencyExtractor.getReversedShortestPath(
					triggerToken, arguToken, stage);
		}
				
		boolean areSameTokens = (arguToken.getBegin() == triggerToken
				.getBegin() && arguToken.getEnd() == triggerToken.getEnd());
		
/*		if ( isTruepositive && null == featurePath && !areSameTokens) {
			int i = sentence.getId();
			String s = triggerToken.getCoveredText();
			String s2 = annoToken.getCoveredText();
			return null;
		}*/
		featurePath = areSameTokens ? "SAMETOKEN" : featurePath;
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
					.getSimplifiedShortestPath(triggerToken, arguToken, stage);
		} else {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedReversedShortestPath(triggerToken, arguToken,
							stage);
		}
		simplifiedFeaturePath = areSameTokens ? "SAMETOKEN"
				: simplifiedFeaturePath;
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
		String triggerText = "text_".concat(trigger.getCoveredText()
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

		// trigger lemma (using the token's POS, which may be inaccurate)
		String triggerLemma = "triggerLemma_".concat(BioLemmatizerUtil
				.lemmatizeWord(trigger.getCoveredText(), triggerToken.getPos())
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath) });
		
		// trigger sublemma
		String triggerSubLemma = (null == triggerToken.getSubLemma() ? triggerLemma
				: "triggerSubLemma_".concat(triggerToken.getSubLemma()
						.toLowerCase()));
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
		String argClassString;
		if (EventType.isSimpleEvent(arguTrigger.getEventType())) {
			argClassString = "class_Simple";
		} else if (EventType.isBindingEvent(arguTrigger.getEventType())) {
			argClassString = "class_Binding";
		} else if (EventType.isRegulatoryEvent(arguTrigger.getEventType())) {
			argClassString = "class_Regulation";
		} else {
			argClassString = "class_Complex";
		}		
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_").concat(
						featurePath).concat("_").concat(argClassString) });		
		
		String argText = "text_".concat(arguTrigger.getCoveredText()
				.toLowerCase());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerText.concat("_").concat(featurePath).
				concat("_").concat(argText) });
		featuresString.add(null == simplifiedFeaturePath ? new String[0]
		: new String[] { triggerText.concat("_").concat(
				simplifiedFeaturePath).concat("_").concat(
						argText) });

		String argLemma = "argLemma_".concat(BioLemmatizerUtil
				.lemmatizeWord(arguTrigger.getCoveredText(), arguToken.getPos())
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath).concat("_").concat(
										argLemma) });

		String argPos = "argPos_".concat(arguToken.getPos());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerPos
				.concat("_").concat(argPos) });

		String argType = "argType_".concat(arguTrigger.getEventType());
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerLemma.concat("_").concat(featurePath)
						.concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath).concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });

		// text string from trigger to theme/cause: compensate when parsing
		// fails
		String textBetween = "", textAbsBetween = "", textShortBetween = "";

		if (!areSameTokens) {
			List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			int start = Math.min(arguToken.getBegin(), triggerToken.getBegin());
			int end = Math.max(arguToken.getEnd(), triggerToken.getEnd());
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
			// concatenate text between trigger and theme/cause with the
			// previous
			// features.
			textBetween = textBetween.equals("") ? null : "textString_".concat(
					reversed ? "reversed_" : "").concat(textBetween);
			textAbsBetween = textAbsBetween.equals("") ? null
					: "textStringAbs_".concat(reversed ? "reversed_" : "")
							.concat(textAbsBetween);
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
		} else {
			textBetween = "SAMETOKEN";
			textAbsBetween = "SAMETOKEN";
			textShortBetween = "SAMETOKEN";
		}

		featuresString.add(null == textBetween ? new String[0]
				: new String[] { textBetween });
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(textBetween) });
		featuresString
				.add(null != textBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textBetween) } : new String[0]);

		featuresString.add(null == textAbsBetween ? new String[0]
				: new String[] { textAbsBetween });
		featuresString
				.add(null == textAbsBetween ? new String[0]
						: new String[] { triggerText.concat("_").concat(
								textAbsBetween) });

		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { textShortBetween });
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						textShortBetween) });
		featuresString
				.add(null != textShortBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textShortBetween) } : new String[0]);


		if (isTheme) {
			instance.setLabelString("Theme");
		} else if (isCause){
			instance.setLabelString("Cause");
		} else {
			instance.setLabelString("Non_Argument");
		}

		return instance;
	}

	private Instance themeCauseToInstance(JCas jcas, Sentence sentence,
			Annotation anno, Trigger trigger, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, boolean isTruepositive,
			Stage stage, Token themeToken) {
		if (!(anno instanceof Trigger) && !(anno instanceof Protein)) {
			throw new IllegalArgumentException(
					"The theme/cause has to be a protein or trigger.");
		}

		List<Token> annoTokens = JCasUtil
				.selectCovered(jcas, Token.class, anno);

		// if protein/trigger is within a token
		if (annoTokens.size() == 0) {
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentence);
			annoTokens = new ArrayList<Token>();
			for (Token token : tokens) {
				if (token.getBegin() <= anno.getBegin()
						&& token.getEnd() >= anno.getEnd()) {
					annoTokens.add(token);
					break;
				}
			}
		}
/*		if (annoTokens.size() == 0) {
			int i = anno.getBegin();
			int j = anno.getEnd();
			String s = anno.getCoveredText();
		}*/
		Token annoToken = null;
		if (anno instanceof Protein)
		// Take the last non-digital token if protein is
		// multi-token.
		{
			annoToken = annoTokens.get(annoTokens.size() - 1);
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
			annoToken = getTriggerToken(jcas, (Trigger) anno);
		}

		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		// get trigger token
		Token triggerToken = getTriggerToken(jcas, trigger);

		// parser : dependency path between trigger-argument
		String dependencyPath = dependencyExtractor.getShortestPath(
				triggerToken, annoToken, stage);
		String featurePath = dependencyPath;

		if (null == dependencyPath) {
			featurePath = dependencyExtractor.getReversedShortestPath(
					triggerToken, annoToken, stage);
		}
				
		boolean areSameTokens = (annoToken.getBegin() == triggerToken
				.getBegin() && annoToken.getEnd() == triggerToken.getEnd());
		
/*		if ( isTruepositive && null == featurePath && !areSameTokens) {
			int i = sentence.getId();
			String s = triggerToken.getCoveredText();
			String s2 = annoToken.getCoveredText();
			return null;
		}*/
		featurePath = areSameTokens ? "SAMETOKEN" : featurePath;
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
					.getSimplifiedShortestPath(triggerToken, annoToken, stage);
		} else {
			simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedReversedShortestPath(triggerToken, annoToken,
							stage);
		}
		simplifiedFeaturePath = areSameTokens ? "SAMETOKEN"
				: simplifiedFeaturePath;
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
		String triggerText = "text_".concat(trigger.getCoveredText()
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

		// trigger lemma (using the token's POS, which may be inaccurate)
		String triggerLemma = "triggerLemma_".concat(BioLemmatizerUtil
				.lemmatizeWord(trigger.getCoveredText(), triggerToken.getPos())
				.toLowerCase());
		featuresString
				.add(null == featurePath ? new String[0]
						: new String[] { triggerLemma.concat("_").concat(
								featurePath) });

		// trigger sublemma
		String triggerSubLemma = (null == triggerToken.getSubLemma() ? triggerLemma
				: "triggerSubLemma_".concat(triggerToken.getSubLemma()
						.toLowerCase()));
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
				: new String[] { triggerLemma.concat("_").concat(featurePath)
						.concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerSubLemma.concat("_")
						.concat(featurePath).concat("_").concat(argType) });
		featuresString.add(null == featurePath ? new String[0]
				: new String[] { triggerClassString.concat("_")
						.concat(featurePath).concat("_").concat(argType) });

		// text string from trigger to theme/cause: compensate when parsing
		// fails
		String textBetween = "", textAbsBetween = "", textShortBetween = "";

		if (!areSameTokens) {
			List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			int start = Math.min(annoToken.getBegin(), triggerToken.getBegin());
			int end = Math.max(annoToken.getEnd(), triggerToken.getEnd());
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
			// concatenate text between trigger and theme/cause with the
			// previous
			// features.
			textBetween = textBetween.equals("") ? null : "textString_".concat(
					reversed ? "reversed_" : "").concat(textBetween);
			textAbsBetween = textAbsBetween.equals("") ? null
					: "textStringAbs_".concat(reversed ? "reversed_" : "")
							.concat(textAbsBetween);
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
		} else {
			textBetween = "SAMETOKEN";
			textAbsBetween = "SAMETOKEN";
			textShortBetween = "SAMETOKEN";
		}

		featuresString.add(null == textBetween ? new String[0]
				: new String[] { textBetween });
		featuresString.add(null == textBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(textBetween) });
		featuresString
				.add(null != textBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textBetween) } : new String[0]);

		featuresString.add(null == textAbsBetween ? new String[0]
				: new String[] { textAbsBetween });
		featuresString
				.add(null == textAbsBetween ? new String[0]
						: new String[] { triggerText.concat("_").concat(
								textAbsBetween) });

		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { textShortBetween });
		featuresString.add(null == textShortBetween ? new String[0]
				: new String[] { triggerText.concat("_").concat(
						textShortBetween) });
		featuresString
				.add(null != textShortBetween && null != dependencyPath ? new String[] { dependencyPath
						.concat("_").concat(textShortBetween) } : new String[0]);

		if (stage.equals(Stage.CAUSE)) {
			String pathToTheme = null;
			if (null != themeToken) {
				pathToTheme = dependencyExtractor.getShortestPath(annoToken,
						themeToken, stage);
				if (null == pathToTheme) {
					pathToTheme = dependencyExtractor.getReversedShortestPath(
							annoToken, themeToken, stage);
				}
			}
			featuresString
					.add(null != pathToTheme && themeToken != null ? new String[] { pathToTheme }
							: new String[0]);
		}

		String label;
		switch (stage) {
		case THEME:
			label = "Theme";
			break;
		case CAUSE:
			label = "Cause";
			break;
		default:
			label = null;
		}
		if (isTruepositive) {

			instance.setLabelString(label);

		} else {
			instance.setLabelString("Non_".concat(label.toLowerCase()));
		}

		return instance;
	}
	
	protected Instance bindingEventToInstance(JCas jcas, Sentence sentence,
			Trigger trigger, List<Protein> themes,
			DependencyExtractor dependencyExtractor, boolean truepositive) {
	
		Instance instance = new Instance();
		List<String[]> featuresString = new ArrayList<String[]>();
		instance.setFeaturesString(featuresString);

		Token triggerToken = getTriggerToken(jcas, trigger);						
		List<Token> themeTokens = new ArrayList<Token>();
		for (Protein aProtein : themes) {
			themeTokens.add(getToken(jcas, aProtein));
		}
		
		List<Token> tokensBetween = JCasUtil.selectCovered(jcas,
				Token.class, sentence);
		List<Protein> proteinsBetween = JCasUtil.selectCovered(jcas,
				Protein.class, sentence);

		String[] themePaths = new String[2];		
		if (themeTokens.size() == 1) {
			themePaths[0] = "themeSize=1";
			themePaths[1] = null;
			//themePaths[2] = null;
			//themePaths[3] = null;
		}else if (themeTokens.size() == 2) {
			String themePath0 = dependencyExtractor.getShortestPath(
					themeTokens.get(0), themeTokens.get(1), Stage.BINDING);
			String themePath1 = dependencyExtractor.getShortestPath(
					themeTokens.get(1), themeTokens.get(0), Stage.BINDING);
			//String themePath2 = dependencyExtractor
			//		.getSimplifiedShortestPath(themeTokens.get(0), themeTokens.get(1), Stage.BINDING);
			//String themePath3 = dependencyExtractor
			//		.getSimplifiedShortestPath(themeTokens.get(1), themeTokens.get(0), Stage.BINDING);
			themePaths[0] = null == themePath0 ? null : "themePath_" + themePath0;
			themePaths[1] = null == themePath1 ? null : "themePath_" + themePath1;
			//themePaths[0] = null == themePath2 ? null : "themeSimplifiedPath_" + themePath2;
			//themePaths[1] = null == themePath3 ? null : "themeSimplifiedPath_" + themePath3;
			//int dependencyPathLength = dependencyExtractor.getDijkstraShortestPathLength(
			//		themeTokens.get(1), themeTokens.get(0));
			//themePaths[2] = "dependencyPathLength_" + String.valueOf(dependencyPathLength) ;
		}
		featuresString.add(themePaths);
		
/*		String[] themeTextBetween = new String[2];
		if (themeTokens.size() == 1) {
			themeTextBetween[0] = "themeSize=1";
			themeTextBetween[1] = null;
			//themeTextBetween[2] = null;
		}else if (themeTokens.size() == 2) {
			int start = Math.min(themeTokens.get(0).getBegin(),
					themeTokens.get(1).getBegin());
			int end = Math.max(themeTokens.get(0).getEnd(), themeTokens.get(1).getEnd());

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
				textBetween = textBetween.equals("") ? aText : textBetween
						.concat("_").concat(aText);				
			}
			for (String aText : tokensAbsTextBetween) {
				textAbsBetween = textAbsBetween.equals("") ? aText
						: textAbsBetween.concat("_").concat(aText);				
			}
			themeTextBetween[0] = null == textBetween ? null : "themeTextBetween_" + textBetween;
			themeTextBetween[1] = null == textAbsBetween ? null : "themeTextAbsBetween_" + textAbsBetween;
			int tokensTextBetweenLength = tokensTextBetween.size();
			//themeTextBetween[2] = "tokensTextBetweenLength_" + String.valueOf(tokensTextBetweenLength);
		}
		featuresString.add(themeTextBetween);*/
		
		String triggerText = "text_".concat(triggerToken.getCoveredText()
				.toLowerCase());
		String triggerLemma = "triggerLemma_".concat(triggerToken.getLemma()
				.toLowerCase());
		triggerLemma = (null == triggerToken.getSubLemma() ? triggerToken
				.getLemma() : "triggerLemma_".concat(triggerToken
				.getSubLemma().toLowerCase()));
		String triggerPos = "triggerPos_".concat(triggerToken.getPos());
		String triggerPosShort = "triggerShortPos_".concat(triggerToken
				.getPos().substring(0, 1));

		// parser : dependency path between trigger-argument
		int i = 0;
		String[] dependencyPaths = new String[themeTokens.size()];
		//String[] pathLength = new String[themeTokens.size()];
		String[] simplifiedFeaturePaths = new String[themeTokens.size()];
		String[] triggerTextPaths = new String[themeTokens.size()];
		String[] triggerLemmaPaths = new String[themeTokens.size()];		
		String[] triggerLemmaSimplifiedPaths = new String[themeTokens.size()];
		String[] triggerPosPaths = new String[themeTokens.size()];
		String[] triggerPosShortPaths = new String[themeTokens.size()];
		String[] textBetweens = new String[themeTokens.size()];
		//String[] textBetweenLength = new String[themeTokens.size()];
		//String[] triggerLemmaBetweens = new String[themeTokens.size()];
		String[] textBetweenDependencies = new String[themeTokens.size()];
		String[] textAbsBetweenDependencies = new String[themeTokens.size()];
		//String[] textShortBetweens = new String[themeTokens.size()];
		//String[] textShortBetweenDependencyPaths = new String[themeTokens.size()];
		for (Token aThemeToken : themeTokens) {
			/*int triggerPathLength = dependencyExtractor.getDijkstraShortestPathLength(
					triggerToken, aThemeToken);
			pathLength[i] = "triggerPathLength_" + String.valueOf(triggerPathLength);
			if (i==1 && pathLength[1].equals(pathLength[0])) {
				pathLength[i] = pathLength[i] + "twice";
			}*/
			String featurePath = dependencyExtractor.getShortestPath(
					triggerToken, aThemeToken, Stage.BINDING);
			boolean areSameTokens = (aThemeToken.getId() == triggerToken.getId());
			featurePath = areSameTokens ? "SAMETOKEN" : featurePath;
			featurePath = (null == featurePath ? null : "featurePath_".concat(featurePath));
			if (null != featurePath) {
				for (int m=0; m<i; m++) {
					if (null != dependencyPaths[m]
							&& dependencyPaths[m].equalsIgnoreCase(featurePath)) {
						featurePath += "_twice";
					}
				}
			}
			dependencyPaths[i] = featurePath;

			String simplifiedFeaturePath = dependencyExtractor
					.getSimplifiedShortestPath(triggerToken, aThemeToken, Stage.BINDING);
			// parser refined?

			// parser_simple: grouping of dpendency type;
			// amod, nn --> nmod
			// anything ending in subj --> subj
			// anything ending in subjpass --> subjpass
			
			simplifiedFeaturePath = areSameTokens ? "SAMETOKEN" : simplifiedFeaturePath;
			simplifiedFeaturePath = (null == simplifiedFeaturePath ? null
					: "simplifiedFeaturePath_".concat(simplifiedFeaturePath));
			if (null != simplifiedFeaturePath) {
				for (int m=0; m<i; m++) {
					if (null != simplifiedFeaturePaths[m]
							&& simplifiedFeaturePaths[m].equalsIgnoreCase(simplifiedFeaturePath)) {
						simplifiedFeaturePath += "_twice";
					}
				}
			}
			simplifiedFeaturePaths[i] = simplifiedFeaturePath;

			triggerTextPaths[i] = null == featurePath ? null : triggerText
					.concat("_").concat(featurePath);

			triggerLemmaPaths[i] = null == featurePath ? null : triggerLemma
					.concat("_").concat(featurePath);
			triggerLemmaSimplifiedPaths[i] = null == featurePath ? null
					: triggerLemma.concat("_").concat(featurePath);

			triggerPosPaths[i] = null == featurePath ? null : triggerPos
					.concat("_").concat(featurePath);
			triggerPosShortPaths[i] = null == featurePath ? null
					: triggerPosShort.concat("_").concat(featurePath);

			// text string from trigger to theme/cause: compensate when parsing
			// fails
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
			if (null != textBetween) {
				for (int m=0; m<i; m++) {
					if (null != textBetweens[m]
							&& textBetweens[m].equalsIgnoreCase(textBetween)) {
						textBetween += "_twice";
					}
				}
			}
			textBetweens[i] = textBetween.equals("") ? null : "textString_"
					.concat(reversed ? "reversed_" : "").concat(textBetween);

			//triggerLemmaBetweens[i] = null == textBetween ? null : triggerLemma
			//		.concat("_").concat(textBetween);
			textBetweenDependencies[i] = null != textBetween
					&& null != featurePath ? featurePath.concat("_")
					.concat(textBetween) : null;
			if (null != textAbsBetween) {
				for (int m=0; m<i; m++) {
					if (null != textAbsBetweenDependencies[m]
							&& textAbsBetweenDependencies[m].equalsIgnoreCase(textAbsBetween)) {
						textAbsBetween += "_twice";
					}
				}
			}
			textAbsBetweenDependencies[i] = textAbsBetween.equals("") ? null
					: "textStringAbs_".concat(reversed ? "reversed_" : "")
							.concat(textAbsBetween) + featurePath;
			/*int triggerTextBetweenLength = tokensTextBetween.size();
			textBetweenLength[i] = "triggerTextBetweenLength_" + String.valueOf(triggerTextBetweenLength);
			if (i==1 && textBetweenLength[1].equals(textBetweenLength[0])) {
				textBetweenLength[i] = textBetweenLength[i] + "twice";
			}
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
			if (null != textShortBetween) {
				for (int m=0; m<i; m++) {
					if (null != textShortBetweens[m]
							&& textShortBetweens[m].equalsIgnoreCase(textShortBetween)) {
						textShortBetween += "_twice";
					}
				}
			}
			textShortBetweens[i] = textShortBetween.equals("") ? null
					: "textStringShort_".concat(reversed ? "reversed_" : "")
							.concat(textShortBetween);
			textShortBetweenDependencyPaths[i] = null != textShortBetween
					&& null != featurePath ? featurePath.concat("_")
					.concat(textShortBetween) : null;*/
    		i++;
		}

		featuresString.add(dependencyPaths);
		featuresString.add(simplifiedFeaturePaths);
		
		//featuresString.add(pathLength);
		//featuresString.add(textBetweenLength);
		
		// trigger token & trigger type
		//featuresString.add(triggerTextPaths);

		// trigger lemma
		//featuresString.add(new String[] { triggerLemma });
		featuresString.add(triggerLemmaPaths);
		//featuresString.add(triggerLemmaSimplifiedPaths);

		// trigger POS
		//featuresString.add(triggerPosPaths);
		//featuresString.add(new String[] { triggerPosShort });
		featuresString.add(triggerPosShortPaths);

		/*featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerLemma.concat("_").concat(
				triggerPosShort) });*/
		/*featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPos) });
		featuresString.add(new String[] { triggerSubLemma.concat("_").concat(
				triggerPosShort) });*/

		// concatenate text between trigger and theme/cause with the previous
		// features.
		featuresString.add(textBetweens);
		//featuresString.add(triggerLemmaBetweens);
		featuresString.add(textBetweenDependencies);
		featuresString.add(textAbsBetweenDependencies);

		//featuresString.add(textShortBetweens);
		//featuresString.add(textShortBetweenDependencyPaths);

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

	/**
	 * Only Binding may have more than one theme, but it doesn't have cause.
	 * Therefore, there is only one returned theme token.
	 * 
	 * @param event
	 * @return
	 */
	protected Token getThemeToken(JCas jcas, Event event, Sentence sentence) {

		String themeId = event.getThemes(0);

		for (Protein protein : JCasUtil.selectCovered(jcas, Protein.class,
				sentence)) {
			if (themeId.equals(protein.getId())) {
				return getProteinToken(jcas, protein);
			}
		}
		for (Event anEvent : JCasUtil
				.selectCovered(jcas, Event.class, sentence)) {
			// an event can't be the theme of itself
			if (event.getBegin() == anEvent.getBegin()
					&& event.getEnd() == anEvent.getEnd()) {
				continue;
			}
			if (themeId.equals(anEvent.getId())) {
				return getTriggerToken(jcas, anEvent.getTrigger());
			}
		}
		return null;
	}

	protected Token getProteinToken(JCas jcas, Protein protein) {
		// find the protein token
		List<Token> proteinTokens = JCasUtil.selectCovered(jcas, Token.class,
				protein);

		// if protein is within a token
		if (proteinTokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(Token.type)
					.iterator();
			proteinTokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token aToken = (Token) iter.next();
				if (aToken.getBegin() <= protein.getBegin()
						&& aToken.getEnd() >= protein.getEnd()) {
					proteinTokens.add(aToken);
					break;
				}
			}
		}
		if (proteinTokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(
					Token.type).iterator();
			proteinTokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() < protein.getBegin()
						&& token.getEnd() > protein.getBegin()) {
					proteinTokens.add(token);
					break;
				}
			}
		}

		if (proteinTokens.size() == 0) {
			logger.warning("No token found for protein.");
			return null;
		}

		Token token = proteinTokens.get(0);						
		for (Token aToken : proteinTokens) {

			try {
				Double.parseDouble(aToken.getLemma());
				break;
			} catch (NumberFormatException e) {
				token = aToken;
			}
		}
		return token;
	}
	
	protected Token getToken(JCas jcas, Annotation annotation) {
		
		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
				annotation);
		// if protein/trigger is within a token
		if (tokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(
					Token.type).iterator();
			tokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= annotation.getBegin()
						&& token.getEnd() >= annotation.getEnd()) {
					tokens.add(token);
					break;
				}
			}
		}
		if (tokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(
					Token.type).iterator();
			tokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() < annotation.getBegin()
						&& token.getEnd() > annotation.getBegin()) {
					tokens.add(token);
					break;
				}
			}
		}
		if (tokens.size() == 0) {
			logger.warning("No token found for annotation.");
			return null;
		}

		Token token = tokens.get(0);						
		for (Token aToken : tokens) {

			try {
				Double.parseDouble(aToken.getLemma());
				break;
			} catch (NumberFormatException e) {
				token = aToken;
			}
		}
		return token;
	}

}
