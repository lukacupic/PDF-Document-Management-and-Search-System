package hr.fer.zemris.zavrsni.input;

import hr.fer.zemris.zavrsni.utils.IOUtils;
import hr.fer.zemris.zavrsni.utils.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The InputProcessor class processes the documents read by
 * the aggregated DocumentReader; it removes stop words and
 * performs stemming analysis of the read document.
 *
 * @author Luka Cupic
 */
public class InputProcessor {

	/**
	 * A list of stop words.
	 */
	private static List<String> stopWords = new ArrayList<>();

	/**
	 * The word stemmer.
	 */
	private static Stemmer stemmer = new Stemmer();

	/**
	 * The document reader.
	 */
	private static DocumentReader reader;

	/**
	 * Creates a new InputProcessor.
	 *
	 * @param stopWordsPath path to the file containing stop words
	 */
	public static void setStopWords(String stopWordsPath) {
		String text = IOUtils.readFromInputStream(IOUtils.getResource("stop_words.txt"));
		stopWords.addAll(TextUtils.getWordsFromText(text));
	}

	/**
	 * Reads the document through the {@link #reader} object, removes
	 * the stop words, stems the remaining words and returns the list
	 * of processed words.
	 *
	 * @return the list of processed words
	 * @throws IOException if an I/O error occurs
	 */
	public static List<String> process() throws IOException {
		List<String> words = reader.readDocument();
		words = words.stream().filter(s -> !stopWords.contains(s)).collect(Collectors.toList());
		words = words.stream().map(stemmer::stripAffixes).collect(Collectors.toList());
		return words;
	}

	/**
	 * Sets the document current reader of this InputProcessor.
	 *
	 * @param reader the reader
	 */
	public static void setReader(DocumentReader reader) {
		InputProcessor.reader = reader;
	}
}
