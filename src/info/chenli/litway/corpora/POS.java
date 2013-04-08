package info.chenli.litway.corpora;

public enum POS {

	CC, CD, DT, EX, FW, IN, JJ, JJR, JJS, LS, MD, NN, NNS, NNP, NNPS, PDT, POS, PRP, PRP$, RB, RBR, RBS, RP, SYM, TO, UH, VB, VBD, VBG, VBN, VBP, VBZ, WDT, WP, WP$, WRB;

	public static boolean isPos(String pos) {

		try {

			POS.valueOf(pos);
			return true;

		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
