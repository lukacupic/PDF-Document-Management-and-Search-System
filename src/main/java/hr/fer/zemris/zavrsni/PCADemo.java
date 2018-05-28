package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.utils.VectorUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dimensionalityreduction.PCA;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author Luka Cupic
 */
public class PCADemo extends Application {

	private static RankingFunction function;
	private static double[][] vals;

	@Override
	public void start(Stage stage) {
		NumberAxis xAxis = new NumberAxis(0, 100, 1);
		NumberAxis yAxis = new NumberAxis(0, 100, 1);
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
//		try {
//			String path = "/media/chup0x/Data/FER/6. semestar/Završni rad/Corpus/dataset_txt_simple";
//			function = new CosineSimilarity(Paths.get(path));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		List<Vector> vectors = RankingFunction.documents.values().stream()
//				.map(Document::getVector)
//				.collect(Collectors.toList());
//
//		vals = VectorUtil.vectorsToMatrix(vectors);

		INDArray rand = Nd4j.rand(40, 11000);
		INDArray rand2 = Nd4j.rand(100, 60000);
//		INDArray small = Nd4j.create(new double[][]{
//				{1, 1, 1, 1},
//				{2, 2, 2, 2},
//				{3, 3, 3, 3}
//		});

		//vals = IOUtils.deserialize();
		//INDArray real = Nd4j.create(vals);
		INDArray p = PCA.pca(rand2, 2, true);
		System.out.println("Done PCA");
		System.exit(0);

		vals = p.toDoubleMatrix();
		VectorUtil.normalize(vals, 100);

		launch(args);
	}
}