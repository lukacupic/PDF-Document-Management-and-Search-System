package hr.fer.zemris.zavrsni;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.*;

/**
 * @author Luka Cupic
 */
public class GUI extends JFrame {

	public GUI() {
		setSize(600, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setTitle("Završni GUI");

		initGUI();
	}

	private void initGUI() {
		JPanel inputPanel = createInputPanel();
		add(inputPanel);
	}

	private JPanel createInputPanel() {
		JPanel inputPanel = new JPanel(new GridLayout(2, 2));

		JRadioButton male = new JRadioButton("m");
		JLabel maleLabel = new JLabel("Tekst ovdje");
		inputPanel.add(male);
		inputPanel.add(maleLabel);

		JRadioButton female = new JRadioButton("f");
		JLabel femaleLabel = new JLabel("Tekst ovdjef");
		inputPanel.add(female);
		inputPanel.add(femaleLabel);

		ButtonGroup bg = new ButtonGroup();
		bg.add(male);
		bg.add(female);

		male.setSelected(true);

		return inputPanel;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(GUI::new);
	}
}
