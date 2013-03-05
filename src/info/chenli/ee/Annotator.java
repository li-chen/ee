package info.chenli.ee;

import java.io.File;

import info.chenli.ee.gson.EntityAnnotation;
import info.chenli.ee.gson.GsonFacade;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class Annotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		EntityAnnotation[] annotations = GsonFacade.instance
				.getDocument(
						new File(
								"/Users/chenli/projects/bionlp2013/data/training/ge/PMC-2791889-00-TIAB.json"))
				.getCatanns();

		// TODO assign catanns to entities
		for (EntityAnnotation annotation : annotations) {
			if (annotation.getCategory().equals("Protein")) {
				new Protein(jCas, annotation.getSpan().getBegin(), annotation
						.getSpan().getEnd()).addToIndexes();
			} else {
				new Trigger(jCas, annotation.getSpan().getBegin(), annotation
						.getSpan().getEnd()).addToIndexes();

			}

		}
	}
}
