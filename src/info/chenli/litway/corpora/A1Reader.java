package info.chenli.litway.corpora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.uima.jcas.JCas;

public class A1Reader {

	private final static Logger logger = Logger.getLogger(A1Reader.class
			.getName());

	public void fetchEntities(String a1FileName, JCas jcas) {

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(new File(a1FileName)));

			String line;

			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line);

				Entity entity = new Entity(jcas);
				entity.setId(st.nextToken());
				entity.setEntityType(st.nextToken());
				entity.setBegin(Integer.parseInt(st.nextToken()));
				entity.setEnd(Integer.parseInt(st.nextToken()));

				entity.addToIndexes();
			}

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} finally {

			try {
				br.close();
			} catch (IOException e) {
				logger.severe(e.getMessage());
				throw new RuntimeException(e);
			}
		}

	}
}
