package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.readers.ConsoleReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The implementation of the Okapi BM25 ranking function.
 *
 * @author Luka Cupic
 * @see <a href="https://en.wikipedia.org/wiki/Okapi_BM25">
 * https://en.wikipedia.org/wiki/Okapi_BM25</a>
 */
public class OkapiBM25 extends RankingFunction {

	/**
	 * The 'k1' constant.
	 */
	private static final double k1 = 1.6;

	/**
	 * The 'b' constant.
	 */
	private static final double b = 0.75;

	/**
	 * Creates a new {@link CosineSimilarity} function.
	 *
	 * @param dataset the path to the dataset
	 * @throws IOException if an I/O error occurs
	 */
	public OkapiBM25(Path dataset) throws IOException {
		super(dataset);
	}

	/**
	 * A helper method for calculating the IDF of the given
	 * word (and the collection of documents).
	 *
	 * @param word the word
	 * @return the IDF value of all the documents and the given
	 * word
	 */
	private double calculateIDF(String word) {
		int freq = wordFrequency.get(word);
		//return Math.max(0, Math.log((documents.size() - freq + 0.5) / (freq + 0.5)));
		return Math.log(documents.size() / (double) freq);
	}

	@Override
	public List<Result> process(String query) throws IOException {
		processor.setReader(new ConsoleReader(query));
		List<String> words = processor.process();

		double avgdl = documents.values().stream()
				.mapToLong(Document::getLength)
				.average()
				.getAsDouble();

		// calculate the results
		List<Result> results = new ArrayList<>();
		for (Document d : documents.values()) {
			double score = 0;
			for (String w : words) {
				Integer wordIndex = vocabulary.get(w);
				if (wordIndex == null) continue;

				double freq = d.getTFVector().get(wordIndex);
				double num = freq * (k1 + 1);
				double den = freq + k1 * (1 - b + b * (d.getLength() / avgdl));
				score += calculateIDF(w) * num / den;
			}
			results.add(new Result(score, d));
		}

		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}
}