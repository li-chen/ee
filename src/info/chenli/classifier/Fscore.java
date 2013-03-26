package info.chenli.classifier;

public class Fscore {

	private double correct, total;

	public Fscore(double correct, double total) {

		this.correct = correct;
		this.total = total;
	}

	public double getPrecision() {
		return correct / (correct + (total - correct));
	}

	public double getRecall() {
		return correct / total;
	}

	public double getFscore() {

		return 2 * getPrecision() * getRecall()
				/ (getPrecision() + getRecall());
	}

	@Override
	public String toString() {
		return "Precision:".concat(String.valueOf(getPrecision())).concat("\t")
				.concat("Recall:").concat(String.valueOf(getRecall()))
				.concat("\t").concat("Fscore:")
				.concat(String.valueOf(getFscore()));
	}
}
