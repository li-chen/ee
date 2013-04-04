package info.chenli.classifier;

import java.util.Iterator;
import java.util.List;

public class Instance {

	// instance ID
	private String id;
	private int label;
	private String labelString;
	private List<String> featuresString;
	private int[] featuresNumeric;

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

	public int[] getFeaturesNumeric() {
		return featuresNumeric;
	}

	public void setFeaturesNumeric(int[] featuresNumeric) {
		this.featuresNumeric = featuresNumeric;
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
		for (int featureNumeric : getFeaturesNumeric()) {
			sb.append("\t".concat(String.valueOf(featureNumeric).concat(":")
					.concat(featureStrIter.next())));
		}

		return sb.toString();
	}

}
