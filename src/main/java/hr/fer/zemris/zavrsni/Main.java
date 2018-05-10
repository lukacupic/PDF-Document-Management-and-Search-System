package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.model.Result;
import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.OkapiBM25;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;

import java.io.IOException;
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

	private static RankingFunction function;

	/**
	 * The main method.
	 *
	 * @param args a single argument - representing the path to the folder of
	 *             with documents
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Initializing, please wait...");
			long t1 = System.currentTimeMillis();
			function = new CosineSimilarity(Paths.get(args[0]));
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
		printResults(function.process(input));
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
