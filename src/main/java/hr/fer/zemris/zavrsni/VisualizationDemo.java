package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.util.IOUtils;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luka Cupic
 */
public class VisualizationDemo {

	private static final double threshold = 0.2;

	public static void main(String[] args) {
		RankingFunction function;
//		try {
//			String path = "/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset_txt_simple";
//			function = new CosineSimilarity(Paths.get(path));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		IOUtils.serialize(RankingFunction.datasetInfo, "src/main/resources/info.ser");
//		System.exit(1);

		function = new CosineSimilarity();
		RankingFunction.datasetInfo = (RankingFunction.DatasetInfo) IOUtils.deserialize("src/main/resources/info.ser");
		List<Document> documents = new ArrayList<>(RankingFunction.datasetInfo.documents.values());

		Graph<Document, Edge> g = new DirectedSparseGraph<>();
		documents.forEach(g::addVertex);

		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);

				if (d1.sim(d2) / d1.sim(d1) > threshold) {
					g.addEdge(new Edge(1), d1, d2);
				}
			}
		}

		FRLayout<Document, Edge> layout = new FRLayout<>(g);
		//SpringLayout<Integer, Edge> layout = new SpringLayout<>(g, e -> e.weight);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		int count = 0;
		while (!layout.done() && count < 700) {
			layout.step();
			count++;
		}

		VisualizationViewer<Document, Edge> vv = new VisualizationViewer<>(layout);

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}

	static class Edge {
		public int weight;

		Edge(int weight) {
			this.weight = weight;
		}
	}
}
