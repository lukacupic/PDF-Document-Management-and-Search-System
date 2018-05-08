package hr.fer.zemris.zavrsni.model;

/**
 * This class represents an immutable N-dimensional vector.
 *
 * @author Luka Cupic
 */
public class Vector {

	/**
	 * Represents the values of this vector.
	 */
	private double[] values;

	/**
	 * Creates a new N-dimensional vector, where the dimension is
	 * obtained from the number of provided elements.
	 *
	 * @param values the values to go in to the vector
	 */
	public Vector(double... values) {
		this.values = values;
	}

	/**
	 * Gets the norm of the vector.
	 *
	 * @return the norm of the vector
	 */
	public double norm() {
		double sum = 0;
		for (double value : values) {
			sum += value * value;
		}
		return Math.sqrt(sum);
	}

	/**
	 * Returns the dot product of this vector and the specified vector.
	 *
	 * @param other the other vector
	 * @return the value of the dot product
	 */
	public double dot(Vector other) {
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i] * other.getValues()[i];
		}
		return sum;
	}

	/**
	 * Returns the cosine of the angle between this vector and
	 * the specified vector.
	 *
	 * @param other the vector
	 */
	public double cos(Vector other) {
		return this.dot(other) / (this.norm() * other.norm());
	}

	/**
	 * Checks if this vector is a null vector.
	 *
	 * @return true if this vector is a null vector; false otherwise
	 */
	public boolean isNullVector() {
		for (double value : values) {
			if (value != 0) return false;
		}
		return true;
	}

	/**
	 * Performs the dot product between the given vectors. Meaning,
	 * for the two given vectors (with the same number of elements),
	 * e.g for the given vectors V1 and V2 where: {@code V1 = [a1, a2,
	 * ..., an]} and {@code V2 = [b1, b2, ..., bn]} the vector V is
	 * returned where {@code V = [a1*b1, a2*b2, ..., an*bn]}.
	 *
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return a new vector, where each element represents the product
	 * of the corresponding elements from the given vectors
	 */
	public static Vector multiply(Vector v1, Vector v2) {
		double[] values = new double[v1.getValues().length];

		for (int i = 0; i < v1.getValues().length; i++) {
			values[i] = v1.getValues()[i] * v2.getValues()[i];
		}
		return new Vector(values);
	}

	/**
	 * Gets the values of this vector.
	 *
	 * @return the values of this vector
	 */
	public double[] getValues() {
		return values;
	}

	public double get(int index) {
		return values[index];
	}
}