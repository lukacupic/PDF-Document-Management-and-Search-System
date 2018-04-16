package hr.fer.zemris.zavrsni;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtil {

	/**
	 * Extracts the words from the given string and returns them as a list.
	 *
	 * @param text the text to extract the words from
	 * @return an {@link ArrayList} collection of extracted  words
	 */
	public static List<String> getWordsFromText(String text) {
		text = text.replaceAll("[^A-Za-z[\t][\n][\r]]+", " ").trim().toLowerCase();
		return new ArrayList<>(Arrays.asList(text.split(" ")));
	}

	/**
	 * Extracts and returns the extension of the file at the specified path.
	 * Note: the extension will automatically be converted to lowercase.
	 *
	 * @param path the path
	 * @return the extension of the file at the given path
	 */
	public static String getFileExtension(Path path) {
		String name = path.toFile().getName();
		try {
			return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}
}
