package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.util.VectorUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luka Cupic
 */
public class GUIDemo extends Application {

	private static RankingFunction function;

	@Override
	public void start(Stage stage) {
		List<Vector> vectors = RankingFunction.documents.values().stream()
				.map(Document::getVector)
				.collect(Collectors.toList());

		double[][] vals = VectorUtil.vectorsToMatrix(vectors);
		vals = VisualizationDemo.processData(vals);

		NumberAxis xAxis = new NumberAxis(0, 10, 1);
		NumberAxis yAxis = new NumberAxis(-100, 500, 100);

		ScatterChart<Number, Number> sc = new ScatterChart<>(xAxis, yAxis);
		XYChart.Series<Number, Number> series = new XYChart.Series<>();

		for (double[] val : vals) {
			series.getData().add(new XYChart.Data<>(val[0], val[1]));
		}
		sc.getData().addAll(series);

		Scene scene = new Scene(sc, 500, 400);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		try {
			String path = "/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset_txt_simple";
			function = new CosineSimilarity(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		launch(args);
	}
}









