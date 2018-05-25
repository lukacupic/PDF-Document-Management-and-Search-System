package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The implementation of the Okapi BM25 ranking function.
 *
 * @author Luka Cupic
 * @see <a href="https://en.wikipedia.org/wiki/Cosine_similarity">
 * https://en.wikipedia.org/wiki/Cosine_similarity</a>
 */
public class CosineSimilarity extends RankingFunction {

	/**
	 * Creates a new {@link CosineSimilarity} function.
	 *
	 * @param dataset the path to the dataset
	 * @throws IOException if an I/O error occurs
	 */
	public CosineSimilarity(Path dataset) throws IOException {
		super(dataset);
	}

	@Override
	public List<Result> process(List<String> words) {
		// create document from the query
		Vector tf = createTFVector(words);
		Document inputDoc = new Document(null, null, Vector.multiply(tf, datasetInfo.idf), 0);

		// get the results
		List<Result> results = new ArrayList<>();
		for (Document d : datasetInfo.documents.values()) {
			results.add(new Result(inputDoc.sim(d), d));
		}
		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}

	@Override
	public double sim(Document d1, Document d2) {
		return d1.getVector().cos(d2.getVector());
	}
}
