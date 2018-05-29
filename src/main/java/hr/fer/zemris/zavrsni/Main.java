package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.readers.ConsoleReader;
import hr.fer.zemris.zavrsni.utils.IOUtils;
import hr.fer.zemris.zavrsni.utils.MD5Visitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * This class represent a CLI document search engine. It uses
 * the TF-IDF vector representation of documents, which are then
 * used for matching the user's input and finding the best results.
 * The program expects a single argument: the path to the folder
 * containing the documents which will be searched (and by which the
 * vocabulary will be created).
 *
 * @author Luka Cupic
 */
public class Main {

	/**
	 * The path to the file containing the stop words.
	 */
	public static final String STOP_WORDS_PATH = "src/main/resources/stop_words.txt";

	public static final String CACHE_FOLDER = "src/main/resources/cache/";

	/**
	 * The path to the serialized dataset file.
	 */
	public static String datasetInfoPrefix = "dinfo";
	public static String DATASET_INFO_FILENAME;

	/**
	 * The path to the dataset directory MD5 hash file.
	 */
	private static String md5Prefix = "md5";
	public static String MD5_FILENAME;

	/**
	 * The function to perform the ranking of the documents.
	 */
	private static RankingFunction function;

	/**
	 * The main method.
	 *
	 * @param args a single argument - representing the path to the folder of
	 *             with documents
	 */
	public static void main(String[] args) {
		System.out.println("Initializing, please wait...");
		try {
			function = init(Paths.get(args[0]));
		} catch (IOException ex) {
			System.out.println("Initialization error: " + ex);
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
	 * Checks if the dataset serialization file exists and whether the
	 * MD5 hash file matches the current hash.
	 *
	 * @param dataset the path to the dataset
	 * @return true iff the serialized file exists and the MD5 hashes
	 * match
	 * @throws IOException if an error occurs while reading the files
	 */
	private static boolean isDatasetCorrect(Path dataset) throws IOException {
		String md5 = new MD5Visitor(dataset).getMd5();
		String md5Real;

		if (new File(MD5_FILENAME).exists()) {
			md5Real = IOUtils.readFromTextFile(MD5_FILENAME);
		} else {
			IOUtils.writeToTextFile(MD5_FILENAME, md5);
			return false;
		}

		if (new File(DATASET_INFO_FILENAME).exists()) {
			if (md5.equals(md5Real)) {
				return true;
			} else {
				IOUtils.writeToTextFile(MD5_FILENAME, md5);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Initializes the dataset information.
	 * The method recovers the dataset info from the serialized file,
	 * if any, or recreates the dataset info from scratch and stores
	 * it in the serialized file for later use.
	 *
	 * @param dataset the path to the dataset
	 * @return the ranking function responsible for comparing the documents
	 * @throws IOException if an error occurs while initializing the dataset
	 */
	public static RankingFunction init(Path dataset) throws IOException {
		Path filename = Paths.get(dataset.toFile().getName());

		DATASET_INFO_FILENAME = datasetInfoPrefix + "_" + filename + ".ser";
		DATASET_INFO_FILENAME = Paths.get(CACHE_FOLDER).resolve(DATASET_INFO_FILENAME).toString();

		MD5_FILENAME = md5Prefix + "_" + filename + ".hash";
		MD5_FILENAME = Paths.get(CACHE_FOLDER).resolve(MD5_FILENAME).toString();

		RankingFunction function;

		if (isDatasetCorrect(dataset)) {
			function = new CosineSimilarity();
			RankingFunction.datasetInfo = IOUtils.deserialize(DATASET_INFO_FILENAME);
		} else {
			System.out.println("You changed the dataset!");
			function = new CosineSimilarity(dataset);
		}
		return function;
	}

	/**
	 * Parses the given input and performs an appropriate command or
	 * throws an exception if an illegal (or unknown) command is given.
	 *
	 * @param input the user's input.
	 */
	private static void parseInput(String input) throws IOException {
		if (input.equals("exit") || input.equals("quit")) {
			System.exit(0);
		} else {
			processQuery(input);
		}
	}

	/**
	 * Processes the user's query input, read from the given scanner object, and
	 * displays the results (onto the standard output).
	 */
	private static void processQuery(String input) throws IOException {
		System.out.println("Here are the search results:");
		InputProcessor.setReader(new ConsoleReader(input));
		//InputProcessor.setReader(new TextReader(Paths.get("src/main/resources/pg4065.txt")));
		printResults(function.process(InputProcessor.process()));
	}

	/**
	 * Prints the given results onto the standard output.
	 */
	private static void printResults(List<Result> results) {
		for (int i = 0; i < results.size(); i++) {
			Result r = results.get(i);
			System.out.printf("[%d] (%f) %s\n", i, r.getSim(), r.getDocument().getPath());
		}
	}
}
