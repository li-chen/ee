package info.chenli.classifier;

public class Fscore {

	private double tp, fp, tn, fn, recall, precision;

	public Fscore(int tp, int fp, int tn, int fn) {

		this.tp = tp;
		this.fp = fp;
		this.tn = tn;
		this.fn = fn;
		this.recall = tp / (tp + fn);
		this.precision = tp / (tp + fp);
	}

	public Fscore(double recall, double precision) {

		this.recall = recall;
		this.precision = precision;
	}

	public double getPrecision() {

		return this.precision;
	}

	public double getRecall() {

		return this.recall;
	}

	public double getFscore() {

		return 2 * getPrecision() * getRecall()
				/ (getPrecision() + getRecall());
	}

	@Override
	public String toString() {

		return "TP:".concat(String.valueOf(this.tp)).concat("\tFP:")
				.concat(String.valueOf(this.fp)).concat("\tTN:")
				.concat(String.valueOf(this.tn)).concat("\tFN:")
				.concat(String.valueOf(this.fn)).concat("\nPrecision:")
				.concat(String.valueOf(getPrecision())).concat("\t")
				.concat("Recall:").concat(String.valueOf(getRecall()))
				.concat("\t").concat("Fscore:")
				.concat(String.valueOf(getFscore()));
	}
}
