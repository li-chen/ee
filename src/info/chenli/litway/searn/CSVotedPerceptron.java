package info.chenli.litway.searn;

import info.chenli.litway.util.MathUtil;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is based on the VotedPerceptron in weka.
 * 
 * @author Chen Li
 * 
 */
public class CSVotedPerceptron extends VotedPerceptron implements
		CostSensitiveClassifier {

	private final static Logger logger = Logger
			.getLogger(CSVotedPerceptron.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The training instances */
	private Instances m_Train = null;

	/** The filter used to get rid of missing values. */
	private ReplaceMissingValues m_ReplaceMissingValues;

	/** The filter used to make attributes numeric. */
	private NominalToBinary m_NominalToBinary;

	/** Seed used for shuffling the dataset */
	private int m_Seed = 1;

	/** The training instances added to the perceptron */
	private int[] m_Additions = null;

	/** Addition or subtraction? */
	private boolean[] m_IsAddition = null;

	/** The maximum number of alterations to the perceptron */
	private int m_MaxK = 10000;

	/** The weights for each perceptron */
	private int[] m_Weights = null;

	/** The actual number of alterations */
	private int m_K = 0;

	/** The number of iterations */
	private int m_NumIterations = 1;

	/** The exponent */
	private double m_Exponent = 1.0;

	/**
	 * Builds the ensemble of perceptrons.
	 * 
	 * @param insts
	 *            the data to train the classifier with
	 * @throws Exception
	 *             if something goes wrong during building
	 */
	public void buildClassifier(Instances insts, double[] costs)
			throws Exception {

		// can classifier handle the data?
		getCapabilities().testWithFail(insts);

		// remove instances with missing class
		insts = new Instances(insts);
		insts.deleteWithMissingClass();

		// Filter data
		m_Train = new Instances(insts);
		m_ReplaceMissingValues = new ReplaceMissingValues();
		m_ReplaceMissingValues.setInputFormat(m_Train);
		m_Train = Filter.useFilter(m_Train, m_ReplaceMissingValues);

		m_NominalToBinary = new NominalToBinary();
		m_NominalToBinary.setInputFormat(m_Train);
		m_Train = Filter.useFilter(m_Train, m_NominalToBinary);

		/** Randomize training data */
		m_Train.randomize(new Random(m_Seed));

		/** Make space to store perceptrons */
		m_Additions = new int[m_MaxK + 1];
		m_IsAddition = new boolean[m_MaxK + 1];
		m_Weights = new int[m_MaxK + 1];

		/** Compute perceptrons */
		m_K = 0;
		out: for (int it = 0; it < m_NumIterations; it++) {
			for (int i = 0; i < m_Train.numInstances(); i++) {
				Instance inst = m_Train.instance(i);
				if (!inst.classIsMissing()) {
					int prediction = makePrediction(m_K, inst);
					int classValue = (int) inst.classValue();
					if (prediction == classValue && costs[i] == 0) {
						m_Weights[m_K]++;
					} else {
						// when cost greater than 0, then interpolate the
						// learning rate alfa
						Vector<Double> vec = new Vector<Double>();
						vec.add(new Double(classValue));
						// calculate loss based on funciton Equ 3.2 in Daume
						// 2006. See Equ 4.1, 4.2 as examples in the same
						// thesis.
						double loss = Math.pow(m_Weights[m_K], prediction)
								- Math.pow(m_Weights[m_K], classValue)
								+ Math.sqrt(Math.pow(MathUtil.magnitude(vec),
										prediction));

						double alfa = loss
								/ (Math.pow(MathUtil.magnitude(vec), 2) + 1 / costs[i]);
						m_IsAddition[m_K] = (classValue == 1);
						m_Additions[m_K] = (int) (alfa * i);
						m_K++;
						m_Weights[m_K]++;
					}
					if (m_K == m_MaxK) {
						break out;
					}
				}
			}
		}
	}

	/**
	 * Compute a prediction from a perceptron
	 * 
	 * @param k
	 * @param inst
	 *            the instance to make a prediction for
	 * @return the prediction
	 * @throws Exception
	 *             if computation fails
	 */
	private int makePrediction(int k, Instance inst) throws Exception {

		double result = 0;
		for (int i = 0; i < k; i++) {
			if (m_IsAddition[i]) {
				result += innerProduct(m_Train.instance(m_Additions[i]), inst);
			} else {
				result -= innerProduct(m_Train.instance(m_Additions[i]), inst);
			}
		}
		if (result < 0) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Computes the inner product of two instances
	 * 
	 * @param i1
	 *            first instance
	 * @param i2
	 *            second instance
	 * @return the inner product
	 * @throws Exception
	 *             if computation fails
	 */
	private double innerProduct(Instance i1, Instance i2) throws Exception {

		// we can do a fast dot product
		double result = 0;
		int n1 = i1.numValues();
		int n2 = i2.numValues();
		int classIndex = m_Train.classIndex();
		for (int p1 = 0, p2 = 0; p1 < n1 && p2 < n2;) {
			int ind1 = i1.index(p1);
			int ind2 = i2.index(p2);
			if (ind1 == ind2) {
				if (ind1 != classIndex) {
					result += i1.valueSparse(p1) * i2.valueSparse(p2);
				}
				p1++;
				p2++;
			} else if (ind1 > ind2) {
				p2++;
			} else {
				p1++;
			}
		}
		result += 1.0;

		if (m_Exponent != 1) {
			return Math.pow(result, m_Exponent);
		} else {
			return result;
		}
	}

	@Override
	public CostSensitiveClassifier getOptimalPolicy(
			List<StructuredInstance> structuralInstances) {

		// By default, use the policy learned from the gold standard. So the
		// structural info isn't very important. The structured instances will
		// be converted to the normal instances.
		Instances instances = null;
		for (StructuredInstance structuredInstance : structuralInstances) {
			for (Instance instance : structuredInstance.getNodes()) {
				if (!instances.add(instance)) {
					RuntimeException e = new RuntimeException(
							"Instance couldn't be appended.");
					logger.log(Level.SEVERE, e.getMessage(), e);
					throw e;
				}
			}

		}

		// get the optimal policy by using the default perceptrons.
		CSVotedPerceptron classifier = new CSVotedPerceptron();
		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return classifier;
	}

	/**
	 * Compare two policies
	 * 
	 * @param another
	 *            policy.
	 * @return dot product of two sets of the policies' parameter
	 */
	@Override
	public int compareTo(CostSensitiveClassifier o) {

		throw new UnsupportedOperationException();
		// return (int) Math.round(Math.abs(MathUtil.dot(o.getParam(),
		// this.getParam())));
	}

	/**
	 * Main method.
	 * 
	 * @param argv
	 *            the commandline options
	 */
	public static void main(String[] argv) {
		runClassifier(new VotedPerceptron(), argv);
	}

}
