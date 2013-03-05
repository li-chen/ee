package info.chenli.ee.gson;

public class Span {

	private int begin, end;

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public String toString() {
		return String.valueOf(this.begin).concat("\t")
				.concat(String.valueOf(this.end));
	}
}
