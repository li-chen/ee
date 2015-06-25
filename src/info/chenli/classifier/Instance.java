package info.chenli.classifier;

import java.util.List;

public class Instance {

	// instance ID
	public boolean isReference;
	public int tokenId;
	public int sentenceId;	
	public String fileId;
	private String id;
	private int label;
	private String labelString;
	private List<String[]> featuresString;
	private int[] featuresNumeric = null;
	private double[] featuresNumericWord2vec = null;
	
	public boolean getIsReference() {
		return this.isReference;
	}

	public void setIsReference(boolean isReference) {
		this.isReference = isReference;
	}
	
	public int getTokenId() {
		return this.tokenId;
	}

	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	
	public int getSentenceId() {
		return this.sentenceId;
	}

	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}
	
	public String getFileId() {
		return this.fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
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
	
	public double[] getFeaturesNumericWord2vec() {
		return featuresNumericWord2vec;
	}

	public void setFeaturesNumericWord2vec(double[] featuresNumericWord2vec) {
		this.featuresNumericWord2vec = featuresNumericWord2vec;
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
			sb.append(String.valueOf(getLabel()));
		}

		for (int value : getFeaturesNumeric()) {
			sb.append(" "
			// .concat(String.valueOf(getFeaturesNumeric()[i])
			// .concat(":")
					.concat(String.valueOf(value)).concat(":1"));
		}

		return sb.toString();
	}

}
