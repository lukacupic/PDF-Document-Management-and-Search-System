package hr.fer.zemris.zavrsni.readers.decorators;

import hr.fer.zemris.zavrsni.readers.DocumentReader;
import hr.fer.zemris.zavrsni.util.Stemmer2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentStemmer implements DocumentReader {

	private DocumentReader reader;
	private Stemmer2 stemmer;

	public DocumentStemmer(DocumentReader reader) {
		this.reader = reader;
		this.stemmer = new Stemmer2();
	}

	@Override
	public List<String> readDocument(Path path) throws IOException {
		List<String> words = reader.readDocument(path);
		return words.stream().map(stemmer::stripAffixes).collect(Collectors.toList());
	}
}
