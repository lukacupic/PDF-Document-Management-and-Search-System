package hr.fer.zemris.zavrsni;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;

import java.io.File;

/**
 * @author Luka Cupic
 */
public class VisualizationDemo {

	public static void main(String[] args) {
		double[][] y = readAndProcessData("src/main/resources/data_demo.txt");
		// do something with y ...
	}

	public static double[][] readAndProcessData(String path) {
		int initial_dims = 4;
		double perplexity = 1;
		double[][] X = MatrixUtils.simpleRead2DMatrix(new File(path));

		//System.out.println(MatrixOps.doubleArrayToPrintString(X, ", ", 50, 10));

		boolean parallel = false;
		BarnesHutTSne tsne = !parallel ? new ParallelBHTsne() : new BHTSne();

		TSneConfiguration config = TSneUtils.buildConfig(X, 2, initial_dims, perplexity, 1000);
		double[][] y = tsne.tsne(config);
		normalize(y, 100);
		return y;
	}

	public static double[][] processData(double[][] X) {
		int initial_dims = X[0].length;
		double perplexity = 1;

		//System.out.println(MatrixOps.doubleArrayToPrintString(X, ", ", 50, 10));

		boolean parallel = false;
		BarnesHutTSne tsne = !parallel ? new ParallelBHTsne() : new BHTSne();

		TSneConfiguration config = TSneUtils.buildConfig(X, 2, initial_dims, perplexity, 1000);
		double[][] y = tsne.tsne(config);
		normalize(y, 100);
		return y;
	}

	private static void normalize(double[][] vectors, double max) {
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
