package info.chenli.litway.bionlp13.ge;

/**
 * 
 * @author Chen Li
 * 
 */
public enum TriggerWord {

	absenc, activ, affect, alter, assembl, associ, bind, bound, chang, control, correl, depend, deregul, detect, downstream, effect, engag, express, function, independ, influenc, interact, involv, level, ligand, ligat, link, local, locat, modul, mrna, mutat, phospho, presen, produc, recogn, recruit, regul, releas, respons, role, secret, synthes, target, transcript, transloc, ubiquitin, unaffect;

	public static TriggerWord isATriggerWord(String word) {

		for (TriggerWord tw : TriggerWord.values()) {

			if (word.toLowerCase().startsWith(String.valueOf(tw))) {
				return tw;
			}
		}

		return null;
	}
}
