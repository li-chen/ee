package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;
import info.chenli.litway.util.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;

public class BindingRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger
			.getLogger(BindingRecogniser.class.getName());
	private String classifierName = "liblinear";

	public void train(String trainingDir, int round) {
		//
		// collect all instances and fetch syntactical information
		//
		BindingInstances trainingInstances = new BindingInstances();
		trainingInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances.getInstances(new File(
				trainingDir));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/bindings.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");

		// save instances
		/*trainingInstances.saveInstances(new File(
				"./model/instances.binding.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.binding.svm.txt"));*/

		// shuffle
		Collections.shuffle(instances);
		logger.info("Shuffle instances.");

		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/bindings.".concat(classifierName).concat(
				".model")));

	}

	public void train2(String trainingDir, int round) {
		//
		// collect all instances and fetch syntactical information
		//
		BindingInstances trainingInstances = new BindingInstances();
		trainingInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances.getInstances(new File(
				trainingDir));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/bindings.train.devel.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");

		// save instances
/*		trainingInstances.saveInstances(new File(
				"./model/instances.binding.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.binding.svm.txt"));
*/
		// shuffle
		Collections.shuffle(instances);
		logger.info("Shuffle instances.");

		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/bindings.train.devel.".concat(classifierName).concat(
				".model")));

	}

	private void test(File file) {
		// TODO Auto-generated method stub
		BindingInstances testInstances = new BindingInstances();
		testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = testInstances.getInstances(file);
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));
		InstanceDictionary dict = new InstanceDictionary();
		dict.loadDictionary(new File("./model/bindings."
				.concat(classifierName).concat(".dict")));
		this.loadModel(new File("./model/bindings.".concat(
				classifierName).concat(".model")));
		instances = dict.instancesToNumeric(instances);
		testInstances.saveSvmLightInstances(new File(
				"./model/instances.bindings.svm.dev.txt"));
		 int total = 0, correct = 0, tp = 0, tn = 0,  fn, fp, pp = 0;
		float p, r, f;
		
		for (Instance instance : instances) {
			int prediction = predict(instance);
			if (prediction == instance.getLabel()) {
				if (instance.getLabelString().equalsIgnoreCase("Binding")){
					tp++;
				}
				correct++;
			}else if (prediction != instance.getLabel()
					&& instance.getLabelString().equalsIgnoreCase("Non_binding")) {
				//System.out.print(instance.getSentenceId());
				//System.out.println("\t" + "fp" + "\t" + instance.getFileId());
			}else if (prediction != instance.getLabel()
					&& instance.getLabelString().equalsIgnoreCase("Binding")) {
				//System.out.print(instance.getSentenceId());
				//System.out.println("\t" + "fn" + "\t" + instance.getFileId());
			}
			
			if (instance.getLabelString().equalsIgnoreCase("Binding")){
				pp++;
			}
			total++;
		}
		
		fn = pp - tp;
		tn = correct - tp;
		fp = total - pp - tn;
		p = (float) tp / (tp + fp);
		r = (float) tp / (tp + fn);
		f = (float) 2 * p * r / (p + r);
		
		System.out.println(new Accurary(correct, total));
		System.out.println("tp: " + tp + "   fp: " + fp + "   fn: " + fn);
		System.out.println("p: " + p + "   r: " + r + "   f: " + f);
	}
	public static void main(String[] args) {

		BindingRecogniser br = new BindingRecogniser();
		//br.train2("/media/songrq/soft/litway/数据/BioNLP13/"
		//		+ "BioNLP-ST-2013_GE_train_devel_data_yuanShuJu", 1);
		
		br.train2(args[0], 1);
		
		/*br.train("/media/songrq/soft/litway/数据/BioNLP13/"
			+ "BioNLP-ST-2013_GE_train_data_yuanShuJu", 1);
		br.test(new File("/media/songrq/soft/litway/数据/BioNLP13/"
			+ "BioNLP-ST-2013_GE_devel_data_yuanShuJu"));*/
		
		/*br.train("/media/songrq/soft/litway/数据/BioNLP11/"
			+ "BioNLP-ST-2011-2013_GE_train_data", 1);
		br.test(new File("/media/songrq/soft/litway/数据/BioNLP13/"
			+ "BioNLP-ST-2013_GE_devel_data_yuanShuJu"));*/
		
		//br.train2("/media/songrq/soft/litway/数据/BioNLP11/"
		//		+ "BioNLP-ST-2011-2013_GE_train_devel_data", 1);
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
