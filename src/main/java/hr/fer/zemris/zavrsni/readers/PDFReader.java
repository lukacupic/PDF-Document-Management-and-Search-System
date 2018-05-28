package hr.fer.zemris.zavrsni.readers;

import hr.fer.zemris.zavrsni.utils.TextUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PDFReader extends FileReader {

	/**
	 * The default constructor.
	 */
	public PDFReader() {
		super();
	}

	/**
	 * Creates a new PDFReader.
	 */
	public PDFReader(Path path) {
		super(path);
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
}
