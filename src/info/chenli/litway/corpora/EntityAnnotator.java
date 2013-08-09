package info.chenli.litway.corpora;

import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class EntityAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		(new A1Reader())
				.fetchEntities(
						FileUtil.removeFileNameExtension(
								UimaUtil.getJCasFilePath(jCas)).concat(".a1"),
						jCas);

	}

}
