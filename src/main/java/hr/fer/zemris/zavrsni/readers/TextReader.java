package hr.fer.zemris.zavrsni.readers;

import hr.fer.zemris.zavrsni.utils.TextUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextReader extends FileReader {

	/**
	 * The default constructor.
	 */
	public TextReader() {
		super();
	}

	/**
	 * Creates a new {@link TextReader} object.
	 */
	public TextReader(Path path) {
		super(path);
	}

	@Override
	public List<String> readDocument() throws IOException {
		if (!TextUtil.getFileExtension(path).equals("txt")) {
			throw new IOException("Unreadable extension!");
		}
		String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return TextUtil.getWordsFromText(text);
	}
}
