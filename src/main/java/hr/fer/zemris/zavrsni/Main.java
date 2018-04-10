package hr.fer.zemris.zavrsni;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This class represent a CMD document search engine. It uses
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
	 * The path to the collection of documents.
	 */
	private static final String DATASET_PATH = "src/main/resources/dataset_simple";

	/**
	 * The path to the file containing the stop words.
	 */
	private static final String STOP_WORDS_PATH = "src/main/resources/stop_words.txt";

	/**
	 * The collection of all words from all the documents (aka. dataset).
	 */
	private static Map<String, Integer> vocabulary = new HashMap<>();

	/**
	 * A set of stop words. A "stop word" is defined as a word irrelevant to
	 * the searching algorithm.
	 */
	private static Set<String> stopWords = new HashSet<>();

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

	/**
	 * The main method.
	 *
	 * @param args a single argument - representing the path to the folder of
	 *             with documents
	 */
	public static void main(String[] args) {
		Path path = Paths.get(DATASET_PATH);

		try {
			System.out.println("Initializing, please wait...");
			long t1 = System.currentTimeMillis();
			init(path);
			long t2 = System.currentTimeMillis();
			System.out.printf("Dataset loaded in %d seconds.\n\n", (t2 - t1) / 1000);
		} catch (IOException e) {
			System.out.println("Initialization error!");
			System.exit(0);
		}

		Scanner sc = new Scanner(System.in);

		System.out.print("Query: ");
		while (sc.hasNextLine()) {
			String line = sc.nextLine();

			try {
				parseInput(line);
			} catch (Exception ex) {
				System.out.println("Sorry, but nothing was found!");
			}
			System.out.printf("%nQuery: ");
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
		readStopWords();
		createVocabulary(path);
		initDocuments(path);
	}

	/**
	 * Reads the file containing the stop words and creates a set
	 * holding them.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	private static void readStopWords() throws IOException {
		Path path = Paths.get(STOP_WORDS_PATH);
		stopWords.addAll(Files.readAllLines(path));
	}

	/**
	 * Creates the vocabulary by recursively reading all documents at the
	 * given path. Words specified in the set {@code {@link #stopWords}} are
	 * ignored, as they are not important for the further steps in the algorithm.
	 * All character that are not words will simply be ignored. So for example, a
	 * part of the document "hitch42iker" shall be interpreted as "hitch iker".
	 *
	 * @param path the path to the folder containing the documents
	 * @throws IOException if an error occurs while creating the vocabulary
	 */
	private static void createVocabulary(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				if (!path.toString().endsWith(".pdf") && !path.toString().endsWith(".PDF")) {
					System.out.println("File format not recognized. Skipping file " + path);
					return FileVisitResult.CONTINUE;
				}

				for (String word : readPDFDocument(path)) {
					if (word.isEmpty() || vocabulary.containsKey(word) || stopWords.contains(word)) continue;
					vocabulary.put(word, -1);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		// Iterate the vocabulary and store the index of each word
		// as the word's value in the 'Vocabulary' map
		List<String> words = new ArrayList<>(vocabulary.keySet());
		vocabulary.forEach((key, value) -> vocabulary.put(key, words.indexOf(key)));
	}

	/**
	 * Reads the document specified by the given path and returns a list of words contained
	 * in the document. All non-letter characters will be ignored in the end result. For
	 * example, a part of the document "hitch42iker" shall be interpreted as "hitch iker".
	 *
	 * @param path the path to the document
	 * @return a list of words representing the contents of the document
	 * @throws IOException if an error occurs while reading the document
	 */
	private static List<String> readDocument(Path path) throws IOException {
		return getWordsFromText(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
	}

	/**
	 * Reads the PDF document specified by the given path and returns a list of words extracted
	 * from  the document. All non-letter characters will be ignored in the end result. For
	 * example, a part of the document "hitch42iker" will be interpreted as "hitch iker".
	 *
	 * @param path the path to the document
	 * @return a list of words representing the contents of the document
	 * @throws IOException if an error occurs while reading the document
	 */
	private static List<String> readPDFDocument(Path path) throws IOException {
		try (PDDocument doc = PDDocument.load(path.toFile())) {
			String text = new PDFTextStripper().getText(doc);
			return getWordsFromText(text);
		}

//		AutoDetectParser parser = new AutoDetectParser();
//		BodyContentHandler handler = new BodyContentHandler();
//		Metadata metadata = new Metadata();
//		try {
//			parser.parse(Files.newInputStream(path), handler, metadata);
//		} catch (SAXException | TikaException e) {
//			e.printStackTrace();
//		}
//		return getWordsFromText(handler.toString());
	}

	/**
	 * Extracts the words from the given string and returns them as a list.
	 *
	 * @param text the text to extract the words from
	 * @return an {@link ArrayList} collection of extracted  words
	 */
	private static List<String> getWordsFromText(String text) {
		text = text.replaceAll("[^A-Za-z[\t][\n][\r]]+", " ").trim().toLowerCase();
		return new ArrayList<>(Arrays.asList(text.split(" ")));
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
				if (!path.toString().endsWith(".pdf") && !path.toString().endsWith(".PDF")) {
					System.out.println("File format not recognized. Skipping file " + path);
					return FileVisitResult.CONTINUE;
				}

				List<String> words = readPDFDocument(path);

				// create the TF vector component
				double[] values = new double[vocabulary.size()];
				for (String word : words) {
					if (stopWords.contains(word)) continue;

					int wordIndex = vocabulary.get(word);
					values[wordIndex]++;
				}

				// update the map, mapping each word to the number of documents containing it
				for (String word : words) {
					wordFrequency.put(word, wordFrequency.containsKey(word) ? wordFrequency.get(word) + 1 : 1);
				}

				Document doc = new Document(path, new Vector(values), null);
				documents.put(path, doc);

				return FileVisitResult.CONTINUE;
			}
		});
		createIDFVector();
	}

	/**
	 * Creates the "main" IDF vector which represents the number of occurrences in
	 * the documents for each of the words from the vocabulary.
	 */
	private static void createIDFVector() {
		double[] values = new double[vocabulary.size()];

		for (String word : vocabulary.keySet()) {
			int wordCount = wordFrequency.get(word);
			values[vocabulary.get(word)] = (double) vocabulary.size() / wordCount;
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
		List<String> words = getWordsFromText(input);
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
			results.add(new Result(doc.similarTo(d), d));
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
