package hr.fer.zemris.zavrsni.readers;

import java.io.IOException;
import java.util.List;

public interface DocumentReader {

	/**
	 * Reads a document specified by the given path and returns a list of words extracted
	 * from  the document. All non-letter characters will be ignored in the end result.
	 * For example, the phrase "hitch42iker" will be interpreted as "hitch iker".
	 *
	 * @return a list of words representing the contents of the document
	 * @throws IOException if an error occurs while reading the document
	 */
	List<String> readDocument() throws IOException;
}
