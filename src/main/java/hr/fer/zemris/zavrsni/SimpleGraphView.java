package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Function;

public class SimpleGraphView {

	public SimpleGraphView() {
		// Create a graph with Integer vertices and String edges
		Graph<Integer, String> g = new SparseGraph<Integer, String>();
		for (int i = 0; i < 5; i++) g.addVertex(i);
		g.addEdge("Edge", 1, 2);
		g.addEdge("Another Edge", 1, 4);

		// Layout implements the graph drawing logic
		Layout<Integer, String> layout = new CircleLayout<>(g);
		layout.setSize(new Dimension(300, 300));

		// VisualizationServer actually displays the graph
		BasicVisualizationServer<Integer, String> vv = new BasicVisualizationServer<>(layout);
		vv.setPreferredSize(new Dimension(350, 350)); //Sets the viewing area size

		// Transformer maps the vertex number to a vertex property
		Function<Integer, Paint> vertexColor = i -> new Color(9, 67, 138, 188);

		Function<Integer, Shape> vertexSize = i -> {
			Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
			// in this case, the vertex is twice as large
			if (i == 2) return AffineTransform.getScaleInstance(2, 2).createTransformedShape(circle);
			else return circle;
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor::apply);
		vv.getRenderContext().setVertexShapeTransformer(vertexSize::apply);

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new SimpleGraphView();
	}
}