package hr.fer.zemris.zavrsni.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Luka Cupic
 */
public class SerializeUtil {

	public static void searilizeDocMatrix(double[][] documentsMatrix) {
		try {
			FileOutputStream fileOut = new FileOutputStream("src/main/resources/docsMatrix.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(documentsMatrix);
			out.close();
			fileOut.close();
		} catch (IOException ex) {
		}
	}

	public static double[][] deserializeDocMatrix() {
		double[][] matrix = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("src/main/resources/docsMatrix.ser"));
			matrix = (double[][]) in.readObject();
			in.close();
		} catch (Exception e) {
		}
		return matrix;
	}
}
