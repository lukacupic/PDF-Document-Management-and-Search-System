package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.model.Document;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.model.Vector;
import hr.fer.zemris.zavrsni.readers.DocumentReader;
import hr.fer.zemris.zavrsni.readers.decorators.DocumentStemmer;
import hr.fer.zemris.zavrsni.readers.decorators.StopFilter;
import hr.fer.zemris.zavrsni.readers.TextReader;
import hr.fer.zemris.zavrsni.util.TextUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class represent a CLI document search engine. It uses
 * the TF-IDF vector representation of documents, which are then
 * used for matching the user's input and finding the best results.
 * The program expects a single argument: the path to the folder
 * containing the documents which will be searched (and by which the
 * vocabulary will be created).
 *
 * @author Luka Čupić
 */
public class Main {

	/**
	 * The path to the file containing the stop words.
	 */
	private static final String STOP_WORDS_PATH = "src/main/resources/stop_words.txt";

	/**
	 * The collection of all words from all the documents (aka. dataset).
	 */
	private static Map<String, Integer> vocabulary = new HashMap<>();

	/**
	 * Holds the number of occurrences in the documents for each word from
	 * the vocabulary.
	 */
	private static Map<String, Integer> wordFrequency = new LinkedHashMap<>();

	/**
	 * A map of all the documents, mapped to by their appropriate file system
	 * paths.
	 */
	private static Map<Path, Document> documents = new LinkedHashMap<>();

	/**
	 * A helper IDF vector which holds the IDF components for each of the
	 * words from the vocabulary.
	 */
	private static Vector idf;

	/**
	 * Holds a list of results which were created by the last "query" command.
	 */
	private static List<Result> results;

	private static DocumentReader reader;
	private static DocumentStemmer stemmer;
	private static StopFilter stopFilter;

	/**
	 * The main method.
	 *
	 * @param args a single argument - representing the path to the folder of
	 *             with documents
	 */
	public static void main(String[] args) {
		Path dataset = Paths.get(args[0]);

		try {
			System.out.println("Initializing, please wait...");
			long t1 = System.currentTimeMillis();
			init(dataset);
			long t2 = System.currentTimeMillis();
			System.out.printf("Dataset loaded in %d seconds.\n\n", (t2 - t1) / 1000);
		} catch (IOException e) {
			System.out.println("Initialization error! " + e);
			System.exit(0);
		}

		Scanner sc = new Scanner(System.in);

		System.out.print("Query: ");
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			try {
				parseInput(line);
			} catch (Exception ex) {
				System.out.println("Sorry, but nothing was found...");
			}
			System.out.print("\nQuery: ");
		}
	}

	/**
	 * Parses the given input and performs an appropriate command or
	 * throws an exception if an illegal (or unknown) command is given.
	 *
	 * @param input the user's input.
	 */
	private static void parseInput(String input) {
		if (input.equals("exit") || input.equals("quit")) {
			System.exit(0);
		} else {
			processQuery(input);
		}
	}

	/**
	 * Initializes the program.
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while initialization
	 */
	private static void init(Path path) throws IOException {
		// initialize document reading mechanism
		reader = new TextReader();
		stemmer = new DocumentStemmer(reader);
		stopFilter = new StopFilter(stemmer, STOP_WORDS_PATH);
		reader = stopFilter;

		// initialize the dataset
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
	private static void createVocabulary(Path path) throws IOException {
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
	private static void initDocuments(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				List<String> words = reader.readDocument(path);

				// create the TF vector component
				double[] values = new double[vocabulary.size()];
				for (String word : words) {
					int wordIndex = vocabulary.get(word);
					values[wordIndex]++;
				}

				// map each word to the number of documents containing the word
				words.stream().distinct().forEach(word ->
						wordFrequency.merge(word, 1, (a, b) -> a + b)
				);

				Document doc = new Document(path, new Vector(values), null);
				documents.put(path, doc);

				return FileVisitResult.CONTINUE;
			}
		});
		createIDFVector();
	}

	/**
	 * Creates the "main" IDF vector which represents the words'
	 * frequencies in all of the documents.
	 */
	private static void createIDFVector() {
		double[] values = new double[vocabulary.size()];

		for (String word : vocabulary.keySet()) {
			int freq = wordFrequency.get(word);
			values[vocabulary.get(word)] = Math.log(vocabulary.size() / (double) freq);
		}
		idf = new Vector(values);

		for (Document d : documents.values()) {
			d.setVector(Vector.multiply(d.getTFVector(), idf));
		}
	}

	/**
	 * Processes the user's query input, read from the given scanner object, and
	 * displays the results (onto the standard output).
	 */
	private static void processQuery(String input) {
		List<String> words = TextUtil.getWordsFromText(input);
		words.retainAll(vocabulary.keySet());

		double[] values = new double[vocabulary.size()];
		for (String word : words) {
			int wordIndex = vocabulary.get(word);
			values[wordIndex]++;
		}

		Document inputDoc = new Document(null, null, Vector.multiply(new Vector(values), idf));
		results = getResults(inputDoc);

		System.out.println("Here are the search results:");
		printResults();
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
	private static List<Result> getResults(Document doc) {
		List<Result> results = new ArrayList<>();
		for (Document d : documents.values()) {
			results.add(new Result(doc.sim(d), d));
		}
		results.sort(Comparator.reverseOrder());
		return results.subList(0, Math.min(9, results.size()));
	}

	/**
	 * Prints the currently stored results onto the standard output.
	 */
	private static void printResults() {
		for (int i = 0; i < results.size(); i++) {
			Result r = results.get(i);
			System.out.printf("[%d] (%f) %s\n", i, r.getSim(), r.getDocument().getPath());
		}
	}
}
