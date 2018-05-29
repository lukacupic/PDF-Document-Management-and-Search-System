package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Luka Cupic
 */
public class VisualizationDemo {

	private static final double threshold = 0.07;

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

		FRLayout2<Document, String> layout = new FRLayout2<>(g);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		layout.setRepulsionMultiplier(0.8);

		while (!layout.done()) {
			layout.step();
		}

		VisualizationViewer<Document, String> vv = new VisualizationViewer<>(layout);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller() {
			@Override
			public String apply(Object v) {
				return ((Document) v).getPath().toFile().getParentFile().getName();
			}
		});

		Function<Document, Paint> vertexColor = d -> new Color(9, 67, 138, 188);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor::apply);

		vv.addGraphMouseListener(new GraphMouseListener<Document>() {
			@Override
			public void graphClicked(Document d, MouseEvent me) {
				if (SwingUtilities.isLeftMouseButton(me)) {
					try {
						Desktop.getDesktop().open(d.getPath().toFile());
					} catch (IOException e) {
					}
				}
				me.consume();
			}

			@Override
			public void graphPressed(Document document, MouseEvent me) {
			}

			@Override
			public void graphReleased(Document document, MouseEvent me) {
			}
		});

		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(graphMouse);

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}
