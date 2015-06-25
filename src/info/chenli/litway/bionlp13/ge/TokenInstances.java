package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class TokenInstances extends AbstractInstances {

	private final static String classifierName = "liblinear";

	private final static Logger logger = Logger.getLogger(TokenInstances.class
			.getName());

	public TokenInstances() {

		super(new int[] { Token.type });

	}

	@Override
	protected List<String> getLabelsString() {

		ArrayList<String> tokenTypes = new ArrayList<String>();

		for (EventType eventType : EventType.values()) {
			tokenTypes.add(String.valueOf(eventType));
		}

		return tokenTypes;
	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> tokenIter) {

		File word2vecFile = new File("./word2vec/word2vec100");
		//File word2vecFile = new File("/home/songrq/word2vec/data/word2vec100");
		Map<String,double[]> word2vec = ReadWord2vec.word2vec(word2vecFile); 

		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		
		AnnotationIndex<Annotation> sentenceIndex = jcas
				.getAnnotationIndex(Sentence.type);

		FSIterator<Annotation> sentenceIter = sentenceIndex.iterator();
		Map<Integer, Set<Pair>> pairsOfArticle = new HashMap<Integer, Set<Pair>>();
		if (new File(FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")).exists()) {
			pairsOfArticle = StanfordDependencyReader
					.getPairs(new File(FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));
		} else {
			pairsOfArticle = StanfordDependencyReader
					.getPairs(new File(FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jcas)).concat(".sd")));
		}
		
		
		
		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			LinkedList<Instance> nodes = new LinkedList<Instance>();
			si.setNodes(nodes);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());
			
			List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			
			if (sentenceProteins.size() <= 0) {
				continue;
			}
			
			
			List<Trigger> triggers = JCasUtil.selectCovered(jcas,
					Trigger.class, sentence);

			// token id and event type
			Map<Integer, String> triggerTokens = new HashMap<Integer, String>();

			// mark trigger tokens
			for (Trigger trigger : triggers) {
				triggerTokens.put(getTriggerToken(jcas, trigger).getId(),
						trigger.getEventType());
			}

			List<Token> tokensOfSentence = JCasUtil.selectCovered(jcas,
					Token.class, sentence);
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);
			 for (Token token : tokensOfSentence) {

				Instance instance = new Instance();
				instance = tokenToInstance(jcas, token, triggerTokens,
						tokensOfSentence, sentenceProteins, pairsOfSentence,
						dependencyExtractor, word2vec);
				if (instance != null) {
					instance.setFileId(FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jcas)).concat(".word2vec"));
					nodes.add(instance);
				}
				
			}

			results.add(si);
		}
		
		//System.out.println(posSet.toString());
		return results;
	}

	/**
	 * 
	 * @param jcas
	 * @param token
	 * @param triggerTokens
	 *            Used for extracting instances from training set. null when
	 *            being used for testing set.
	 * @param sentenceProteins
	 * @param dependencyExtractor
	 * @return
	 */
	/**
	 * @param jcas
	 * @param token
	 * @param triggerTokens
	 * @param tokensOfSentence
	 * @param sentenceProteins
	 * @param pairsOfSentence
	 * @param dependencyExtractor
	 * @return
	 */
	protected Instance tokenToInstance(JCas jcas, Token token,
			Map<Integer, String> triggerTokens, List<Token> tokensOfSentence,
			List<Protein> sentenceProteins, Set<Pair> pairsOfSentence,
			DependencyExtractor dependencyExtractor, Map<String,double[]> word2vec) {
		
		if(isProtein(token, sentenceProteins)) {
			return null;
		}
		String pos2 = token.getPos();
		if (!POS.isPos(pos2)) {
			return null;
		}
		if(pos2.equals("EX")	| pos2.equals("LS")
				| pos2.equals("NNP") | pos2.equals("PRP")
				| pos2.equals("MD") | pos2.equals("NNPS")
			 	| pos2.equals("WDT")
				| pos2.equals("PRP$") | pos2.equals("PDT")
				| pos2.equals("SYM") | pos2.equals("RP")
				| pos2.equals("FW") | pos2.equals("POS")
				| pos2.equals("WP") | pos2.equals("RBS")
				| pos2.equals("VH") | pos2.equals("WP$")
				) {
			return null;
		}

		if(token.getCoveredText().equals("%") | token.getCoveredText().equals("#")
				| token.getCoveredText().equals("\"") | token.getCoveredText().equals("\'")
				| token.getCoveredText().equals("&") | token.getCoveredText().equals("+")
				| token.getCoveredText().equals("-") | token.getCoveredText().equals("/")
				| token.getCoveredText().equals("[") | token.getCoveredText().equals("]")
				| token.getCoveredText().equals("(") | token.getCoveredText().equals(")")
				| token.getCoveredText().equals("<") | token.getCoveredText().equals(":") 
				| token.getCoveredText().equals(";")
				| token.getCoveredText().equals(",") | token.getCoveredText().equals(".")
				| token.getCoveredText().equals("?") | token.getCoveredText().equals("\\")
				| token.getCoveredText().equals("|") | token.getCoveredText().equals("{")
				| token.getCoveredText().equals("}") | token.getCoveredText().equals(">")
				| token.getCoveredText().equals("*") | token.getCoveredText().equals("=")
				| token.getCoveredText().equals("^") | token.getCoveredText().equals("$")
				| token.getCoveredText().equals("@") | token.getCoveredText().equals("!")
				| token.getCoveredText().equals("~") | token.getCoveredText().equals("`")) {
			return null;
		}
		
		String s = token.getCoveredText();
		String s1 = "+-/*()[]{}?!:;\\\"\'.<>,@#￥%^&_|=$~`";
		boolean b = true;
		for (int i=0; i<s.length(); i++) {
			if (!isDigit(s.charAt(i)) && !s1.contains(String.valueOf(s.charAt(i)))) {
				b = false;
				break;
			}
		}
		if (b == true) {
			return null;
		}

		Instance instance = new Instance();

		int tokenBegin = token.getBegin();
		int tokenEnd = token.getEnd();
		token = containsProtein(token, sentenceProteins);
		token.setBegin(tokenBegin);
		token.setEnd(tokenEnd);
		String lemma2 = token.getLemma();
		double[] fs = new double[100];
		double[] fs2 = new double[100];
		double[] fs3 = new double[100];
		double[] fs4 = new double[100];
		double[] fs5 = new double[100];
		double[] fs0 = new double[500];
		
		if(word2vec.containsKey(lemma2)
				|| word2vec.containsKey(lemma2.toUpperCase())) {
			if (word2vec.containsKey(lemma2)) {
				fs = word2vec.get(lemma2);
			}else {
				fs = word2vec.get(lemma2.toUpperCase());
			}
			instance.setFeaturesNumericWord2vec(fs);
		}
		
		// only consider the tokens, which are words.
		
		
		List<String[]> featureString = new ArrayList<String[]>();
		instance.setFeaturesString(featureString);

		featureString.add(new String[] { "text_".concat(token.getCoveredText()
			.toLowerCase()) });
		
		//String isPro = isProtein(token, sentenceProteins) ? token.getCoveredText()
		//		.toLowerCase() : null;
		//featureString.add(null == isPro ? new String[0]
		//		: new String[] { "proText_".concat("_").concat(isPro) });
		
		String lemma = "lemma_".concat(token.getLemma().toLowerCase());
		featureString.add(new String[] { lemma });
		String stem = "stem_".concat(token.getStem().toLowerCase());
		//featureString.add(new String[] { stem });
		String pos = "pos_".concat(token.getPos());
		//featureString.add(new String[] { lemma.concat("_").concat(pos) });
		featureString.add(new String[] { pos });
		 
		if (!token.getSubLemma().equals(token.getLemma())) {
			String subLemma = "sublemma_"
				.concat(token.getSubLemma().toLowerCase());
			featureString.add(new String[] { subLemma });
		}else {
			featureString.add(new String[0]);
		}
		
		if (!token.getSubStem().equals(token.getStem())) {
			String subStem = "substem_"
					.concat(token.getSubStem().toLowerCase());
			featureString.add(new String[] { subStem });
		}else {
			featureString.add(new String[0]);
		}

		List<String> modifiers = new ArrayList<String>();
		List<String> heads = new ArrayList<String>();
		List<String> noLemmaModifiers = new ArrayList<String>();
		List<String> noLemmaHeads = new ArrayList<String>();
		List<String> noDepModifiers = new ArrayList<String>();
		List<String> noDepHeads = new ArrayList<String>();
		List<String> nsubjList = new ArrayList<String>();
		List<String> dobjList = new ArrayList<String>();
		List<String> iobjList = new ArrayList<String>();
		for (Pair pair : pairsOfSentence) {
			if (pair.getRelation().equalsIgnoreCase("punct")) {
				continue;
			}
			if (pair.getHead() == token.getId()) {
				for (Token aToken : tokensOfSentence) {
					if (aToken.getId() == pair.getModifier()) {
						
						tokenBegin = aToken.getBegin();
						tokenEnd = aToken.getEnd();
						aToken = containsProtein(aToken, sentenceProteins);
						aToken.setBegin(tokenBegin);
						aToken.setEnd(tokenEnd);
						lemma2 = aToken.getLemma();
						if(isProtein(aToken, sentenceProteins)) {
							lemma2 = "PROTEIN";
						}
						if(word2vec.containsKey(lemma2)
								|| word2vec.containsKey(lemma2.toUpperCase())) {
							if (word2vec.containsKey(lemma2)) {
								fs2 = word2vec.get(lemma2);
							}else {
								fs2 = word2vec.get(lemma2.toUpperCase());
							}
							//instance.setFeaturesNumericWord2vec(fs);
						}

						String tokenLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
								: aToken.getLemma().toLowerCase();
						modifiers.add(lemma.concat("_")
								.concat(pair.getRelation()).concat("_lemma_")
								.concat(tokenLemma));
						noLemmaModifiers.add(pair.getRelation()
								.concat("_lemma_").concat(tokenLemma));
						
						if (!aToken.getSubLemma().equals(aToken.getLemma())) {
							String subLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
									: aToken.getSubLemma().toLowerCase();
							noLemmaModifiers.add(pair.getRelation()
									.concat("_sublemma_").concat(subLemma));
						}
						noDepModifiers.add(lemma.concat("_lemma_").concat(
								tokenLemma));

						// if (pair.getRelation().equalsIgnoreCase("nsubj")) {
						// nsubjList.add("nsubj_lemma_".concat(aToken
						// .getLemma()));
						// }
						// if (pair.getRelation().equalsIgnoreCase("dobj")) {
						// dobjList.add("dobj_lemma_".concat(aToken.getLemma()));
						// }
						// if (pair.getRelation().equalsIgnoreCase("iobj")) {
						// iobjList.add("iobj_lemma_".concat(aToken.getLemma()));
						// }
					}
				}
			} else if (pair.getModifier() == token.getId()) {
				for (Token aToken : tokensOfSentence) {
					if (aToken.getId() == pair.getHead()) {
						tokenBegin = aToken.getBegin();
						tokenEnd = aToken.getEnd();
						aToken = containsProtein(aToken, sentenceProteins);
						aToken.setBegin(tokenBegin);
						aToken.setEnd(tokenEnd);
						lemma2 = aToken.getLemma();
						if(isProtein(aToken, sentenceProteins)) {
							lemma2 = "PROTEIN";
						}
						if(word2vec.containsKey(lemma2)
								|| word2vec.containsKey(lemma2.toUpperCase())) {
							if (word2vec.containsKey(lemma2)) {
								fs3 = word2vec.get(lemma2);
							}else {
								fs3 = word2vec.get(lemma2.toUpperCase());
							}
							//instance.setFeaturesNumericWord2vec(fs);
						}
						
						String tokenLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
								: aToken.getLemma().toLowerCase();
						heads.add(lemma.concat("_-").concat(pair.getRelation())
								.concat("_lemma_")
								.concat(tokenLemma));
						noLemmaHeads.add(pair.getRelation().concat("_lemma_")
								.concat(tokenLemma));
						
						if (!aToken.getSubLemma().equals(aToken.getLemma())) {
							String subLemma = isProtein(aToken, sentenceProteins) ? "PROTEIN"
									: aToken.getSubLemma().toLowerCase();
							noLemmaHeads.add(pair.getRelation().concat("_sublemma_")
									.concat(subLemma));
						}
						noDepHeads.add(lemma.concat("_-").concat("_lemma_")
								.concat(tokenLemma));
					}
				}
			}
		}
		String[] modifiersFeature = new String[modifiers.size()];
		modifiersFeature = modifiers.toArray(modifiersFeature);
		String[] headsFeature = new String[heads.size()];
		headsFeature = heads.toArray(headsFeature);
		String[] noLemmaModifiersFeature = new String[noLemmaModifiers.size()];
		noLemmaModifiersFeature = noLemmaModifiers
				.toArray(noLemmaModifiersFeature);
		String[] noLemmaHeadsFeature = new String[noLemmaHeads.size()];
		noLemmaHeadsFeature = noLemmaHeads.toArray(noLemmaHeadsFeature);
		String[] noDepModifiersFeature = new String[noDepModifiers.size()];
		noDepModifiersFeature = noDepModifiers.toArray(noDepModifiersFeature);
		String[] noDepHeadsFeature = new String[noDepHeads.size()];
		noDepHeadsFeature = noDepHeads.toArray(noDepHeadsFeature);
		String[] nsubjFeature = new String[nsubjList.size()];
		nsubjFeature = nsubjList.toArray(nsubjFeature);
		String[] dobjFeature = new String[dobjList.size()];
		dobjFeature = dobjList.toArray(dobjFeature);
		String[] iobjFeature = new String[iobjList.size()];
		iobjFeature = iobjList.toArray(iobjFeature);

		//featureString.add(modifiersFeature);
		//featureString.add(headsFeature);
		 featureString.add(noLemmaModifiersFeature);
		 featureString.add(noLemmaHeadsFeature);
		// featureString.add(noDepModifiersFeature);
		// featureString.add(noDepHeadsFeature);
		// featureString.add(nsubjFeature);
		// featureString.add(dobjFeature);
		// featureString.add(iobjFeature);

		//
		// ngram
		// previous word
		String leftTokenStr = token.getLeftToken() == null ? null : (POS
				.isPos(token.getLeftToken().getPos()) ? 
				isProtein(token.getLeftToken(), sentenceProteins) ? "PROTEIN":		
				"previousWord_".concat(token.getLeftToken().getLemma()) : null);
		
		String leftTokenSubLemma = null;
		if (token.getLeftToken() != null) {
			Token leftToken = token.getLeftToken();
			tokenBegin = leftToken.getBegin();
			tokenEnd = leftToken.getEnd();
			leftToken = containsProtein(leftToken, sentenceProteins);
			leftToken.setBegin(tokenBegin);
			leftToken.setEnd(tokenEnd);
			lemma2 = leftToken.getLemma();
			if(isProtein(leftToken, sentenceProteins)) {
				lemma2 = "PROTEIN";
			}
			if(word2vec.containsKey(lemma2)
					|| word2vec.containsKey(lemma2.toUpperCase())) {
				if (word2vec.containsKey(lemma2)) {
					fs4 = word2vec.get(lemma2);
				}else {
					fs4 = word2vec.get(lemma2.toUpperCase());
				}
				//instance.setFeaturesNumericWord2vec(fs);
			}
			
			if (!token.getLeftToken().getSubLemma().equals(token.getLeftToken().getLemma())) {
				leftTokenSubLemma = isProtein(token.getLeftToken(), sentenceProteins) ? "PROTEIN"
						: token.getLeftToken().getSubLemma().toLowerCase();
			}
		}
		 featureString.add(null == leftTokenStr ? new String[0]
		 : new String[] { leftTokenStr });
		 featureString.add(null == leftTokenSubLemma ? new String[0]
				 : new String[] { leftTokenSubLemma });
		//featureString.add(null == leftTokenStr ? new String[0]
		//: new String[] { lemma.concat("_").concat(leftTokenStr) });
		String posLeftTokenStr = token.getLeftToken() == null ? null : ((token
				.getLeftToken().getPos().indexOf("NN") > -1
				|| token.getLeftToken().getPos().indexOf("JJ") > -1 || token
				.getLeftToken().getPos().indexOf("V") > -1) ? lemma
				+ "_previousWord_".concat(token.getLeftToken().getLemma())
				: null);
		// featureString.add(null == posLeftTokenStr ? new String[0]
		// : new String[] { posLeftTokenStr });
		// after word
		String rightTokenStr = token.getRightToken() == null ? null : (POS
				.isPos(token.getRightToken().getPos()) ? 
				isProtein(token.getRightToken(), sentenceProteins) ? "PROTEIN":
				"afterWord_".concat(token.getRightToken().getLemma()) : null);
		
		String rightTokenSubLemma = null;
		if (token.getRightToken() != null) {
			Token leftToken = token.getRightToken();
			tokenBegin = leftToken.getBegin();
			tokenEnd = leftToken.getEnd();
			leftToken = containsProtein(leftToken, sentenceProteins);
			leftToken.setBegin(tokenBegin);
			leftToken.setEnd(tokenEnd);
			lemma2 = leftToken.getLemma();
			if(isProtein(leftToken, sentenceProteins)) {
				lemma2 = "PROTEIN";
			}
			if(word2vec.containsKey(lemma2)
					|| word2vec.containsKey(lemma2.toUpperCase())) {
				if (word2vec.containsKey(lemma2)) {
					fs5 = word2vec.get(lemma2);
				}else {
					fs5 = word2vec.get(lemma2.toUpperCase());
				}
				//instance.setFeaturesNumericWord2vec(fs);
			}
			
			if (!token.getRightToken().getSubLemma().equals(token.getRightToken().getLemma())) {
				rightTokenSubLemma = isProtein(token.getRightToken(), sentenceProteins) ? "PROTEIN"
						: token.getRightToken().getSubLemma().toLowerCase();
			}
		}
		 featureString.add(null == rightTokenStr ? new String[0]
				 : new String[] { rightTokenStr });
		 featureString.add(null == rightTokenSubLemma ? new String[0]
				 : new String[] { rightTokenSubLemma });
		//featureString.add(null == rightTokenStr ? new String[0]
		//: new String[] { lemma.concat("_").concat(rightTokenStr) });
		
		String posRightTokenStr = token.getRightToken() == null ? null : ((token
				.getRightToken().getPos().indexOf("NN") > -1) ? lemma
				+ "_afterWord_".concat(token.getRightToken().getLemma()) : null);
		
		// featureString.add(null == posRightTokenStr ? new String[0]
		// : new String[] { posRightTokenStr });

		// protein in the sentence
		String[] proteins = new String[sentenceProteins.size()];
		String[] proteinsDummy = sentenceProteins.size() > 0 ? new String[] { "PROTEIN" }
				: new String[0];
		String[] proteinsLemma = new String[sentenceProteins.size()];
		String[] proteinsDep = new String[sentenceProteins.size()];

		int i = 0;
		for (Protein protein : sentenceProteins) {

			Token aProteinToken = getProteinToken(jcas, protein);

			proteins[i] = "protein_"
					+ protein.getCoveredText().toLowerCase()
							.replaceAll(" ", "_");
			proteinsLemma[i] = lemma.concat("_").concat(
					protein.getCoveredText().toLowerCase());

			proteinsDep[i] = dependencyExtractor.getShortestPath(token,
					aProteinToken, Stage.TRIGGER);

			if (null == proteinsDep[i]) {
				proteinsDep[i] = dependencyExtractor.getReversedShortestPath(
						token, aProteinToken, Stage.TRIGGER);
			}
		}
		// featureString.add(proteins);
		// featureString.add(proteinsDummy);
		// featureString.add(proteinsLemma);
		boolean isDepNull = true;
		for (String dep : proteinsDep) {
			if (null != dep) {
				isDepNull = false;
				break;
			}
		}
		// featureString.add(isDepNull ? new String[0] : proteinsDep);
		/*System.arraycopy(fs, 0, fs0, 0, 100);
		System.arraycopy(fs2, 0, fs0, 100, 100);
		System.arraycopy(fs3, 0, fs0, 200, 100);
		System.arraycopy(fs4, 0, fs0, 300, 100);
		System.arraycopy(fs5, 0, fs0, 400, 100);
		instance.setFeaturesNumericWord2vec(fs0);*/
		if (null != triggerTokens) {

			instance.setLabelString(triggerTokens.containsKey(token.getId()) ? triggerTokens
					.get(token.getId()) : String.valueOf(EventType.Non_trigger));
		} else {
			instance.setLabelString(String.valueOf(EventType.Non_trigger));
		}
		
		return instance;
	}

	private boolean isDigit(Character i) {
		if (i >= '0' && i <= '9') {
			return true;
		}else {
			return false;
		}
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		//List<Instance> instances = ti.getInstances(new File("/media/songrq/soft/litway/数据/"
		//		+ "BioNLP13/BioNLP-ST-2013_GE_train_data_yuanShuJu"));
		List<Instance> instances = ti.getInstances(new File("/media/songrq/soft/litway/数据/"
				+ "BioNLP13/BioNLP-ST-2013_GE_devel_data_yuanShuJu"));
		InstanceDictionary dict = new InstanceDictionary();
/*		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/triggers.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");*/
		dict.loadDictionary(new File("./model/triggers.".concat(classifierName)
				.concat(".dict")));
		dict.instancesToNumeric(instances);
		//File f = new File("./model/instances.trigger.svm.txt");
		File f = new File("./model/instances.trigger.svm.dev.txt");
		OutputStreamWriter word2vecFileStream;
		try {
			word2vecFileStream = new OutputStreamWriter(
					new FileOutputStream(f), "UTF8");
		
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
				String instancesStr = sb.toString();
				word2vecFileStream.write(instancesStr);
				sb.delete(0,sb.length());
			}
			word2vecFileStream.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected boolean isProtein(Token token, List<Protein> proteinsOfSentence) {
		List<Protein> proteins = new LinkedList<Protein>();
		for (Protein protein : proteinsOfSentence) {
			if (token.getBegin() >= protein.getBegin() && token.getEnd() <= protein
					.getEnd()) {
				return true;
			}else if (((protein.getBegin() >= token.getBegin() && protein
                    .getEnd() < token.getEnd()) ||
                    (protein.getBegin() > token.getBegin() && protein
                    .getEnd() <= token.getEnd()))) {
				proteins.add(protein);
			}
		}
        if (proteins.size() < 1) {
        	return false;
        }

		String tokenText = token.getCoveredText();
		for (Protein protein : proteins) {
			tokenText = tokenText.replace(protein.getCoveredText(), "");
		}
		tokenText = tokenText.replace("/", "");
		tokenText = tokenText.replace("-", "");
		if (tokenText.trim().equals("")) {
			return true;
		}
		return false;
	}

	protected Token containsProtein(Token token, List<Protein> proteinsOfSentence) {
		List<Protein> proteins = new LinkedList<Protein>();
        for (Protein protein : proteinsOfSentence) {
            if (((protein.getBegin() >= token.getBegin() && protein
                            .getEnd() < token.getEnd()) ||
                            (protein.getBegin() > token.getBegin() && protein
                            .getEnd() <= token.getEnd()))
                ) {
            	proteins.add(protein);
            }
        }
        if (proteins.size() < 1) {
        	return token;
        }

		String tokenText = token.getCoveredText();
		for (Protein protein : proteins) {
			tokenText = tokenText.replace(protein.getCoveredText(), " PROTEIN ");
		}
		tokenText = tokenText.replace("/", " ");
		tokenText = tokenText.replace("-", " ");		
		while(tokenText.contains("  ")) {
			tokenText = tokenText.replace("  ", " ");		
		}
		tokenText = tokenText.trim();
		String[] s = tokenText.split(" ");
		for (int i=s.length-1; i>=0; i--) {
			if (!s[i].equals("PROTEIN")) {
				tokenText = s[i];
				break;
			}
		}
		token.setBegin(token.getBegin() + token.getCoveredText().indexOf(tokenText));
		token.setEnd(token.getBegin() + tokenText.length());
		token.setLemma(tokenText);
        return token;
    }

	
}
