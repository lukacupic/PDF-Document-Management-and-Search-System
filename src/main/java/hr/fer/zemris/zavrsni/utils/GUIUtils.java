package hr.fer.zemris.zavrsni.utils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Luka Cupic
 */
public class GUIUtils {

	public static void showErrorMessage(JFrame frame, String text) {
		JOptionPane.showMessageDialog(frame, text, "Error!", JOptionPane.PLAIN_MESSAGE);
	}
}
