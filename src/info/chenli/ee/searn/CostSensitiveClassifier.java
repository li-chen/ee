package info.chenli.ee.searn;

import java.util.List;

import weka.core.Instances;

public interface CostSensitiveClassifier extends Comparable<CostSensitiveClassifier> {

	public CostSensitiveClassifier getOptimalPolicy(List<StructuredInstance> instances);

	public void buildClassifier(Instances instances, double[] costs) throws Exception;

	public int compareTo(CostSensitiveClassifier h);

}
