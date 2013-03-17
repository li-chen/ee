package info.chenli.ee.util;

import java.util.Vector;

public class MathUtil {

	/**
	 * Dot product
	 * 
	 * @param vector1
	 * @param vector2
	 * @return the inner product of this Vector a and b
	 */
	public static double dot(Vector<Double> vector1, Vector<Double> vector2) {

		if (vector1.size() != vector2.size()) {
			throw new RuntimeException("Vectors' dimensions are not equal.");
		}

		double sum = 0.0;

		for (int i = 0; i < vector1.size(); i++)
			sum = sum + ((Double) vector1.get(i) * (Double) vector1.get(i));

		return sum;
	}

	/**
	 * the Euclidean norm
	 * 
	 * @param vector
	 * @return
	 */
	public static double magnitude(Vector<Double> vector) {

		return Math.sqrt(dot(vector, vector));
	}

}
