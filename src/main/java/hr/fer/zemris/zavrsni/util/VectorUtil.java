package hr.fer.zemris.zavrsni.util;

import hr.fer.zemris.zavrsni.model.Vector;

import java.util.List;

/**
 * @author Luka Cupic
 */
public class VectorUtil {

	public static double[][] vectorsToMatrix(List<Vector> vectors) {
		int n = vectors.size();
		int m = vectors.get(0).getValues().length;
		double[][] matrix = new double[n][m];

		for (int i = 0; i < vectors.size(); i++) {
			Vector vector = vectors.get(i);
			matrix[i] = vector.getValues();
		}
		return matrix;
	}
}
