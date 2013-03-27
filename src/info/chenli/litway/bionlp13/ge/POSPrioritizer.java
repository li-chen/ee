package info.chenli.litway.bionlp13.ge;

import info.chenli.litway.corpora.POS;

import java.util.Comparator;

/**
 * The class prioritize different pos tag, which is, for example, useful for
 * choosing one token out of a multi-token trigger.
 * 
 * @author Chen Li
 * 
 */
public class POSPrioritizer implements Comparator<POS> {

	/**
	 * The available tags in this list should be identical to the ones in {@link POS}
	 * 
	 * @author Chen Li
	 * 
	 */
	public enum order {
		NN, NNS, NNP, NNPS, VB, VBD, VBG, VBN, VBP, VBZ, RB, RBR, RBS, JJ, JJR, JJS, CC, CD, DT, EX, FW, IN, LS, MD, PDT, POS, PRP, PRP$, RP, SYM, TO, UH, WDT, WP, WP$, WRB
	}

	@Override
	public int compare(POS pos1, POS pos2) {

		return order.valueOf(String.valueOf(pos1)).compareTo(
				order.valueOf(String.valueOf(pos2)));
	}

}
