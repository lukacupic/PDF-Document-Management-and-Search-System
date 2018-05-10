package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.InputProcessor;
import hr.fer.zemris.zavrsni.Main;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.readers.ConsoleReader;
import hr.fer.zemris.zavrsni.readers.TextReader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OkapiBM25 implements RankingFunction {

	private static final double k1 = 1.6;
	private static final double b = 0.75;

	/**
	 * The collection of all words from all the documents (aka. dataset).
	 */
	private Map<String, Integer> vocabulary = new HashMap<>();

	/**
	 * Holds the number of occurrences in the documents for each word from
	 * the vocabulary.
	 */
	private Map<String, Integer> wordFrequency = new LinkedHashMap<>();

	/**
	 * A map of all the documents, mapped to by their appropriate file system
	 * paths.
	 */
	private Map<Path, Document> documents = new LinkedHashMap<>();

	/**
	 * The processor for reading the Corpus' documents.
	 */
	private InputProcessor processor;

	/**
	 * Creates a new {@link CosineSimilarity} function.
	 *
	 * @param dataset the path to the dataset
	 * @throws IOException if an I/O error occurs
	 */
	public OkapiBM25(Path dataset) throws IOException {
		init(dataset);
	}

	/**
	 * Initializes the program.
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while initialization
	 */
	private void init(Path path) throws IOException {
		// Initialize document reading mechanism
		processor = new InputProcessor(Main.STOP_WORDS_PATH);

		// Initialize the dataset
		createVocabulary(path);
		initDocuments(path);
	}

	/**
	 * Creates the vocabulary by recursively reading all documents at the
	 * given path. All character that are not letters will simply be ignored.
	 * So for example, a part of the document "hitch42iker" shall be interpreted
	 * as "hitch iker".
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while creating the vocabulary
	 */
	private void createVocabulary(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				processor.setReader(new TextReader(path));
				for (String word : processor.process()) {
					if (vocabulary.containsKey(word)) continue;
					vocabulary.put(word, -1);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		// After the vocabulary has been created, iterate it and store
		// the index of each word as the word's value in the vocabulary map
		List<String> words = new ArrayList<>(vocabulary.keySet());
		vocabulary.keySet().forEach(key -> vocabulary.put(key, words.indexOf(key)));
	}

	/**
	 * Creates the TF vectors for all documents in the dataset (which are
	 * then added to the {@link #documents } map) and fills {@link #wordFrequency}
	 * with values obtained from the documents.
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while creating the documents
	 */
	private void initDocuments(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				processor.setReader(new TextReader(path));
				List<String> words = processor.process();

				Document doc = new Document(path, createTFVector(words), null, words.size());
				documents.put(path, doc);

				// Update wordFrequency for each word
				words.stream().distinct().forEach(word ->
						wordFrequency.merge(word, 1, (a, b) -> a + b)
				);

				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates the TF vector component for the given words.
	 *
	 * @param words the words of the document to build the TF
	 *              vector from
	 * @return the TF vector representation of the given document
	 */
	private Vector createTFVector(List<String> words) {
		double[] values = new double[vocabulary.size()];
		for (String word : words) {
			int wordIndex = vocabulary.get(word);
			values[wordIndex]++;
		}
		return new Vector(values);
	}

	private double calculateIDF(String word) {
		int freq = wordFrequency.get(word);
		//return Math.max(0, Math.log((documents.size() - freq + 0.5) / (freq + 0.5)));
		return Math.log(documents.size() / (double) freq);
	}

	@Override
	public List<Result> process(String query) throws IOException {
		processor.setReader(new ConsoleReader(query));
		List<String> words = processor.process();

		double avgdl = documents.values().stream().mapToLong(Document::getLength).average().getAsDouble();

		List<Result> results = new ArrayList<>();
		for (Document d : documents.values()) {
			double res = 0;
			for (String w : words) {
				double freq = d.getTFVector().get(vocabulary.get(w));
				double num = freq * (k1 + 1);
				double den = freq + k1 * (1 - b + b * (d.getLength() / avgdl));
				res += calculateIDF(w) * num / den;
			}
			results.add(new Result(res, d));
		}

		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}
}
