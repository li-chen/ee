package info.chenli.litway.util;

import edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer;
import edu.ucdenver.ccp.nlp.biolemmatizer.LemmataEntry;

/**
 * The class is the facade of the <a
 * href="http://biolemmatizer.sourceforge.net/">BioLemmatizer</a>. Its all
 * methods are static to avoid reloading the library, which is time-consuming.
 * 
 * @author Chen Li
 * 
 */
public class BioLemmatizerUtil {

	private static BioLemmatizer lemmatizer = new BioLemmatizer();

	/**
	 * 
	 * @param word
	 * @param pos
	 * @return
	 */
	public static String lemmatizeWord(String word, String pos) {

		LemmataEntry lemma = lemmatizer.lemmatizeByLexiconAndRules(word, pos);

		return lemma.getLemmas().iterator().next().getLemma();

	}
}
