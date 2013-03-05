package info.chenli.ee.util;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

public class UimaUtil {

	public static String getJCasFileName(JCas jCas) {

		try {
			return jCas.getView("FileName")
					.getSofaDataURI();

		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
}
