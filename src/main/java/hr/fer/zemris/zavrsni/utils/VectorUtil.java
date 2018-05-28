package hr.fer.zemris.zavrsni.utils;

import hr.fer.zemris.zavrsni.model.Vector;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dimensionalityreduction.PCA;
import org.nd4j.linalg.factory.Nd4j;

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

	public static double[][] pcaTransform(double[][] data) {
		INDArray m = Nd4j.create(data);
		INDArray p = PCA.pca(m, 2, true);
		return p.toDoubleMatrix();
	}

	public static double[][] pcaTransform(INDArray array) {
		INDArray p = PCA.pca(array, 2, true);
		return p.toDoubleMatrix();
	}

	public static void normalize(double[][] vectors, double max) {
		double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE;
		double ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;

		// find min and max values for x and y
		for (double[] xy : vectors) {
			if (xy[0] < xmin) xmin = xy[0];
			if (xy[0] > xmax) xmax = xy[0];

			if (xy[1] < ymin) ymin = xy[1];
			if (xy[1] > ymax) ymax = xy[1];
		}

		// scale each vector
		for (double[] xy : vectors) {
			xy[0] = max * (xy[0] - xmin) / (xmax - xmin);
			xy[1] = max * (xy[1] - ymin) / (ymax - ymin);
		}
	}
}
