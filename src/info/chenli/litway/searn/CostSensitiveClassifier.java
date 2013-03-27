package info.chenli.litway.searn;

import java.util.List;

public interface CostSensitiveClassifier extends Comparable<CostSensitiveClassifier> {

	public CostSensitiveClassifier getOptimalPolicy(List<StructuredInstance> instances);

	public void buildClassifier(List<StructuredInstance> instances, double[] costs) throws Exception;

	public int compareTo(CostSensitiveClassifier h);

}
