package info.chenli.litway.util;

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
	public static int dot(List<Integer> vector1, List<Integer> vector2) {

		if (vector1.size() != vector2.size()) {
			throw new RuntimeException("Vectors' dimensions are not equal.");
		}

		int sum = 0;

		Iterator<Integer> iter1 = vector1.iterator();
		Iterator<Integer> iter2 = vector2.iterator();
		while (iter1.hasNext()) {
			sum = sum + (iter1.next() * iter2.next());
		}

		return sum;
	}

	/**
	 * Dot product of two vectors, one of which is a sparse vector with value 1.
	 * 
	 * @param vector
	 * @param sparseVector
	 * @return
	 */
	public static int dot(List<Integer> vector, int[] sparseVector) {

		int result = 0;

		for (int offset : sparseVector) {
			if (offset != -1) {
				result = result + vector.get(offset);
			}
		}

		return result;
	}

	/**
	 * the Euclidean norm
	 * 
	 * @param vector
	 * @return
	 */
	public static double magnitude(List<Integer> vector) {

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
	public static List<Integer> add(List<Integer> vector1, List<Integer> vector2) {

		if (vector1.size() != vector2.size()) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		List<Integer> result = new ArrayList<Integer>();

		Iterator<Integer> iter1 = vector1.iterator();
		Iterator<Integer> iter2 = vector2.iterator();
		while (iter1.hasNext()) {

			result.add(iter1.next() + iter2.next());
		}

		return result;
	}

	/**
	 * The addition of two vectors, one of which is a sparse vector with value
	 * 1.
	 * 
	 * @param vector
	 * @param sparseVector
	 * @return
	 */
	public static List<Integer> add(List<Integer> vector, int[] sparseVector) {

		List<Integer> result = new ArrayList<Integer>(vector);
		for (int offset : sparseVector) {
			if (offset == -1) {
				continue;
			}
			result.set(offset, result.get(offset) + 1);

		}

		return result;
	}

	/**
	 * The multiplication of a vector and a real number.
	 * 
	 * @param vector1
	 * @param number
	 * @return
	 */
	public static List<Double> multiply(List<Double> vector1, Double number) {

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
	public static List<Integer> subtract(List<Integer> vector1,
			List<Integer> vector2) {

		if (vector1.size() != vector2.size()) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		List<Integer> result = new ArrayList<Integer>();

		Iterator<Integer> iter1 = vector1.iterator();
		Iterator<Integer> iter2 = vector2.iterator();
		while (iter1.hasNext()) {

			result.add(iter1.next() - iter2.next());
		}

		return result;
	}

	/**
	 * 
	 * @param vector
	 * @param sparseVector
	 * @return
	 */
	public static List<Integer> subtract(List<Integer> vector,
			int[] sparseVector) {

		List<Integer> result = new ArrayList<Integer>(vector);
		for (int offset : sparseVector) {
			if (offset == -1) {
				continue;
			}
			result.set(offset, result.get(offset) - 1);

		}

		return result;
	}
}
