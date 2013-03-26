package info.chenli.ee.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MathUtil {

	/**
	 * Dot product
	 * 
	 * @param vector1
	 * @param vector2
	 * @return the inner product of this Vector a and b
	 */
	public static double dot(List<Double> vector1, List<Double> vector2) {

		if (vector1.size() != vector2.size()) {
			throw new RuntimeException("Vectors' dimensions are not equal.");
		}

		double sum = 0.0;

		Iterator<Double> iter1 = vector1.iterator();
		Iterator<Double> iter2 = vector2.iterator();
		while (iter1.hasNext()) {
			sum = sum + (iter1.next() * iter2.next());
		}

		return sum;
	}

	/**
	 * the Euclidean norm
	 * 
	 * @param vector
	 * @return
	 */
	public static double magnitude(List<Double> vector) {

		return Math.sqrt(dot(vector, vector));
	}

	/**
	 * The addition of two vectors.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static List<Double> add(List<Double> vector1, List<Double> vector2)
			throws IllegalArgumentException {

		if (vector1.size() != vector2.size()) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		List<Double> result = new ArrayList<Double>();

		Iterator<Double> iter1 = vector1.iterator();
		Iterator<Double> iter2 = vector2.iterator();
		while (iter1.hasNext()) {

			result.add(iter1.next() + iter2.next());
		}

		return result;
	}

	/**
	 * The multiplication of a vector and a real number.
	 * 
	 * @param vector1
	 * @param number
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static List<Double> multiply(List<Double> vector1, Double number)
			throws IllegalArgumentException {

		List<Double> result = new ArrayList<Double>();

		Iterator<Double> iter1 = vector1.iterator();

		while (iter1.hasNext()) {

			result.add(iter1.next() + number);
		}

		return result;
	}

	/**
	 * The first given vector subtract the second one.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static List<Double> subtract(List<Double> vector1,
			List<Double> vector2) throws IllegalArgumentException {

		if (vector1.size() != vector2.size()) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		List<Double> result = new ArrayList<Double>();

		Iterator<Double> iter1 = vector1.iterator();
		Iterator<Double> iter2 = vector2.iterator();
		while (iter1.hasNext()) {

			result.add(iter1.next() - iter2.next());
		}

		return result;
	}

}
