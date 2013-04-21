package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;
import info.chenli.classifier.PerceptronClassifier;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Chen Li
 * 
 */
public class CauseRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger.getLogger(CauseRecogniser.class
			.getName());

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			CauseInstances trainingInstances = new CauseInstances();
			trainingInstances
					.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatNumericDictionary(instances);
			dict.saveDictionary(new File("./model/causes.dict"));

			this.train(instances);

//			System.out.println(this.accuracy(instances));
//
//			CauseInstances testInstances = new CauseInstances();
//			testInstances.setTaeDescriptor("/desc/TrainingSetAnnotator.xml");
//			instances = testInstances.getInstances(new File("./data/test/"));
//
//			System.out
//					.println(this.accuracy(dict.instancesToNumeric(instances)));
		}

	}

	public static void main(String[] args) {

		CauseRecogniser tr = new CauseRecogniser();
		tr.train(new File(args[0]), false);
		tr.saveModel(new File("./model/causes.liblinear.model"));

	}
}
