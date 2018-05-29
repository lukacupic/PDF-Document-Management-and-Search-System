package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luka Cupic
 */
public class VisualizationDemo {

	private static final double threshold = 0.07;

	public static void main(String[] args) throws IOException {
		Main.init(Paths.get(args[0]));
		List<Document> documents = new ArrayList<>(RankingFunction.datasetInfo.documents.values());

		DirectedSparseGraph<Document, Edge> g = new DirectedSparseGraph<>();
		documents.forEach(g::addVertex);

		double t1 = System.currentTimeMillis();
		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);

				double sim = d1.sim(d2) / d1.sim(d1);
				if (sim > threshold) {
					g.addEdge(new Edge(sim), d1, d2);
				}
			}
		}
		double t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000);

		FRLayout<Document, Edge> layout = new FRLayout<>(g);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		layout.setRepulsionMultiplier(1);

		while (!layout.done()) {
			layout.step();
		}

		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		KMeansPlusPlusClusterer<DocumentLocation> clusterer = new KMeansPlusPlusClusterer<>(5, 10000);
		List<CentroidCluster<DocumentLocation>> clusterResults = clusterer.cluster(clusterInput);

		Color[] colors = new Color[]{
				new Color(72, 118, 190),
				new Color(42, 161, 33),
				new Color(158, 18, 22),
				new Color(214, 156, 43),
				new Color(206, 18, 215),
		};

		// output the clusters
		for (int i = 0; i < clusterResults.size(); i++) {
			CentroidCluster<DocumentLocation> cluster = clusterResults.get(i);
			for (DocumentLocation docLoc : cluster.getPoints()) {
				docLoc.getDocument().setCluster(i);
			}
		}

		// -- visualize --

		VisualizationViewer<Document, Edge> vv = new VisualizationViewer<>(layout);

		vv.getRenderContext().setEdgeDrawPaintTransformer(input -> new Color(163, 163, 163));
		vv.getRenderContext().setEdgeArrowPredicate(input -> false);
		vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
		vv.getRenderContext().setEdgeStrokeTransformer(input -> new BasicStroke(0.2f));

		vv.getRenderContext().setVertexLabelTransformer(input -> input.getPath().toFile().getParentFile().getName());
		vv.getRenderContext().setVertexStrokeTransformer(input -> new BasicStroke(0.1f));
		vv.getRenderContext().setVertexShapeTransformer(input -> new Ellipse2D.Double(-10, -10, 10, 10));
		vv.getRenderContext().setVertexFillPaintTransformer(d -> colors[d.getCluster()]);

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

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}

	private static class Edge {
		double weight;

		public Edge(double weight) {
			this.weight = weight;
		}
	}

	// wrapper class
	public static class DocumentLocation implements Clusterable {

		private double[] points;
		private Document document;

		public DocumentLocation(Document document, AbstractLayout<Document, Edge> layout) {
			this.document = document;
			points = new double[2];
			points[0] = layout.getX(document);
			points[1] = layout.getY(document);
		}

		public double[] getPoint() {
			return points;
		}

		public Document getDocument() {
			return document;
		}
	}

}
