package hr.fer.zemris.zavrsni.readers;

import hr.fer.zemris.zavrsni.util.TextUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextReader implements DocumentReader {

	private Path path;

	/**
	 * Creates a new TextReader.
	 */
	public TextReader(Path path) {
		this.path = path;
	}

	@Override
	public List<String> readDocument() throws IOException {
		if (!TextUtil.getFileExtension(path).equals("txt")) {
			throw new IOException("Unreadable extension!");
		}
		String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return TextUtil.getWordsFromText(text);
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
}
