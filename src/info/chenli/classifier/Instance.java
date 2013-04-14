package info.chenli.classifier;

import java.util.Iterator;
import java.util.List;

public class Instance {

	// instance ID
	private String id;
	private int label = -1;
	private String labelString;
	private List<String[]> featuresString;
	private int[] featuresNumeric;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLabel() {
		return this.label;
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

	public List<String[]> getFeaturesString() {
		return featuresString;
	}

	public void setFeaturesString(List<String[]> featuresString) {
		this.featuresString = featuresString;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		if (null != getLabelString()) {
			sb.append(String.valueOf(getLabel()).concat(":")
					.concat(getLabelString()));
		}

		Iterator<String[]> featureStrIter = getFeaturesString().iterator();
//		int i = 0;
		while (featureStrIter.hasNext()) {
			String[] feature = featureStrIter.next();
			for (String value : feature) {
				sb.append("\t"
//						.concat(String.valueOf(getFeaturesNumeric()[i])
//						.concat(":")
						.concat(value));
			}
		}

		return sb.toString();
	}

}
