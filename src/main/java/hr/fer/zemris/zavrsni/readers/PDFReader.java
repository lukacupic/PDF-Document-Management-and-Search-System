package hr.fer.zemris.zavrsni.readers;

import hr.fer.zemris.zavrsni.util.TextUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PDFReader implements DocumentReader {

	private Path path;

	/**
	 * Creates a new PDFReader.
	 */
	public PDFReader(Path path) {
		this.path = path;
	}

	@Override
	public List<String> readDocument() throws IOException {
		if (!TextUtil.getFileExtension(path).equals("pdf")) {
			throw new IOException("Unreadable extension!");
		}
		try (PDDocument doc = PDDocument.load(path.toFile())) {
			String text = new PDFTextStripper().getText(doc);
			return TextUtil.getWordsFromText(text);
		}
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
}
