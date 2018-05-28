package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luka Cupic
 */
public class VisualizationDemo {

	private static final double threshold = 0.08;

	public static void main(String[] args) throws IOException {
		Main.init(Paths.get(args[0]));
		List<Document> documents = new ArrayList<>(RankingFunction.datasetInfo.documents.values());

		DirectedSparseGraph<Document, String> g = new DirectedSparseGraph<>();
		documents.forEach(g::addVertex);

		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);

				if (d1.sim(d2) / d1.sim(d1) > threshold) {
					g.addEdge(d1.getPath() + " " + d2.getPath(), d1, d2);
				}
			}
		}

		FRLayout<Document, String> layout = new FRLayout<>(g);
		//SpringLayout<Integer, Edge> layout = new SpringLayout<>(g, e -> e.weight);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		int count = 0;
		while (!layout.done() && count < 700) {
			layout.step();
			count++;
		}

		VisualizationViewer<Document, String> vv = new VisualizationViewer<>(layout);

		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller() {
			@Override
			public String apply(Object v) {
				return ((Document) v).getPath().toFile().getParentFile().getName();
			}
		});

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().

				add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}
