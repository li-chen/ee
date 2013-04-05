package info.chenli.litway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class reads stop words from files used by <a
 * href="http://www.sapientaproject.com">SAPIENTA Project</a>.
 * 
 * @author Chen Li
 * 
 */
public class StopWords {

	private final static Logger logger = Logger.getLogger(StopWords.class
			.getName());

	private static String[] stopWordsShort;
	private static String[] stopWordsShortForNgram;

	static {

		try {
			//
			// stopWordsShort
			//
			InputStream stopWordsShortInput = StopWords.class
					.getResourceAsStream("/stopWords/stop_words_short.txt");

			BufferedReader stopWordsShortBr = new BufferedReader(
					new InputStreamReader(stopWordsShortInput));
			String line;
			List<String> stopWordsShortList = new ArrayList<String>();
			while ((line = stopWordsShortBr.readLine()) != null) {

				stopWordsShortList.add(line.toLowerCase());
			}
			stopWordsShort = new String[stopWordsShortList.size()];
			stopWordsShortList.toArray(stopWordsShort);

			stopWordsShortBr.close();
			stopWordsShortInput.close();

			//
			// stopWordsShortForNgram
			//
			InputStream stopWordsShortForNgramInput = StopWords.class
					.getResourceAsStream("/stopWords/stop_words_short_for_ngram.txt");

			BufferedReader stopWordsShortForNgramBr = new BufferedReader(
					new InputStreamReader(stopWordsShortForNgramInput));

			List<String> stopWordsShortForNgramList = new ArrayList<String>();
			while ((line = stopWordsShortForNgramBr.readLine()) != null) {

				stopWordsShortForNgramList.add(line.toLowerCase());
			}
			stopWordsShortForNgram = new String[stopWordsShortForNgramList
					.size()];
			stopWordsShortForNgramList.toArray(stopWordsShortForNgram);

			stopWordsShortForNgramBr.close();
			stopWordsShortForNgramInput.close();

			logger.info("Stop words are loaded.");

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @return The list of the stop words, which can be used for checking
	 *         individual tokens. e.g. The words are not considered for trigger
	 *         detection in BioNLP.
	 */
	public static String[] getStopWordsShort() {
		return stopWordsShort;
	}

	/**
	 * 
	 * @return The list of the stop words, which can be used for checking the
	 *         n-gram tokens. e.g. The adjacent words are not considered for
	 *         trigger detection in BioNLP.
	 */
	public static String[] getStopWordsShortForNgram() {
		return stopWordsShortForNgram;
	}

	/**
	 * Check whether the given word exists in the list of
	 * {@link #getStopWordsShort()}.
	 * 
	 * @param word
	 *            The word to be checked.
	 * @return true, if exists; false, if not.
	 */
	public static boolean isAStopWordShort(String word) {

		word = word.toLowerCase();

		for (String stopWord : stopWordsShort) {
			if (word.equals(stopWord)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given word exists in the list of
	 * {@link #getStopWordsShortForNgram()}.
	 * 
	 * @param word
	 *            The word to be checked.
	 * @return true, if exists; false, if not.
	 */
	public static boolean isAStopWordShortForNgram(String word) {

		word = word.toLowerCase();

		for (String stopWord : stopWordsShortForNgram) {
			if (word.equals(stopWord)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) {

		int i = 0;
		for (String word : getStopWordsShort()) {
			System.out.println(word + "\t" + getStopWordsShortForNgram()[i++]);
		}
	}
}
