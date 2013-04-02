package info.chenli.classifier;

public class Accurary {

	private double correct, total;

	public Accurary(double correct, double total) {

		this.correct = correct;
		this.total = total;
	}

	@Override
	public String toString() {
		return "Correct:".concat(String.valueOf(this.correct)).concat("\t")
				.concat("Total:").concat(String.valueOf(this.total))
				.concat("\t").concat("Accurary:")
				.concat(String.valueOf(this.correct / this.total));
	}
}
