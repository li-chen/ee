package info.chenli.ee.gson;

public class Document {

	private String pmcdoc_id, div_id, text;

	private EntityAnnotation[] catanns;
	private InstanceOfAnnotation[] insanns;
	private RelationAnnotation[] relanns;

	public String getPmcdoc_id() {
		return pmcdoc_id;
	}


	public String getDiv_id() {
		return div_id;
	}


	public String getText() {
		return text;
	}


	public EntityAnnotation[] getCatanns() {
		return catanns;
	}

	public InstanceOfAnnotation[] getInsanns() {
		return insanns;
	}

	public RelationAnnotation[] getRelanns() {
		return relanns;
	}


	@Override
	public String toString() {

		String result = this.pmcdoc_id.concat("\t").concat(this.div_id)
				.concat("\t").concat(this.text);

		for (EntityAnnotation catann : this.catanns) {
			result = result.concat("\t").concat(catann.toString());
		}

		return result;
	}
}
