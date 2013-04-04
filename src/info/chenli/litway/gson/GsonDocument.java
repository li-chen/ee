package info.chenli.litway.gson;

public class GsonDocument {

	private String source_db, source_id, section, text;
	private int divistion_id;

	private EntityAnnotation[] catanns;
	private InstanceOfAnnotation[] insanns;
	private RelationAnnotation[] relanns;

	public String getSource_db() {
		return source_db;
	}

	public String getSource_id() {
		return source_id;
	}

	public int getDivistion_id() {
		return divistion_id;
	}

	public String getSection() {
		return section;
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

		String result = this.source_db.concat(source_id).concat("\t")
				.concat(String.valueOf(this.divistion_id)).concat("\t")
				.concat(this.section).concat("\t").concat(text);

		for (EntityAnnotation catann : this.catanns) {
			result = result.concat("\t").concat(catann.toString());
		}

		return result;
	}
}
