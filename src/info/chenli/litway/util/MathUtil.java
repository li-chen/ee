package info.chenli.litway.util;

import info.chenli.classifier.SparseVector;

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
	// public static int dot(List<Integer> vector1, List<Integer> vector2) {
	//
	// if (vector1.size() != vector2.size()) {
	// throw new RuntimeException("Vectors' dimensions are not equal.");
	// }
	//
	// int sum = 0;
	//
	// Iterator<Integer> iter1 = vector1.iterator();
	// Iterator<Integer> iter2 = vector2.iterator();
	// while (iter1.hasNext()) {
	// sum = sum + (iter1.next() * iter2.next());
	// }
	//
	// return sum;
	// }

	public static int dot(List<SparseVector> sparseVectors, List<Integer> vector) {

		int offset = 0;
		for (SparseVector sparseVector : sparseVectors) {
			if (-1 != sparseVector.getPosition()) {
				int index = offset + sparseVector.getPosition();
				vector.set(index, vector.get(index) * sparseVector.getValue());
			} else {
				for (int i = offset; i < offset + sparseVector.getLength(); i++) {
					vector.set(i, 0);
				}
			}
			offset = offset + sparseVector.getLength();
		}

		if (offset != vector.size()) {
			throw new RuntimeException("Vectors' dimensions are not equal.");
		}

		int sum = 0;
		Iterator<Integer> iter = vector.iterator();
		while (iter.hasNext()) {
			sum = sum + iter.next();
		}

		return sum;
	}

	/**
	 * the Euclidean norm
	 * 
	 * @param vector
	 * @return
	 */
	// public static double magnitude(List<Integer> vector) {
	//
	// return Math.sqrt(dot(vector, vector));
	// }

	/**
	 * The addition of two vectors.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 * @throws IllegalArgumentException
	 */
	// public static List<Integer> add(List<Integer> vector1, List<Integer>
	// vector2)
	// throws IllegalArgumentException {
	//
	// if (vector1.size() != vector2.size()) {
	// throw new IllegalArgumentException(
	// "The vectors have different dimentions.");
	// }
	//
	// List<Integer> result = new ArrayList<Integer>();
	//
	// Iterator<Integer> iter1 = vector1.iterator();
	// Iterator<Integer> iter2 = vector2.iterator();
	// while (iter1.hasNext()) {
	//
	// result.add(iter1.next() + iter2.next());
	// }
	//
	// return result;
	// }

	public static List<Integer> add(List<Integer> vector,
			List<SparseVector> sparseVectors) throws IllegalArgumentException {

		int offset = 0;
		for (SparseVector sparseVector : sparseVectors) {

			if (sparseVector.getPosition() != -1) {
				int index = offset + sparseVector.getPosition();
				vector.set(index, vector.get(index) + sparseVector.getValue());
			}

			offset = offset + sparseVector.getLength();
		}

		if (vector.size() != offset) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		return vector;
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
	// public static List<Integer> subtract(List<Integer> vector1,
	// List<Integer> vector2) throws IllegalArgumentException {
	//
	// if (vector1.size() != vector2.size()) {
	// throw new IllegalArgumentException(
	// "The vectors have different dimentions.");
	// }
	//
	// List<Integer> result = new ArrayList<Integer>();
	//
	// Iterator<Integer> iter1 = vector1.iterator();
	// Iterator<Integer> iter2 = vector2.iterator();
	// while (iter1.hasNext()) {
	//
	// result.add(iter1.next() - iter2.next());
	// }
	//
	// return result;
	// }

	public static List<Integer> subtract(List<Integer> vector,
			List<SparseVector> sparseVectors) throws IllegalArgumentException {

		int offset = 0;
		for (SparseVector sparseVector : sparseVectors) {

			if (-1 != sparseVector.getPosition()) {
				int index = offset + sparseVector.getPosition();
				vector.set(index, vector.get(index) - sparseVector.getValue());
			}

			offset = offset + sparseVector.getLength();
		}

		if (vector.size() != offset) {
			throw new IllegalArgumentException(
					"The vectors have different dimentions.");
		}

		return vector;
	}

}
