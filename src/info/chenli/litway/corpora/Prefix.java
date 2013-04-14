package info.chenli.litway.corpora;

/**
 * Containing the list from <a
 * href="http://en.wikipedia.org/wiki/English_prefixes">Wikepedia</a>, with
 * single character removed. "super" -> "supe" due to Java's the reserved word.
 * 
 * @author Chen Li
 * 
 */
public enum Prefix {

	Afro, ambi, amphi, an, ana, Anglo, ante, anti, apo, arch, astro, auto, be, bi, bio, circum, cis, co, col, com, con, contra, cor, counter, cryo, crypto, de, demi, demo, deuter, deutero, di, dia, dif, dis, du, duo, eco, el, electro, em, en, epi, Euro, ex, extra, fin, fore, Franco, geo, gyro, hemi, hetero, hind, homo, hydro, hyper, hypo, ideo, idio, il, im, in, Indo, infra, inter, intra, ir, iso, macr, macro, mal, maxi, mega, megalo, meta, micro, mid, midi, mini, mis, mon, mono, mult, multi, neo, non, omni, ortho, out, over, paleo, pan, para, ped, per, peri, photo, pod, poly, post, pre, preter, pro, pros, proto, pseudo, pyro, quasi, re, retro, self, semi, socio, step, sub, sup, supe, supra, sur, sy, syl, sym, syn, tele, trans, tri, twi, ultra, un, under, uni, up, vice, with;

	public static String getPrefix(String word) {

		String result = "";

		for (Prefix prefix : Prefix.values()) {
			String prefixStr = String.valueOf(prefix);
			if (word.startsWith(prefixStr)
					&& prefixStr.length() > result.length()) {
				result = prefixStr;
			}
		}

		return result;
	}
}
