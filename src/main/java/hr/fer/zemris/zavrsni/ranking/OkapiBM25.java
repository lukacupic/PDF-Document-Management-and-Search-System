package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
	 * The default constructor. Used when constructing the object
	 * through deserialization.
	 */
	public OkapiBM25() {
		super();
	}

	/**
	 * Creates a new {@link CosineSimilarity} function.
	 *
	 * @param dataset the path to the dataset
	 * @throws IOException if an I/O error occurs
	 */
	public OkapiBM25(Path dataset) throws IOException {
		super(dataset);
	}

	@Override
	public List<Result> process(List<String> words) {
		double avgdl = calculateAvgdl();

		// calculate the results
		List<Result> results = new ArrayList<>();
		for (Document d : datasetInfo.documents.values()) {
			double score = processOne(words, d, avgdl);
			results.add(new Result(score, d));
		}

		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}

	/**
	 * Performs the BM25 calculation for the given document and query.
	 *
	 * @param words the list of words from the query
	 * @param d     the document
	 * @param avgdl the average length of the documents
	 * @return the similarity measure of the given document and query
	 */
	private static double processOne(List<String> words, Document d, double avgdl) {
		double score = 0;
		for (String w : words) {
			Integer wordIndex = datasetInfo.vocabulary.get(w);
			if (wordIndex == null) continue;

			double freq = d.getTFVector().get(wordIndex);
			double num = freq * (k1 + 1);
			double den = freq + k1 * (1 - b + b * (d.getLength() / avgdl));
			score += calculateIDF(w) * num / den;
		}
		return score;
	}

	/**
	 * Calculates the avgdl parameter.
	 *
	 * @return the avgdl value, as defined in the BM25 method
	 */
	private static double calculateAvgdl() {
		return datasetInfo.documents.values().stream()
				.mapToLong(Document::getLength)
				.average()
				.getAsDouble();
	}

	/**
	 * A helper method for calculating the IDF of the given
	 * word (and the collection of documents).
	 *
	 * @param word the word
	 * @return the IDF value of all the documents and the given
	 * word
	 */
	private static double calculateIDF(String word) {
		int freq = datasetInfo.wordFrequency.get(word);
		//return Math.log((documents.size() - freq + 0.5) / (freq + 0.5));
		return Math.log(datasetInfo.documents.size() / (double) freq);
	}

	@Override
	public double sim(Document d1, Document d2) {
		List<String> words = getWordsFrom(d1);
		return processOne(words, d2, calculateAvgdl());
	}

	/**
	 * Returns all the words contained in the given document.
	 *
	 * @param d the document
	 * @return all the words contained in the given document
	 */
	private static List<String> getWordsFrom(Document d) {
		List<String> words = new ArrayList<>();

		for (Map.Entry<String, Integer> e : datasetInfo.vocabulary.entrySet()) {
			if (d.getVector().getValues()[e.getValue()] != 0) {
				words.add(e.getKey());
			}
		}
		return words;
	}
}