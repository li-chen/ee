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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

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
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));

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

						fetchSentence(jcas, sentenceBegin, offset, sentenceId,
								pairsOfArticle.get(sentenceId));
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
			fetchSentence(jcas, sentenceBegin, offset, sentenceId,
					pairsOfArticle.get(sentenceId));

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
		ConnlxReader.Token connlxToken = tokenItor.next();
		String pos = connlxToken.getPos();
		Token token = createNewToken(jcas, tokenBegin, offset, pos);
		token.setId(connlxToken.getId());

		token.setLeftToken(leftToken);
		if (null != leftToken) {
			leftToken.setRightToken(token);
		}
		leftToken = token;
		token.addToIndexes();

		return token;

	}

	private void fetchSentence(JCas jcas, int sentenceBegin, int offset,
			int sentenceId, Set<Pair> pairsOfSentence) {
		// the last sentence is missed due to reaching the end of the file.
		Sentence sentence = new Sentence(jcas, sentenceBegin, offset);
		sentence.setId(sentenceId);
		sentence.addToIndexes();

		// as many protein (1036 in bionlp development data) are within token.
		// They will be separated as tokens
//		postProcessSentence(jcas, sentence, pairsOfSentence);
	}

	private void postProcessSentence(JCas jcas, Sentence sentence,
			Set<Pair> pairsOfSentence) {

		List<Entity> sentenceProteins = JCasUtil.selectCovered(jcas,
				Entity.class, sentence);

		List<Token> originalTokens = JCasUtil.selectCovered(jcas, Token.class,
				sentence);

		System.out.println(sentence.getCoveredText());
		int tokenId = originalTokens.size() + 1;

		// process the tokens which may contain protein and/or trigger.
		for (Token token : originalTokens) {

			List<Entity> containedProteins = new ArrayList<Entity>();

			for (Entity protein : sentenceProteins) {
				// if the protein is the token
				if (protein.getBegin() == token.getBegin()
						&& protein.getEnd() == token.getEnd()) {
					continue;
				}
				if (protein.getBegin() >= token.getBegin()
						&& protein.getEnd() <= token.getEnd()) {
					containedProteins.add(protein);
				}
			}

			if (containedProteins.size() < 1) {
				continue;
			}

			Collections.sort(containedProteins, new AnnotationSorter());

			//
			// if there is contained protein(s), start breaking the old token
			// into new tokens
			//
			List<Token> newTokens = new ArrayList<Token>();

			// collect all candidate new tokens
			int tokenBegin = token.getBegin(), tokenEnd;
			for (Entity protein : containedProteins) {
				tokenEnd = protein.getBegin();
				if (tokenBegin == tokenEnd) {
					tokenEnd = protein.getEnd();
					Token proteinToken = createNewToken(jcas, tokenBegin,
							tokenEnd, null);
					newTokens.add(proteinToken);
				} else if (tokenBegin < tokenEnd) {
					Token newToken = createNewToken(jcas, tokenBegin, tokenEnd,
							null);
					newTokens.addAll(furtherBreakToken(jcas, newToken));
					tokenBegin = protein.getBegin();
					tokenEnd = protein.getEnd();
					Token proteinToken = createNewToken(jcas, tokenBegin,
							tokenEnd, null);
					newTokens.add(proteinToken);
				}
				tokenBegin = tokenEnd;
			}
			if (tokenBegin != token.getEnd()) {
				newTokens
						.addAll(furtherBreakToken(
								jcas,
								createNewToken(jcas, tokenBegin,
										token.getEnd(), null)));
			}

			System.out.print(token.getCoveredText() + "\t|");
			for (Entity protein : containedProteins) {
				System.out.print("\t" + protein.getCoveredText());
			}
			System.out.print("\t|");
			Collections.sort(newTokens, new AnnotationSorter());
			for (Token newToken : newTokens) {
				System.out.print("\t" + newToken.getCoveredText());
			}
			System.out.println();

			Token leftToken = token.getLeftToken();
			for (Token newToken : newTokens) {
				if (newToken.getBegin() == token.getBegin()
						&& newToken.getEnd() == token.getEnd()) {
					continue;
				}

				newToken.setLeftToken(leftToken);
				if (null != leftToken) {
					leftToken.setRightToken(newToken);
				}
				leftToken = newToken;
				newToken.setId(tokenId++);

				newToken.addToIndexes();
			}
			Token lastToken = newTokens.get(newTokens.size() - 1);
			lastToken.setRightToken(token.getRightToken());
			if (null != token.getRightToken()) {
				token.getRightToken().setLeftToken(lastToken);
			}

			token.removeFromIndexes();
		}

	}

	private List<Token> furtherBreakToken(JCas jcas, Token token) {

		List<Token> result = new ArrayList<Token>();
		if (token.getCoveredText().length() == 1) {
			result.add(token);
		} else {
			Token lastToken = token;
			if (token.getCoveredText().startsWith("/")
					|| token.getCoveredText().startsWith("-")
					|| token.getCoveredText().startsWith("+")
					|| token.getCoveredText().startsWith(":")) {
				Token newToken = createNewToken(jcas, token.getBegin(),
						token.getBegin() + 1, null);
				result.add(newToken);
				result.addAll(furtherBreakToken(
						jcas,
						createNewToken(jcas, token.getBegin() + 1,
								token.getEnd(), null)));
				for (Token aNewToken : result) {
					if (aNewToken.getEnd() == token.getEnd()) {
						lastToken = aNewToken;
						break;
					}
				}
				if (lastToken.getCoveredText().length() == 1) {
					return result;
				}
			} else if (lastToken.getCoveredText().endsWith("/")
					|| lastToken.getCoveredText().endsWith("-")
					|| lastToken.getCoveredText().endsWith("+")
					|| lastToken.getCoveredText().endsWith(":")) {
				Token newToken = createNewToken(jcas, lastToken.getEnd() - 1,
						lastToken.getEnd(), null);
				result.add(newToken);
				result.addAll(furtherBreakToken(jcas,
						new Token(jcas, lastToken.getBegin(),
								token.getEnd() - 1)));
			} else {
				result.add(lastToken);
			}
		}
		return result;
	}

	private Token createNewToken(JCas jcas, int begin, int end, String pos) {

		Token token = new Token(jcas, begin, end);

		token.setPos(pos);
		String text = token.getCoveredText();
		token.setLemma(BioLemmatizerUtil.lemmatizeWord(text.toLowerCase(),
				token.getPos()));
		Stemmer stem = new Stemmer();
		stem.add(text.toCharArray(), text.length());
		stem.stem();
		token.setStem(stem.toString());

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

		return token;
	}

}
