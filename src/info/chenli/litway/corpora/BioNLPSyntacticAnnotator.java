package info.chenli.litway.corpora;

import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.ConnlxReader;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.UimaUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
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
		File tokenisedFile = new File(FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas)).concat(".tok"));
		List<ConnlxReader.Token> tokens = ConnlxReader
				.getTokens(new File(FileUtil.removeFileNameExtension(
						UimaUtil.getJCasFilePath(jcas)).concat(".connlx")));

		try {

			// get the stream of the original text
			InputStreamReader originalTextStream = new InputStreamReader(
					new ByteArrayInputStream(jcas.getDocumentText().getBytes()));

			// get the stream of the sentencised text
			InputStreamReader sentencisedFileStream = new InputStreamReader(
					new FileInputStream(sentencisedFile), "UTF8");

			// get the stream of the tokenised text
			InputStreamReader tokenisedFileStream = new InputStreamReader(
					new FileInputStream(tokenisedFile), "UTF8");

			int originalTextCh, sentencisedTextCh, tokenisedTextCh, offset = 0, sentenceBegin = 0, tokenBegin = 0;

			Iterator<ConnlxReader.Token> tokenItor = tokens.iterator();
			Token leftToken = null;

			// token map of each sentence
			TreeMap<Integer, Token> tokensOfSentence = new TreeMap<Integer, Token>();
			int sentenceId = 0;

			while ((originalTextCh = originalTextStream.read()) != -1) {

				Character originalTextChar = (char) originalTextCh;

				//
				// Tokens
				//

				if ((tokenisedTextCh = tokenisedFileStream.read()) != -1) {
					Character tokenisedFileChar = (char) tokenisedTextCh;
					if (tokenisedFileChar == ' '
							|| tokenisedFileChar == System.getProperty(
									"line.separator").charAt(0)) {

						Token token = fetchToken(jcas, tokenBegin, offset,
								tokenItor, leftToken);

						// put tokens in same sentence into a map.
						tokensOfSentence.put(token.getId(), token);

						tokenBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							tokenBegin++;
						} else {
							tokenisedFileStream.read();
						}

					}
				}

				//
				// Sentences
				//
				if ((sentencisedTextCh = sentencisedFileStream.read()) != -1) {
					Character sentencisedFileChar = (char) sentencisedTextCh;
					if (sentencisedFileChar == System.getProperty(
							"line.separator").charAt(0)) {

						fetchSentence(jcas, sentenceBegin, offset, sentenceId);
						sentenceId++;

						tokensOfSentence = new TreeMap<Integer, Token>();

						sentenceBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							sentenceBegin++;
						} else {
							sentencisedFileStream.read();
						}

					}
				}

				offset++;
			}

			if (tokenItor.hasNext()) {
				fetchToken(jcas, tokenBegin, offset, tokenItor, leftToken);
			}
			fetchSentence(jcas, sentenceBegin, offset, sentenceId);

			sentencisedFileStream.close();
			tokenisedFileStream.close();

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new RuntimeException(e);
		}
	}

	private Token fetchToken(JCas jcas, int tokenBegin, int offset,
			Iterator<ConnlxReader.Token> tokenItor, Token leftToken) {
		// the last token is missed due to reaching the end of the file.
		Token token = new Token(jcas, tokenBegin, offset);
		ConnlxReader.Token connlxToken = tokenItor.next();
		String pos = connlxToken.getPos();
		token.setId(connlxToken.getId());
		token.setPos(pos);

		token.setLemma(BioLemmatizerUtil.lemmatizeWord(token.getCoveredText(),
				pos));
		Stemmer stemmer1 = new Stemmer();
		stemmer1.add(token.getCoveredText().toCharArray(), token
				.getCoveredText().length());
		stemmer1.stem();
		token.setStem(stemmer1.toString());
		String subWord = null, subLemma = null, subStem = null;
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
		token.setLeftToken(leftToken);
		if (null != leftToken) {
			leftToken.setRightToken(token);
		}
		leftToken = token;
		token.addToIndexes();

		return token;

	}

	private void fetchSentence(JCas jcas, int sentenceBegin, int offset,
			int sentenceId) {
		// the last sentence is missed due to reaching the end of the file.
		Sentence sentence = new Sentence(jcas, sentenceBegin, offset);
		sentence.setId(sentenceId);
		sentence.addToIndexes();

	}

}
