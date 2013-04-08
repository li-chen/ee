package info.chenli.litway.corpora;

import info.chenli.litway.bioc.BioCFacade;
import info.chenli.litway.util.UimaUtil;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import sharedtask.bioc.Annotation;
import sharedtask.bioc.Document;
import sharedtask.bioc.Passage;
import sharedtask.bioc.Sentence;

public class BioCAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		for (Document document : BioCFacade.instance
				.getDocuments("./resources/syntacticAnalysis/bioc/bionlp-st-2013_ge_train_devel_texts_pos+lemma.xml")) {

			if (document.id.equals(UimaUtil.getJCasFileName(jCas))) {

				for (Passage passage : document.passages) {

					for (Sentence sentence : passage.sentences) {
						for (Annotation annotation : sentence.annotations) {

							Token token = new Token(jCas, annotation.offset,
									annotation.offset + annotation.length);

							String annotationType = annotation.type;
							token.setPos(annotationType.substring(0,
									annotationType.lastIndexOf("|")));
							token.setLemma(annotationType
									.substring(annotationType.lastIndexOf("|") + 1));

							token.addToIndexes();
						}
					}
				}
			}
		}

	}
}
