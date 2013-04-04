package info.chenli.classifier;

import java.util.Iterator;
import java.util.List;

public class Instance {

	// instance ID
	private String id;
	private int label;
	private String labelString;
	private List<SparseVector> features;
	private List<String> featuresString;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public List<SparseVector> getFeatures() {
		return features;
	}

	public void setFeatures(List<SparseVector> features) {
		this.features = features;
	}

	public String getLabelString() {
		return labelString;
	}

	public void setLabelString(String labelString) {
		this.labelString = labelString;
	}

	public List<String> getFeaturesString() {
		return featuresString;
	}

	public void setFeaturesString(List<String> featuresString) {
		this.featuresString = featuresString;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(getLabel()).concat(":")
				.concat(getLabelString()));

		Iterator<String> featureStrIter = getFeaturesString().iterator();
		for (SparseVector feature : getFeatures()) {
			sb.append("\t".concat(String.valueOf(feature.getPosition())
					.concat(":").concat(featureStrIter.next())));
		}

		return sb.toString();
	}
}
