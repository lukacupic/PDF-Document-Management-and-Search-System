package hr.fer.zemris.zavrsni.gui;

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

import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luka Cupic
 */
public class GraphViewer {

	private static Color[] colors = new Color[]{
			new Color(82, 129, 190),
			new Color(37, 161, 48),
			new Color(191, 0, 4),
			new Color(229, 116, 0),
			new Color(215, 70, 191),
			new Color(30, 215, 211),
			new Color(11, 1, 215),
			new Color(2, 86, 0),
			new Color(212, 215, 17),
			new Color(112, 215, 123),
			new Color(0, 8, 106),
			new Color(109, 0, 2),
			new Color(137, 41, 197),
			new Color(105, 79, 54),
			new Color(168, 164, 168),
			new Color(96, 91, 95),
			new Color(214, 148, 103),
			new Color(123, 119, 0),
			new Color(30, 120, 120),
			new Color(94, 0, 80),
			new Color(170, 113, 95),

	};

	private static double threshold = 0.07;

	public static VisualizationViewer createViewer(int width, int height) {
		List<Document> documents = new ArrayList<>(RankingFunction.datasetInfo.documents.values());

		DirectedSparseGraph<Document, String> g = new DirectedSparseGraph<>();
		documents.forEach(g::addVertex);

		initGraph(g, documents);

		FRLayout<Document, String> layout = new FRLayout<>(g);
		layout.setSize(new Dimension(width, height));
		layout.initialize();

		layout.setRepulsionMultiplier(1);

		while (!layout.done()) {
			layout.step();
		}

		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		int k = (int) Math.sqrt(documents.size() / (double) 2);
		Map<Document, Integer> clusterMap = performClustering(documents, layout, k);

		return createVV(layout, g, clusterMap);
	}

	private static void initGraph(DirectedSparseGraph<Document, String> g, List<Document> documents) {
		RankingFunction.DatasetInfo.DocumentPair pair = new RankingFunction.DatasetInfo.DocumentPair();
		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);
				pair.setDocuments(d1, d2);

				double sim = RankingFunction.datasetInfo.similarities.get(pair);

				if (sim > threshold) {
					g.addEdge(d1.hashCode() + " " + d2.hashCode(), d1, d2);
				}
			}
		}
	}

	private static Map<Document, Integer> performClustering(List<Document> documents, AbstractLayout<Document, String> layout, int k) {
		Map<Document, Integer> clusterMap = new HashMap<>();

		List<DocumentLocation> clusterInput = new ArrayList<>();
		documents.forEach(document -> clusterInput.add(new DocumentLocation(document, layout)));

		KMeansPlusPlusClusterer<DocumentLocation> clusterer = new KMeansPlusPlusClusterer<>(k, 10000);
		List<CentroidCluster<DocumentLocation>> clusterResults = clusterer.cluster(clusterInput);

		for (int i = 0; i < clusterResults.size(); i++) {
			CentroidCluster<DocumentLocation> cluster = clusterResults.get(i);
			for (DocumentLocation docLoc : cluster.getPoints()) {
				docLoc.document.setCluster(i);
				clusterMap.put(docLoc.document, i);
			}
		}
		return clusterMap;
	}

	private static VisualizationViewer<Document, String> createVV(FRLayout<Document, String> layout,
	                                                              DirectedSparseGraph<Document, String> g,
	                                                              Map<Document, Integer> clusterMap) {
		VisualizationViewer<Document, String> vv = new VisualizationViewer<>(layout);

		vv.getRenderContext().setEdgeDrawPaintTransformer(input -> new Color(163, 163, 163));
		vv.getRenderContext().setEdgeArrowPredicate(input -> false);
		vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
		vv.getRenderContext().setEdgeStrokeTransformer(input -> new BasicStroke(0.2f));

		vv.getRenderContext().setVertexStrokeTransformer(input -> new BasicStroke(0.1f));
		vv.getRenderContext().setVertexShapeTransformer(input -> new Ellipse2D.Double(-10, -10, 10, 10));
		vv.getRenderContext().setVertexFillPaintTransformer(d -> colors[clusterMap.get(d)]);
		vv.setVertexToolTipTransformer(input -> {
			if (input == null) return "[Unknown]";

			Path path = input.getPath();
			return path != null ? path.toFile().getName() : "[Unknown]";
		});

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

		return vv;
	}

	// wrapper class
	private static class DocumentLocation implements Clusterable {

		double[] point = new double[2];
		Document document;

		public DocumentLocation(Document document, AbstractLayout<Document, String> layout) {
			this.document = document;
			point[0] = layout.getX(document);
			point[1] = layout.getY(document);
		}

		public double[] getPoint() {
			return point;
		}
	}

}