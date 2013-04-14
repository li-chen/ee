package info.chenli.litway.corpora;

/**
 * Containing the list from <a
 * href="http://grammar.about.com/od/words/a/comsuffixes.htm">About.com</a>,
 * with single character removed.
 * 
 * @author Chen Li
 * 
 */
public enum Suffix {

	able, acy, al, ance, ate, dom, en, ence, er, esque, ful, fy, ible, ic, ical, ify, ious, ise, ish, ism, ist, ity, ive, ize, less, ment, ness, or, ous, ship, sion, tion, ty;

	public static String getSuffix(String word) {

		String result = "";

		for (Suffix suffix : Suffix.values()) {
			String suffixStr = String.valueOf(suffix);
			if (word.endsWith(suffixStr)
					&& suffixStr.length() > result.length()) {
				result = suffixStr;
			}
		}

		return result;
	}
}
