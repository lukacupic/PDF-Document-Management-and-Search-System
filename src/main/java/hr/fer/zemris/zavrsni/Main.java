package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.input.InputProcessor;
import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.input.ConsoleReader;
import hr.fer.zemris.zavrsni.utils.IOUtils;
import hr.fer.zemris.zavrsni.utils.MD5Visitor;
import org.apache.commons.codec.digest.DigestUtils;

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

	/**
	 * The user home directory.
	 */
	private static final String HOME_DIR = IOUtils.getUserHomeDir();

	/**
	 * The path to the serialized dataset file.
	 */
	private static final String DATASET_INFO_PREFIX = "info";

	/**
	 * The path to the dataset directory MD5 hash file.
	 */
	private static final String MD5_PREFIX = "md5";

	public static String datasetInfoFilename;

	public static String md5Filename;

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
			double t1 = System.currentTimeMillis();
			function = init(Paths.get(args[0]));
			double t2 = System.currentTimeMillis();
			System.out.println((t2 - t1) / 1000);
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
		IOUtils.createUserHomeDir(); // create the user home directory if it doesn't exist
		constructFilenames(dataset); // construct dataset info and md5 filenames

		RankingFunction function;
		if (isDatasetCorrect(dataset)) {
			function = new CosineSimilarity();
			RankingFunction.datasetInfo = IOUtils.deserialize(datasetInfoFilename);
		} else {
			System.out.println("You changed the dataset!");
			function = new CosineSimilarity(dataset);
		}
		return function;
	}

	/**
	 * Creates a unique name for each of the dataset info and md5 files.
	 *
	 * @param dataset the path to the dataset
	 */
	private static void constructFilenames(Path dataset) {
		String filename = DigestUtils.md5Hex(dataset.toString());

		datasetInfoFilename = DATASET_INFO_PREFIX + "_" + filename + ".ser";
		datasetInfoFilename = Paths.get(HOME_DIR).resolve(datasetInfoFilename).toString();

		md5Filename = MD5_PREFIX + "_" + filename + ".hash";
		md5Filename = Paths.get(HOME_DIR).resolve(md5Filename).toString();
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

		if (new File(md5Filename).exists()) {
			md5Real = IOUtils.readFromTextFile(md5Filename);
		} else {
			IOUtils.writeToTextFile(md5Filename, md5);
			return false;
		}

		if (new File(datasetInfoFilename).exists()) {
			if (md5.equals(md5Real)) {
				return true;
			} else {
				IOUtils.writeToTextFile(md5Filename, md5);
				return false;
			}
		} else {
			return false;
		}
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
