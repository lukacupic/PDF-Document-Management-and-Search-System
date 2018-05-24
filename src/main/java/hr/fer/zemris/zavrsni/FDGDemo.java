package hr.fer.zemris.zavrsni;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;

/**
 * @author Luka Cupic
 */
public class FDGDemo {

	public static void main(String[] args) {
		Graph<Integer, Edge> g = new DirectedSparseGraph<>();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);

		g.addEdge(new Edge(10), 1, 2);
		g.addEdge(new Edge(10), 1, 3);
		g.addEdge(new Edge(10), 2, 3);
		g.addEdge(new Edge(1), 2, 4);

		FRLayout<Integer, Edge> layout = new FRLayout<>(g);
		//SpringLayout<Integer, Edge> layout = new SpringLayout<>(g, e -> e.weight);
		layout.setSize(new Dimension(600, 600));
		layout.initialize();

		int count = 0;
		while (!layout.done() && count < 700) {
			layout.step();
			count++;
		}

		VisualizationViewer<Integer, Edge> vv = new VisualizationViewer<>(layout);

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
