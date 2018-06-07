package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.gui.VisualizationDemo;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Luka Cupic
 */
public class GUI extends JFrame {

	private JTabbedPane tabbedPane;

	public GUI() {
		setSize(600, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setTitle("Sustav za upravljanje i pretraživanje baze PDF dokumenata");

		initGUI();
	}

	private void initGUI() {
		//setLayout(new BorderLayout());

		chooseDataset();

		tabbedPane = createTabbedPane();
		add(tabbedPane);

		//add(VisualizationDemo.createViewer());
		pack();
	}

	private static void chooseDataset() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setDialogTitle("Choose Dataset Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				//Main.init(chooser.getCurrentDirectory().toPath());
				Main.init(Paths.get("/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset"));//lose je hardkodirat path
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.exit(1);
		}
	}

	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel panel1 = createTab1();
		tabbedPane.addTab("Pane 1", panel1);

		JPanel panel2 = createTab2();
		tabbedPane.addTab("Pane 2", panel2);

		JPanel panel3 = createTab3();
		tabbedPane.addTab("Pane 3", panel3);

		return tabbedPane;
	}

	private JPanel createTab1() {
		JPanel panel = new JPanel();
		panel.add(VisualizationDemo.createViewer());
		return panel;
	}

	private JPanel createTab2() {
		return new JPanel();
	}

	private JPanel createTab3() {
		return new JPanel();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(GUI::new);
//		chooseDataset();
//		VisualizationDemo.main(null);
	}
}
