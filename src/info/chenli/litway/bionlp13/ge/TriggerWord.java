package info.chenli.litway.bionlp13.ge;

/**
 * 
 * @author Chen Li
 * 
 */
public enum TriggerWord {

	absenc, activ, acetyl, affect, alter, assembl, associ, bind, bound, chang, control, correl, deacetyl, depend, deregul, detect, dimer, downstream, effect, engag, express, function, independ, induc, influenc, interact, involv, level, ligand, ligat, link, local, locat, modul, mrna, mutat, phospho, presen, produc, recogn, recruit, regul, releas, respons, role, secret, synthes, target, transcript, transloc, ubiquitin, unaffect;

	public static TriggerWord isATriggerWord(String word) {

		for (TriggerWord tw : TriggerWord.values()) {

			if (word.toLowerCase().startsWith(String.valueOf(tw))) {
				return tw;
			}
		}

		return null;
	}

	public static EventType getEventType(String word) {

		if (word.startsWith(String.valueOf(TriggerWord.bind))
				|| word.startsWith(String.valueOf(TriggerWord.dimer))
				|| word.startsWith(String.valueOf(TriggerWord.ligat))
				|| word.startsWith(String.valueOf(TriggerWord.ligand))
				|| word.startsWith(String.valueOf(TriggerWord.interact))) {
			return EventType.Binding;
		} else if (word.indexOf(String.valueOf(TriggerWord.phospho)) > -1) {
			return EventType.Phosphorylation;
		} else if (word.indexOf(String.valueOf(TriggerWord.ubiquitin)) > -1) {
			return EventType.Ubiquitination;
		} else if (word.startsWith(String.valueOf(TriggerWord.acetyl))) {
			return EventType.Acetylation;
		} else if (word.startsWith(String.valueOf(TriggerWord.deacetyl))) {
			return EventType.Deacetylation;
		} else if (word.startsWith(String.valueOf(TriggerWord.induc))) {
			return EventType.Positive_regulation;
		}

		return null;
	}
}
