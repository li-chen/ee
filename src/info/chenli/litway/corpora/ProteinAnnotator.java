package info.chenli.litway.corpora;

import info.chenli.litway.gson.EntityAnnotation;
import info.chenli.litway.gson.GsonDocument;
import info.chenli.litway.gson.GsonFacade;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import java.io.File;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class ProteinAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		GsonDocument document = GsonFacade.instance.getDocument(new File(
				"./resources/training/ge/".concat(
						FileUtil.removeFileNameExtension(UimaUtil
								.getJCasFileName(jCas))).concat(".json")));

		EntityAnnotation[] entityAnnotations = document.getCatanns();
		if (null == entityAnnotations || entityAnnotations.length == 0) {
			return;
		}

		// assign catanns to protein and trigger
		for (EntityAnnotation entity : entityAnnotations) {
			if (entity.getCategory().equals("Protein")) {

				Protein protein = new Protein(jCas,
						entity.getSpan().getBegin(), entity.getSpan().getEnd());
				protein.setId(entity.getId());
				protein.addToIndexes();

			}
		}
	}

}
