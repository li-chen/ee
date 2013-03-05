package info.chenli.ee.bioc;

import java.util.ArrayList;
import java.util.List;

import sharedtask.bioc.*;

public class BioCFacade {

	public static BioCFacade instance = new BioCFacade();

	private BioCFacade() {
	}

	public Collection getCollection(String fileName) {

		ConnectorWoodstox inConnector = new ConnectorWoodstox();

		return inConnector.startRead(fileName);

	}

	public List<Document> getDocuments(String fileName) {

		ConnectorWoodstox inConnector = new ConnectorWoodstox();
		inConnector.startRead(fileName);

		List<Document> documents = new ArrayList<Document>();

		while (inConnector.hasNext()) {
			documents.add(inConnector.next());
		}

		return documents;
	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("usage: java File-To-Be-Read.xml");
			System.exit(1);
		}
		// generate bioc object to read BioC XML file
		BioCFacade bioC = new BioCFacade();

	}
}