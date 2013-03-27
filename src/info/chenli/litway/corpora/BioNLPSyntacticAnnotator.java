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

		File sentencisedFile = new File(
				"./resources/syntacticAnalysis/sentences/BioNLP-ST-2013_GE_train_data_rev2/"
						.concat(FileUtil.removeFileNameExtension(UimaUtil
								.getJCasFileName(jcas))).concat(".ss"));
		File tokenisedFile = new File(
				"./resources/syntacticAnalysis/tokenization/BioNLP-ST-2013_GE_train_data_rev2/"
						.concat(FileUtil.removeFileNameExtension(UimaUtil
								.getJCasFileName(jcas))).concat(".tok"));
		List<ConnlxReader.Token> tokens = ConnlxReader.getTokens(new File(
				"./resources/syntacticAnalysis/McCCJ/BioNLP-ST-2013_GE_train_data_rev2/"
						.concat(FileUtil.removeFileNameExtension(UimaUtil
								.getJCasFileName(jcas))).concat(".connlx")));

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

						Token token = new Token(jcas, tokenBegin, offset);
						ConnlxReader.Token connlxToken = tokenItor.next();
						String pos = connlxToken.getPos();
						token.setId(connlxToken.getId());
						token.setPos(pos);
						token.setLemma(BioLemmatizerUtil.lemmatizeWord(
								token.getCoveredText(), pos));
						Stemmer stemmer = new Stemmer();
						stemmer.add(token.getCoveredText().toCharArray(), token
								.getCoveredText().length());
						stemmer.stem();
						token.setStem(stemmer.toString());
						token.setLeftToken(leftToken);
						if (null != leftToken) {
							leftToken.setRightToken(token);
						}
						leftToken = token;
						token.setDependentId(connlxToken.getDependentId());
						token.setRelation(connlxToken.getRelation());
						token.addToIndexes();

						// put tokens in same sentence into a map.
						tokensOfSentence.put(connlxToken.getId(), token);

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

						Sentence sentence = new Sentence(jcas, sentenceBegin,
								offset);
						sentence.addToIndexes();

						// Once one sentence is finished, start appending
						// dependent for each token.
						for (Token token : tokensOfSentence.values()) {

							token.setDependent(tokensOfSentence.get(token
									.getDependentId()));
						}

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

			sentencisedFileStream.close();
			tokenisedFileStream.close();

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new RuntimeException(e);
		}
	}

}
