package info.chenli.litway.bionlp13;

import info.chenli.litway.corpora.Protein;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * Read a1 file.
 * 
 * @author Chen Li
 * 
 */
public class EntityAnnotator extends JCasAnnotator_ImplBase {

	private final static Logger logger = Logger.getLogger(EntityAnnotator.class
			.getName());

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(
					FileUtil.removeFileNameExtension(
							UimaUtil.getJCasFilePath(jCas)).concat(".a1"))));

			String line;
			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line);

				String id = st.nextToken();
				EntityTypes entityType = EntityTypes.valueOf(st.nextToken());
				int start = Integer.parseInt(st.nextToken());
				int end = Integer.parseInt(st.nextToken());
				String text = st.nextToken();

				switch (entityType) {

				case Cellular_component:
					CellularComponent cellularComponent = new CellularComponent(
							jCas, start, end);
					cellularComponent.setId(id);
					cellularComponent.addToIndexes();
					break;
				case Complex:
					Complex complex = new Complex(jCas, start, end);
					complex.setId(id);
					complex.addToIndexes();
					break;
				case Gene_or_gene_product:
					Gene gene = new Gene(jCas, start, end);
					gene.setId(id);
					gene.addToIndexes();
					break;
				case Protein:
					Protein protein = new Protein(jCas, start, end);
					protein.setId(id);
					protein.addToIndexes();
					break;
				case Simple_chemical:
					Chemical chemical = new Chemical(jCas, start, end);
					chemical.setId(id);
					chemical.addToIndexes();
					break;
				}
			}

			br.close();

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}
}
