package info.chenli.litway.corpora;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.XMLInputSource;

/**
 * An example application that reads documents from files, sends them though an
 * Analysis Engine, and prints all discovered annotations to System.out.
 * <p>
 * The application takes two arguments:
 * <ol type="1">
 * <li>The path to an XML descriptor for the Analysis Engine to be executed</li>
 * <li>An input directory containing files to be processed</li>
 * </ol>
 */
public class Application {
	/**
	 * Main program.
	 * 
	 * @param args
	 *            Command-line arguments - see class description
	 */
	public static void main(String[] args) {
		try {
			File taeDescriptor = null;
			File inputDir = null;

			// Read and validate command line arguments
			boolean validArgs = false;
			if (args.length == 2) {
				taeDescriptor = new File(args[0]);
				inputDir = new File(args[1]);

				validArgs = taeDescriptor.exists()
						&& !taeDescriptor.isDirectory()
						&& (inputDir.isDirectory() || inputDir.isFile());
			}
			if (!validArgs) {
				printUsageMessage();
			} else {
				// get Resource Specifier from XML file
				XMLInputSource in = new XMLInputSource(taeDescriptor);
				ResourceSpecifier specifier = UIMAFramework.getXMLParser()
						.parseResourceSpecifier(in);

				// for debugging, output the Resource Specifier
				// System.out.println(specifier);

				// create Analysis Engine
				AnalysisEngine ae = UIMAFramework
						.produceAnalysisEngine(specifier);
				// create a CAS
				CAS cas = ae.newCAS();

				if (inputDir.isFile()) {
					processFile(inputDir, ae, cas);
				} else {
					// get all files in the input directory
					File[] files = inputDir.listFiles();
					if (files == null) {
						System.out.println("No files to process");
					} else {
						// process documents
						for (int i = 0; i < files.length; i++) {
							if (!files[i].isDirectory()) {
								processFile(files[i], ae, cas);
							}
						}
					}
				}
				ae.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints usage message.
	 */
	private static void printUsageMessage() {
		System.err
				.println("Usage: java org.apache.uima.example.ExampleApplication "
						+ "<Analysis Engine descriptor or PEAR file name> <input dir>");
	}

	/**
	 * Processes a single XML file and prints annotations to System.out
	 * 
	 * @param aFile
	 *            file to process
	 * @param aAE
	 *            Analysis Engine that will process the file
	 * @param aCAS
	 *            CAS that will be used to hold analysis results
	 */
	private static void processFile(File aFile, AnalysisEngine aAE, CAS aCAS)
			throws IOException, AnalysisEngineProcessException {
		System.out.println("Processing file " + aFile.getName());

		String document = FileUtils.file2String(aFile);
		document = document.trim();

		// put document text in CAS
		aCAS.setDocumentText(document);

		// set the path of resource file
		aCAS.createView("FileName").setSofaDataURI(aFile.getName(), "text");

		// process
		aAE.process(aCAS);

		// print annotations to System.out
		PrintAnnotations.printAnnotations(aCAS, System.out);

		// reset the CAS to prepare it for processing the next document
		aCAS.reset();
	}

}