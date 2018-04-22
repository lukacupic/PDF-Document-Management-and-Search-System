package hr.fer.zemris.zavrsni.functions;

import hr.fer.zemris.zavrsni.Main;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.readers.DocumentReader;
import hr.fer.zemris.zavrsni.readers.decorators.DocumentStemmer;
import hr.fer.zemris.zavrsni.readers.decorators.StopFilter;
import hr.fer.zemris.zavrsni.util.Stemmer2;
import hr.fer.zemris.zavrsni.util.TextUtil;

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
import java.util.stream.Collectors;

public class CosineSimilarity implements RankingFunction {

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
	 * A helper IDF vector which holds the IDF components for each of the
	 * words from the vocabulary.
	 */
	private Vector idf;

	/**
	 * The reader for reading the Corpus' documents.
	 */
	private DocumentReader reader;

	/**
	 * Creates a new {@link CosineSimilarity} function.
	 *
	 * @param dataset the path to the dataset
	 * @param reader  the reader to use for reading the documents
	 * @throws IOException if an I/O error occurs
	 */
	public CosineSimilarity(Path dataset, DocumentReader reader) throws IOException {
		this.reader = reader;
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
		DocumentStemmer stemmer = new DocumentStemmer(reader);
		reader = new StopFilter(stemmer, Main.STOP_WORDS_PATH);

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
				for (String word : reader.readDocument(path)) {
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
				List<String> words = reader.readDocument(path);

				Document doc = new Document(path, createTFVector(words), null, words.size());
				documents.put(path, doc);

				// Update wordFrequency for each word
				words.stream().distinct().forEach(word ->
						wordFrequency.merge(word, 1, (a, b) -> a + b)
				);

				return FileVisitResult.CONTINUE;
			}
		});
		createIDFVector();
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

	/**
	 * Creates the "main" IDF vector which represents the words'
	 * frequencies in all of the documents.
	 */
	private void createIDFVector() {
		double[] values = new double[vocabulary.size()];

		for (String word : vocabulary.keySet()) {
			int freq = wordFrequency.get(word);
			values[vocabulary.get(word)] = Math.log(documents.size() / (double) freq);
		}
		idf = new Vector(values);

		for (Document d : documents.values()) {
			d.setVector(Vector.multiply(d.getTFVector(), idf));
		}
	}

	/**
	 * Compares the given document object to all other documents in the
	 * collection, compares them, and returns the list of Result objects,
	 * encapsulating the similarity coefficients representing the similarity
	 * in respect to the provided document.
	 *
	 * @param doc the document
	 * @return a list of top 10 search results (the top 10 result with the
	 * highest similarity coefficients)
	 */
	private List<Result> getResults(Document doc) {
		List<Result> results = new ArrayList<>();
		for (Document d : documents.values()) {
			results.add(new Result(doc.sim(d), d));
		}
		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}

	public List<Result> process(String query) {
		List<String> words = TextUtil.getWordsFromText(query);
		Stemmer2 stemmer = new Stemmer2();
		words = words.stream().map(stemmer::stripAffixes).collect(Collectors.toList());
		words.retainAll(vocabulary.keySet());


		Vector tf = createTFVector(words);
		Document inputDoc = new Document(null, null, Vector.multiply(tf, idf), 0);

		return getResults(inputDoc);
	}
}
