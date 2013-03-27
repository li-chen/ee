package info.chenli.litway.gson;

public class EntityAnnotation {

	private String id, category;
	private Span span;

	public String getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public Span getSpan() {
		return span;
	}

	@Override
	public String toString() {
		return this.id.concat("\t").concat(span.toString()).concat("\t")
				.concat(category);
	}
}
