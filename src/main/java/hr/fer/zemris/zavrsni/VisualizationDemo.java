package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
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

	private static Color[] colors = new Color[]{
			new Color(72, 118, 190),
			new Color(42, 161, 33),
			new Color(158, 18, 22),
			new Color(214, 156, 43),
			new Color(206, 18, 215),
			new Color(30, 215, 211),
	};

	public static void main(String[] args) throws IOException {
		Main.init(Paths.get(args[0]));
		List<Document> documents = new ArrayList<>(RankingFunction.datasetInfo.documents.values());

		DirectedSparseGraph<Document, Edge> g = new DirectedSparseGraph<>();
		documents.forEach(g::addVertex);

		RankingFunction.DatasetInfo.DocumentPair pair = new RankingFunction.DatasetInfo.DocumentPair();
		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);
				pair.setDocuments(d1, d2);

				//double sim = d1.sim(d2) / d1.sim(d1);
				double sim = RankingFunction.datasetInfo.similarities.get(pair);

				if (sim > threshold) {
					g.addEdge(new Edge(sim), d1, d2);
				}
			}
		}

		FRLayout<Document, Edge> layout = new FRLayout<>(g);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		layout.setRepulsionMultiplier(1);

		while (!layout.done()) {
			layout.step();
		}

		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		int k = (int) Math.sqrt(documents.size() / (double) 2);
		performClustering(documents, layout, k);

		// -- visualize --

		VisualizationViewer<Document, Edge> vv = new VisualizationViewer<>(layout);

		vv.getRenderContext().setEdgeDrawPaintTransformer(input -> new Color(163, 163, 163));
		vv.getRenderContext().setEdgeArrowPredicate(input -> false);
		vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
		vv.getRenderContext().setEdgeStrokeTransformer(input -> new BasicStroke(0.2f));

		vv.getRenderContext().setVertexStrokeTransformer(input -> new BasicStroke(0.1f));
		vv.getRenderContext().setVertexShapeTransformer(input -> new Ellipse2D.Double(-10, -10, 10, 10));
		vv.getRenderContext().setVertexFillPaintTransformer(d -> colors[d.getCluster()]);
		vv.setVertexToolTipTransformer(input -> input.getPath().toFile().getName());

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

	private static int getK(List<Document> documents, AbstractLayout<Document, Edge> layout) {
		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		List<Double> sums = new ArrayList<>();
		for (int k = 2; k < 25; k++) {
			KMeansPlusPlusClusterer<DocumentLocation> clusterer = new KMeansPlusPlusClusterer<>(k, 10000);
			List<CentroidCluster<DocumentLocation>> clusterResults = clusterer.cluster(clusterInput);

			double sum = 0;
			for (int i = 0; i < clusterResults.size(); i++) {
				CentroidCluster<DocumentLocation> cluster = clusterResults.get(i);
				for (DocumentLocation docLoc : cluster.getPoints()) {
					docLoc.getDocument().setCluster(i);
					double[] cc = cluster.getCenter().getPoint();
					double[] dp = docLoc.getPoint();
					sum += dist(cc, dp);
				}
			}
			sums.add(sum);
		}
		return 6;
	}

	private static void performClustering(List<Document> documents, AbstractLayout<Document, Edge> layout, int k) {
		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		KMeansPlusPlusClusterer<DocumentLocation> clusterer = new KMeansPlusPlusClusterer<>(6, 10000);
		List<CentroidCluster<DocumentLocation>> clusterResults = clusterer.cluster(clusterInput);

		for (int i = 0; i < clusterResults.size(); i++) {
			CentroidCluster<DocumentLocation> cluster = clusterResults.get(i);
			for (DocumentLocation docLoc : cluster.getPoints()) {
				docLoc.getDocument().setCluster(i);
			}
		}
	}

	private static double dist(double[] p1, double[] p2) {
		return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
	}

	private static class Edge {
		double weight;

		public Edge(double weight) {
			this.weight = weight;
		}
	}

	// wrapper class
	private static class DocumentLocation implements Clusterable {

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
