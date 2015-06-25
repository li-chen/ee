package info.chenli.litway.corpora;

import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.ConnlxReader;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.UimaUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class BioNLPSyntacticAnnotator extends JCasAnnotator_ImplBase {

	private final static Logger logger = Logger
			.getLogger(BioNLPSyntacticAnnotator.class.getName());
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		File sentencisedFile = new File(FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas)).concat(".ss"));
		if (!sentencisedFile.exists()) {
			sentencisedFile = new File(FileUtil.removeFileNameExtension(
					UimaUtil.getJCasFilePath(jcas)).concat(".tok"));
		}
		File tokenisedFile = new File(FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas)).concat(".tok"));
		List<ConnlxReader.Token> tokens = new LinkedList<ConnlxReader.Token>();
		if (new File(FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas)).concat(".connlx")).exists()) {
			tokens = ConnlxReader
					.getTokens(new File(FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jcas)).concat(".connlx")));
		} else {
			tokens = ConnlxReader
					.getTokens(new File(FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jcas)).concat(".conll")));
		}
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
		Token leftToken = null;
		try {

			// get the stream of the original text
			InputStreamReader originalTextStream = new InputStreamReader(
					new ByteArrayInputStream(jcas.getDocumentText().getBytes()));
			InputStreamReader originalTextStream2 = new InputStreamReader(
					new ByteArrayInputStream(jcas.getDocumentText().getBytes()));
			// get the stream of the sentencised text
			InputStreamReader sentencisedFileStream = new InputStreamReader(
					new FileInputStream(sentencisedFile), "UTF8");

			// get the stream of the tokenised text
			InputStreamReader tokenisedFileStream = new InputStreamReader(
					new FileInputStream(tokenisedFile), "UTF8");

			int originalTextCh, originalTextCh2 = 0, sentencisedTextCh, tokenisedTextCh, offset = 0, sentenceBegin = 0, tokenBegin = 0;

			Iterator<ConnlxReader.Token> tokenItor = tokens.iterator();
			

			// token map of each sentence
			TreeMap<Integer, Token> tokensOfSentence = new TreeMap<Integer, Token>();
			int sentenceId = 0;
			originalTextStream2.read();

			while ((originalTextCh = originalTextStream.read()) != -1) {
				originalTextCh2 = originalTextStream2.read();
				Character originalTextChar = (char) originalTextCh;
				if (originalTextChar == ' ' && originalTextCh2 != -1) {
					Character originalTextChar2 = (char) originalTextCh2;
					if (originalTextChar2 == System.getProperty(
								"line.separator").charAt(0)) {
						offset++;
						continue;
					}
				}
				
				//
				// Tokens
				//

				if ((tokenisedTextCh = tokenisedFileStream.read()) != -1) {
					Character tokenisedFileChar = (char) tokenisedTextCh;
					if (tokenisedFileChar == ' '
							|| tokenisedFileChar == System.getProperty(
									"line.separator").charAt(0)) {

						List<Token> tokenss = fetchToken(jcas, tokenBegin, offset,
								tokenItor, leftToken, UimaUtil.getJCasFilePath(jcas));
						Token token = tokenss.get(0);
						leftToken = tokenss.get(1);
						// put tokens in same sentence into a map.
						tokensOfSentence.put(token.getId(), token);
						 
						tokenBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							tokenBegin++;
						}else {
							tokenisedFileStream.read();
						}
					}
				}

				//
				// Sentences
				//
				if ((sentencisedTextCh = sentencisedFileStream.read()) != -1) {
					Character sentencisedFileChar = (char) sentencisedTextCh;
					if (sentencisedFileChar == ' ') {
						if (originalTextChar != ' ') {
							sentencisedFileStream.read();
						}
					}
					if (sentencisedFileChar == System.getProperty(
							"line.separator").charAt(0)) {

						fetchSentence(jcas, sentenceBegin, offset, sentenceId,
								pairsOfArticle.get(sentenceId));
						sentenceId++;

						tokensOfSentence = new TreeMap<Integer, Token>();

						sentenceBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							sentenceBegin++;
						}else {
							sentencisedFileStream.read();
						}
					}
				}

				offset++;
			}

			if (tokenItor.hasNext()) {
				fetchToken(jcas, tokenBegin, offset, tokenItor, leftToken, UimaUtil.getJCasFilePath(jcas));
			}
			fetchSentence(jcas, sentenceBegin, offset, sentenceId,
					pairsOfArticle.get(sentenceId));

			sentencisedFileStream.close();
			tokenisedFileStream.close();

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new RuntimeException(e);
		}
	}

	private List<Token> fetchToken(JCas jcas, int tokenBegin, int offset,
			Iterator<ConnlxReader.Token> tokenItor, Token leftToken, String fileName) {
		// the last token is missed due to reaching the end of the file.
		/*System.out.println(tokenBegin);
		if (null != leftToken) {
			System.out.println(leftToken.getCoveredText());
		}*/
		ConnlxReader.Token connlxToken = tokenItor.next();
		String pos = connlxToken.getPos();
		Token token = createNewToken(jcas, tokenBegin, offset, pos);
		token.setId(connlxToken.getId());
		//token.setStem(fileName);
		
		token.setLeftToken(leftToken);
		if (null != leftToken) {
			leftToken.setRightToken(token);
		}
		leftToken = token;
		token.addToIndexes();
        
		List<Token> tokenss = new ArrayList<Token>(); 
		tokenss.add(token);
		tokenss.add(leftToken);
		return tokenss;

	}

	private void fetchSentence(JCas jcas, int sentenceBegin, int offset,
			int sentenceId, Set<Pair> pairsOfSentence) {
		Sentence sentence = new Sentence(jcas, sentenceBegin, offset);
		sentence.setId(sentenceId);
		sentence.addToIndexes();

	}

	private Token createNewToken(JCas jcas, int begin, int end, String pos) {

		Token token = new Token(jcas, begin, end);

		if (token.getCoveredText().length() > 1) {
			if (token.getCoveredText().charAt(0) == '-' || 
					token.getCoveredText().charAt(0) == '.' ||
					token.getCoveredText().charAt(0) == '#' ||
					token.getCoveredText().charAt(0) == '/') {
				token.setBegin(token.getBegin() + 1);
			}
		}
		
		if (token.getCoveredText().length() > 2) {
			if (token.getCoveredText().charAt(0) == '(' && 
				token.getCoveredText().
				charAt(token.getCoveredText().length()-1) == ')') {
				token.setBegin(token.getBegin() + 1);
				token.setEnd(token.getEnd() - 1);
			}
		}

		

		token.setPos(pos);
		String text = token.getCoveredText();
		String lemma = BioLemmatizerUtil.lemmatizeWord(text.toLowerCase(),
				token.getPos());
		token.setLemma(lemma);
		Stemmer stem = new Stemmer();
		stem.add(text.toCharArray(), text.length());
		stem.stem();
		token.setStem(stem.toString());

		String subWord = null, subLemma = lemma, subStem = stem.toString();
		if (token.getCoveredText().indexOf("-") > -1) {
			subWord = token.getCoveredText().substring(
					token.getCoveredText().lastIndexOf("-") + 1);
			subLemma = BioLemmatizerUtil.lemmatizeWord(subWord, pos);
			Stemmer stemmer2 = new Stemmer();
			stemmer2.add(subWord.toCharArray(), subWord.length());
			stemmer2.stem();
			subStem = stemmer2.toString();
		}
		token.setSubLemma(subLemma);
		token.setSubStem(subStem);
		
		return token;
	}

}