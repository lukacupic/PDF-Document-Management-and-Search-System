package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.gui.GraphViewer;
import hr.fer.zemris.zavrsni.input.InputProcessor;
import hr.fer.zemris.zavrsni.input.PDFReader;
import hr.fer.zemris.zavrsni.input.QueryReader;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Luka Cupic
 */
public class GUI extends JFrame {

	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;

	private static RankingFunction function;

	public GUI() {
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setTitle("Sustav za upravljanje i pretraživanje baze PDF dokumenata");

		initGUI();
	}


	// GUI initialization

	private void initGUI() {
		chooseDataset();

		JTabbedPane tabbedPane = createTabbedPane();
		add(tabbedPane);

		pack();
	}

	private static void chooseDataset() {
		try {
			function = Initializer.init(Paths.get("/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset"));
		chooser.setDialogTitle("Choose Dataset Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				function = Initializer.init(chooser.getSelectedFile().toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.exit(1);
		}
		*/
	}

	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel panel1 = createTab1();
		tabbedPane.addTab("Graph", panel1);

		JPanel panel2 = createTab2();
		tabbedPane.addTab("Query Search", panel2);

		JPanel panel3 = createTab3();
		tabbedPane.addTab("Document Search", panel3);

		return tabbedPane;
	}

	private JPanel createTab1() {
		JPanel panel = new JPanel();
		panel.add(GraphViewer.createViewer(WIDTH, HEIGHT));
		return panel;
	}

	private JPanel createTab2() {
		JPanel panel = new JPanel(new BorderLayout());

		JTable table = createTable();
		JPanel form = createPanel2Form(table);

		panel.add(form, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		return panel;
	}

	private JPanel createTab3() {
		JPanel panel = new JPanel(new BorderLayout());

		JTable table = createTable();
		JPanel form = createPanel3Form(table);

		panel.add(form, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		return panel;
	}

	private static MouseListener createOpenDocumentListener() {
		return new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable table = (JTable) mouseEvent.getSource();

				int row = table.getSelectedRow();
				if (mouseEvent.getClickCount() == 2 && row != -1) {
					table.getSelectedRow();
					Document doc = (Document) table.getValueAt(row, 0);

					try {
						Desktop.getDesktop().open(doc.getPath().toFile());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	private static ActionListener createQuerySearchListener(JTextField textField, JTable table) {
		return (l) -> {
			List<Result> results = null;
			try {
				results = processQuery(textField.getText());
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);

			results.forEach(result -> model.addRow(new Object[]{result.getDocument(), result.getSim()}));
		};
	}

	private static ActionListener createLoadDocumentListener(JLabel label, JTable table) {
		return (l) -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle("Choose Document");

			Path document;
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				document = chooser.getSelectedFile().toPath();
			} else {
				return;
			}

			label.setText(document.toString());

			List<Result> results = null;
			try {
				results = processDocument(document);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);

			results.forEach(result -> model.addRow(new Object[]{result.getDocument(), result.getSim()}));
		};
	}

	private JTable createTable() {
		JTable table = new JTable(new DefaultTableModel(new Object[]{"Document", "Similarity"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.addMouseListener(createOpenDocumentListener());
		return table;
	}

	private JPanel createPanel2Form(JTable table) {
		JPanel form = new JPanel(new GridLayout(1, 3));

		JLabel label = new JLabel("Query");
		form.add(label);

		JTextField textField = new JTextField();
		textField.addActionListener(createQuerySearchListener(textField, table));
		form.add(textField);

		JButton button = new JButton("Search");
		button.addActionListener(createQuerySearchListener(textField, table));
		form.add(button);

		form.setBorder(BorderFactory.createTitledBorder("Enter Query"));
		return form;
	}

	private JPanel createPanel3Form(JTable table) {
		JPanel form = new JPanel(new GridLayout(1, 3));

		JLabel label = new JLabel("Document");
		form.add(label);

		JLabel docLabel = new JLabel("No document selected.");
		form.add(docLabel);

		JButton button = new JButton("Browse...");

		button.addActionListener(createLoadDocumentListener(docLabel, table));
		form.add(button);

		form.setBorder(BorderFactory.createTitledBorder("Enter Query"));
		return form;
	}

	// end of GUI initialization


	/**
	 * Processes the user Query input and returns the list of results.
	 *
	 * @return list of results
	 */
	private static List<Result> processQuery(String input) throws IOException {
		InputProcessor.setReader(new QueryReader(input));
		return function.process(InputProcessor.process());
	}

	/**
	 * Processes the user Document input and returns the list of results.
	 *
	 * @return list of results
	 */
	private static List<Result> processDocument(Path document) throws IOException {
		InputProcessor.setReader(new PDFReader(document));
		return function.process(InputProcessor.process());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(GUI::new);
	}
}
