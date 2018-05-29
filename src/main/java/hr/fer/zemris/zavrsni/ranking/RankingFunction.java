package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.InputProcessor;
import hr.fer.zemris.zavrsni.Main;
import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.readers.FileReader;
import hr.fer.zemris.zavrsni.readers.PDFReader;
import hr.fer.zemris.zavrsni.utils.IOUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RankingFunction {

	/**
	 * The object holding all the information about the function
	 * dataset.
	 */
	public static DatasetInfo datasetInfo = new DatasetInfo();

	/**
	 * The ranking function used to compare the documents.
	 */
	private static RankingFunction function;

	/**
	 * The concrete reader responsible for reading the documents
	 * from the dataset.
	 */
	private static FileReader reader = new PDFReader();

	/**
	 * The default constructor. Used when constructing the object
	 * through deserialization.
	 */
	public RankingFunction() {
		function = this;
	}

	/**
	 * Creates a new {@link RankingFunction} function.
	 *
	 * @param dataset the path to the dataset
	 * @throws IOException if an I/O error occurs
	 */
	public RankingFunction(Path dataset) throws IOException {
		this();
		init(dataset);
		// we're here the first time, so serialize the dataset info
		IOUtils.serialize(datasetInfo, Main.DATASET_INFO_FILENAME);
	}


	// ----------------------------- abstract methods -----------------------------

	/**
	 * Parses the given query, processes it, and returns a list of results.
	 *
	 * @param words the words from the input source (console, document, ...)
	 * @return the list of results
	 * @throws IOException if an error occurs while processing
	 */
	public abstract List<Result> process(List<String> words) throws IOException;

	/**
	 * Compares the given two documents and returns the result.
	 *
	 * @param d1 the first document
	 * @param d2 the second document
	 */
	public abstract double sim(Document d1, Document d2);

	// -------------------------- end of abstract methods --------------------------


	/**
	 * Initializes the program.
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while initialization
	 */
	private void init(Path path) throws IOException {
		// Initialize document reading mechanism
		InputProcessor.setStopWords(Main.STOP_WORDS_PATH);
		InputProcessor.setReader(reader);

		// Initialize the dataset
		createVocabulary(path);
		initDocuments(path);
		calculateSimilarities();
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
				List<String> words = readWords(path);
				for (String word : words) {
					if (datasetInfo.vocabulary.containsKey(word)) continue;
					datasetInfo.vocabulary.put(word, -1);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		// After the vocabulary has been created, iterate it and store
		// the index of each word as the word's value in the vocabulary map
		List<String> words = new ArrayList<>(datasetInfo.vocabulary.keySet());
		datasetInfo.vocabulary.keySet().forEach(key -> datasetInfo.vocabulary.put(key, words.indexOf(key)));
	}

	/**
	 * Creates the TF vectors for all documents in the dataset (which are
	 * then added to the {@link DatasetInfo#documents} map) and fills {@link DatasetInfo#wordFrequency}
	 * with values obtained from the documents.
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while creating the documents
	 */
	private void initDocuments(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				List<String> words = readWords(path);

				Document doc = new Document(path, createTFVector(words), null, words.size());
				datasetInfo.documents.put(path.toString(), doc);

				// Update wordFrequency for each word
				words.stream().distinct().forEach(word ->
						datasetInfo.wordFrequency.merge(word, 1, (a, b) -> a + b)
				);
				return FileVisitResult.CONTINUE;
			}
		});
		createIDFVector();
	}

	/**
	 * Returns a list of filtered (removal of stop words, stemming etc.) words
	 * read from the given document by the current InputProcessor.
	 *
	 * @param path the path to the document to read
	 * @return a list of words extracted from the document
	 * @throws IOException if the document cannot be read
	 */
	private List<String> readWords(Path path) throws IOException {
		// implicitly change the document the InputProcessor is reading
		reader.setPath(path);
		return InputProcessor.process();
	}

	/**
	 * Creates the TF vector component for the given words.
	 *
	 * @param words the words of the document to build the TF
	 *              vector from
	 * @return the TF vector representation of the given document
	 */
	protected Vector createTFVector(List<String> words) {
		double[] values = new double[datasetInfo.vocabulary.size()];
		for (String word : words) {
			Integer wordIndex = datasetInfo.vocabulary.get(word);
			if (wordIndex == null) continue;
			values[wordIndex]++;
		}
		return new Vector(values);
	}

	/**
	 * Creates the "main" IDF vector which represents the words'
	 * frequencies in all of the documents.
	 */
	protected void createIDFVector() {
		double[] values = new double[datasetInfo.vocabulary.size()];

		for (String word : datasetInfo.vocabulary.keySet()) {
			Integer freq = datasetInfo.wordFrequency.get(word);
			if (freq == null) continue;
			values[datasetInfo.vocabulary.get(word)] = Math.log(datasetInfo.documents.size() / (double) freq);
		}
		datasetInfo.idf = new Vector(values);

		for (Document d : datasetInfo.documents.values()) {
			d.setVector(Vector.multiply(d.getTFVector(), datasetInfo.idf));
		}
	}

	/**
	 * Calculates the similarity coefficients between all documents.
	 */
	private void calculateSimilarities() {
		Map<DatasetInfo.DocumentPair, Double> similiarities = new HashMap<>();
		List<Document> documents = new ArrayList<>(datasetInfo.documents.values());
		for (int i = 0; i < documents.size(); i++) {
			for (int j = 0; j < documents.size(); j++) {
				if (i >= j) continue;

				Document d1 = documents.get(i);
				Document d2 = documents.get(j);

				double sim = d1.sim(d2) / d1.sim(d1);
				similiarities.put(new DatasetInfo.DocumentPair(d1, d2), sim);
			}
		}
		datasetInfo.similarities = similiarities;
	}

	/**
	 * Retrieve the function ranking function.
	 *
	 * @return function ranking function
	 */
	public static RankingFunction getCurrent() {
		return function;
	}

	/**
	 * Holds all the relevant information about the dataset.
	 *
	 * @author Luka Cupic
	 */
	public static class DatasetInfo implements Serializable {

		private static final long serialVersionUID = 1L;

		/**
		 * The collection of all words from all the documents (aka. dataset).
		 * Each word maps to it's position (i.e. index) in the vocabulary.
		 */
		public Map<String, Integer> vocabulary = new HashMap<>();

		/**
		 * Holds the number of occurrences in the documents for each word from
		 * the vocabulary.
		 */
		public Map<String, Integer> wordFrequency = new LinkedHashMap<>();

		/**
		 * A map of all the documents, mapped to by their appropriate file system
		 * paths.
		 */
		public Map<String, Document> documents = new LinkedHashMap<>();

		/**
		 * A helper IDF vector which holds the IDF components for each of the
		 * words from the vocabulary.
		 */
		public Vector idf;

		public Map<DocumentPair, Double> similarities = new HashMap<>();

		public static class DocumentPair implements Serializable {

			private static final long serialVersionUID = 1L;

			Document doc1;

			Document doc2;

			public DocumentPair() {
			}

			public DocumentPair(Document doc1, Document doc2) {
				this.doc1 = doc1;
				this.doc2 = doc2;
			}

			public void setDocuments(Document doc1, Document doc2) {
				this.doc1 = doc1;
				this.doc2 = doc2;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				DocumentPair that = (DocumentPair) o;
				return Objects.equals(doc1, that.doc1) && Objects.equals(doc2, that.doc2);
			}

			@Override
			public int hashCode() {
				return Objects.hash(doc1, doc2);
			}
		}
	}
}
