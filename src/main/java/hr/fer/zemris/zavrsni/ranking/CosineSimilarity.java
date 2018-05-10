package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.readers.ConsoleReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
	public List<Result> process(String query) throws IOException {
		processor.setReader(new ConsoleReader(query));
		List<String> words = processor.process();

		// create document from the query
		Vector tf = createTFVector(words);
		Document inputDoc = new Document(null, null, Vector.multiply(tf, idf), 0);

		// get the results
		List<Result> results = new ArrayList<>();
		for (Document d : documents.values()) {
			results.add(new Result(inputDoc.sim(d), d));
		}
		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}
}
