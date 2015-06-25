package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;

/**
 * 
 * @author rqsong
 * 
 */
public class ArgumentRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger.getLogger(ArgumentRecogniser.class
			.getName());
	private final String classifierName = "liblinear";

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			ArgumentInstances trainingInstances = new ArgumentInstances();
			trainingInstances
					.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatNumericDictionary(instances);
			dict.saveDictionary(new File("./model/arguments.".concat(
					classifierName).concat(".dict")));
			/*
			trainingInstances.saveInstances(new File(
					"./model/instances.theme.txt"));
			trainingInstances.saveSvmLightInstances(new File(
					"./model/instances.theme.svm.txt"));
			*/
			train(dict.instancesToNumeric(instances));
			saveModel(new File("./model/arguments.".concat(classifierName)
					.concat(".model")));
			// System.out.println(accuracy(instances));

			
			
			// System.out.println(accuracy(instances));
		}

	}

	public void train2(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			ArgumentInstances trainingInstances = new ArgumentInstances();
			trainingInstances
					.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatNumericDictionary(instances);
			dict.saveDictionary(new File("./model/arguments.train.devel.".concat(
					classifierName).concat(".dict")));
			/*
			trainingInstances.saveInstances(new File(
					"./model/instances.theme.txt"));
			trainingInstances.saveSvmLightInstances(new File(
					"./model/instances.theme.svm.txt"));
			*/
			train(dict.instancesToNumeric(instances));
			saveModel(new File("./model/arguments.train.devel.".concat(classifierName)
					.concat(".model")));
			// System.out.println(accuracy(instances));

			
			
			// System.out.println(accuracy(instances));
		}

	}

	public static void main(String[] args) {

		ArgumentRecogniser tr = new ArgumentRecogniser();
		//tr.train2(new File("/media/songrq/soft/litway/数据/BioNLP13/"
		//		+ "BioNLP-ST-2013_GE_train_devel_data_yuanShuJu"), false);
		
		tr.train2(new File(args[0]), false);
		
		//tr.train(new File("/media/songrq/soft/litway/数据/BioNLP13/"
		//			+ "BioNLP-ST-2013_GE_train_data_yuanShuJu"), false);

		/*tr.train(new File("/media/songrq/soft/litway/数据/BioNLP11/"
					+ "BioNLP-ST-2011-2013_GE_train_data"), false);
		tr.test(new File("/media/songrq/soft/litway/数据/BioNLP13/"
					+ "BioNLP-ST-2013_GE_devel_data_yuanShuJu"));*/
		
		//tr.train(new File("/media/songrq/soft/litway/数据/BioNLP11/"
		//		+ "b"), false);
		
		//tr.test(new File("/media/songrq/soft/litway/数据/BioNLP13/"
		//			+ "BioNLP-ST-2013_GE_devel_data_yuanShuJu"));
		
		tr.train2(new File("/media/songrq/soft/litway/数据/BioNLP11/"
				+ "BioNLP-ST-2011-2013_GE_train_devel_data"), false);


		
		/*
		tr.loadModel(new File("./model/themes.liblinear.model".concat(tr.classifierName)
				.concat(".model")));

		InstanceDictionary dict = new InstanceDictionary();
		dict.loadDictionary(new File("./model/themes."
				.concat(tr.classifierName).concat(".dict")));

		ThemeInstances ti = new ThemeInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(new File(args[0]));

		instances = dict.instancesToNumeric(instances);

		int total = 0, correct = 0;
		for (Instance instance : instances) {
			int prediction = tr.predict(instance);
			System.out.print(instance.getLabel() + ":" + prediction);
			for (String[] values : instance.getFeaturesString()) {
				for (String value : values) {
					System.out.print("\t" + value);
				}
			}
			System.out.println();
			for (int value : instance.getFeaturesNumeric()) {
				System.out.print("\t" + value);
			}
			System.out.println();
			if (prediction == instance.getLabel()) {
				correct++;
			}
			total++;
		}
		System.out.println(new Accurary(correct, total));
		*/
	}

	private void test(File file) {
		// TODO Auto-generated method stub
		ArgumentInstances testInstances = new ArgumentInstances();
		testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = testInstances.getInstances(file);
		InstanceDictionary dict = new InstanceDictionary();
		dict.loadDictionary(new File("./model/arguments."
				.concat(classifierName).concat(".dict")));
		this.loadModel(new File("./model/arguments.".concat(
				classifierName).concat(".model")));
		instances = dict.instancesToNumeric(instances);
		testInstances.saveSvmLightInstances(new File(
				"./model/instances.arguments.svm.dev.txt"));
		 int total = 0, correct = 0, tp, tn = 0, n = 0, fn, fp;
		float p, r, f;
		
		for (Instance instance : instances) {
			int prediction = predict(instance);
			if (prediction == instance.getLabel()) {
				if (instance.getLabelString().equalsIgnoreCase("Non_Argument")){
					tn++;
				}//else if (instance.getFileId().equals("Binding")){
					//System.out.println("TP    " + instance.getFileId() + "    " + instance.getLabelString() + "    " + instance.getId());
				//}
				correct++;
			}//else if (!instance.getLabelString().equalsIgnoreCase("Non_Argument")
			//		&& prediction == dict.getLabelNumeric("Non_Argument")
			//		&& instance.getFileId().equals("Binding")) {
				//System.out.println("FN    " + instance.getFileId() + "    " + instance.getId());
			//}
			
			if (instance.getLabelString().equalsIgnoreCase("Non_Argument")){
				n++;
			}
			total++;
		}
		
		fp = n - tn;
		tp = correct - tn;
		fn = total - n - tp;
		p = (float) tp / (tp + fp);
		r = (float) tp / (tp + fn);
		f = (float) 2 * p * r / (p + r);
		
		System.out.println(new Accurary(correct, total));
		System.out.println("tp: " + tp + "   fp: " + fp + "   fn: " + fn);
		System.out.println("p: " + p + "   r: " + r + "   f: " + f);
	}
	
	public double predict_values(int[] featureSparseVector) {

		if (featureSparseVector == null) {
			throw new IllegalArgumentException(
					"Empty sparse vector. This probably due to that the dictionary hasn't converted instances to numeric features yet.");
		}

		int n;
		int nr_feature = this.model.getNrFeature();
		if (this.model.getBias() >= 0) {
			n = nr_feature + 1;
		} else {
			n = nr_feature;
		}

		List<Feature> featureNodes = new ArrayList<Feature>();
		int previousIndex = 0;
		for (int index : featureSparseVector) {
			if (index > previousIndex) {
				featureNodes.add(new FeatureNode(index, 1));
			}
			previousIndex = index;
		}
		if (model.getBias() >= 0) {
			Feature node = new FeatureNode(n, model.getBias());
			featureNodes.add(node);
		}
		Feature[] instance = new FeatureNode[featureNodes.size()];
		instance = featureNodes.toArray(instance);
		double[] dec_values = new double[this.model.getNrClass()];
		int type = (int) Math.round(Linear.predictValues(this.model, instance, dec_values));
		return dec_values[type];
	}
}
