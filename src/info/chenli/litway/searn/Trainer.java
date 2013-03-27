package info.chenli.litway.searn;

import info.chenli.classifier.AbstractClassifier;
import info.chenli.classifier.Instance;
import info.chenli.litway.Paragraph;
import info.chenli.litway.Performance;
import info.chenli.litway.Sentence;
import info.chenli.litway.bionlp13.ge.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Trainer {

	private final static Logger logger = Logger.getLogger(Trainer.class
			.getName());

	private CostSensitiveClassifier classifier;
	private double beta;

	public Trainer(CostSensitiveClassifier classifier) {

		this.classifier = classifier;
	}

//	public void train(List<StructuredInstance> instances) {
//
//		// Optimal policy h. It can take the one learned from the gold standard,
//		// or even a set of rules.
//		CostSensitiveClassifier pi = classifier.getOptimalPolicy(instances);
//
//		// current policy begins with the optimal policy
//		CostSensitiveClassifier h = pi;
//
//		int threshold = 0;
//		int counter = 100;
//
//		while (pi.compareTo(h) > threshold || counter-- != 0) {
//
//			// cost-sensitive examples
//			Map<Instance, Double> E = null;
//
//			train(instances, h, E);
//		}
//	}
//
//	void train(List<StructuredInstance> instances, CostSensitiveClassifier h, List<Instance> E) {
//
//		for (StructuredInstance instance : instances) {
//
//			train(instance, h, E);
//		}
//
//		// train to get the new policy based on the collected examples
//		CostSensitiveClassifier h_new = classifier;
//
//		// update the current policy based on linear interpolation
//		h = beta*h_new + (1-beta)h;
//	}
//
//	void train(StructuredInstance structuredInstance, CostSensitiveClassifier h, List<Instance> E) {
//
//		// use policy h to make prediction
//		State state = predict(structuredInstance, h);
//
//		// iterate through each prediction by using the give policy h.
//		for (int i = 0; i < structuredInstance.getNodes().size(); i++) {
//
//			double[] features = extractFeatures(structuredInstance, state, i);
//
//			double[] cost =null;
//			for (int j = 0; j < h.getPossibleActions(); j++) {
//
//				for (int k = i + 1; k < state.getPredictions().size(); k++) {
//					predict(structuredInstance.getNodes().get(k), h) ;
//					cost[k];
//				}
//
//			}
//
//			E.add(features, cost);
//		}
//
//	}
//
//	double[] extractFeatures(StructuredInstance structuredInstance,
//			State state, int structuralPosition) {
//
//		// TODO return features of node(0, structuralPosition-1)
//
//		return null;
//	}
//
//	State predict(StructuredInstance structuredInstance, Policy h) {
//
//		// predict each node according to policy h
//		for (Instance node : structuredInstance.getNodes()) {
//			predict(node, h);
//		}
//
//		return null;
//	}
//
//	private void predict(Instance node, Policy h) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public AbstractClassifier getModel() {
//		// TODO Auto-generated method stub
//		
//	}
}