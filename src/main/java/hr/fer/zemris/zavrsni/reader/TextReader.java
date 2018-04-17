package hr.fer.zemris.zavrsni.reader;

import hr.fer.zemris.zavrsni.util.TextUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextReader implements DocumentReader {

	/**
	 * Creates a new TextReader.
	 */
	public TextReader() {
	}

	@Override
	public List<String> readDocument(Path path) throws IOException {
		if (!TextUtil.getFileExtension(path).equals("txt")) {
			throw new IOException("Unreadable extension!");
		}
		String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return TextUtil.getWordsFromText(text);
	}
}
