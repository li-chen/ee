package info.chenli.ee.corpora;

import java.io.File;

import info.chenli.ee.bionlp13.ge.EventType;
import info.chenli.ee.gson.GsonDocument;
import info.chenli.ee.gson.EntityAnnotation;
import info.chenli.ee.gson.GsonFacade;
import info.chenli.ee.gson.RelationAnnotation;
import info.chenli.ee.util.FileUtil;
import info.chenli.ee.util.UimaUtil;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class GSAnnotator extends JCasAnnotator_ImplBase {

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

				new Protein(jCas, entity.getSpan().getBegin(), entity.getSpan()
						.getEnd()).addToIndexes();

			} else if (EventType.contains(entity.getCategory())) {

				Trigger trigger = new Trigger(jCas,
						entity.getSpan().getBegin(), entity.getSpan().getEnd());
				trigger.setEventType(entity.getCategory());
				trigger.addToIndexes();

			}
		} // Anaphora is ignored at the moment

		if (null == document.getRelanns() || document.getRelanns().length == 0) {
			return;
		}
		for (RelationAnnotation relation : document.getRelanns()) {
			// TODO construct event annotation
		}
	}
}
