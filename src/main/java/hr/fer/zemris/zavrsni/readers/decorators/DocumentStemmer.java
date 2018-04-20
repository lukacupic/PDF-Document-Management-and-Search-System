package hr.fer.zemris.zavrsni.readers.decorators;

import hr.fer.zemris.zavrsni.readers.DocumentReader;
import hr.fer.zemris.zavrsni.util.Stemmer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentStemmer implements DocumentReader {

	private DocumentReader reader;
	private Stemmer stemmer;

	public DocumentStemmer(DocumentReader reader) {
		this.reader = reader;
		this.stemmer = new Stemmer();
	}

	@Override
	public List<String> readDocument(Path path) throws IOException {
		List<String> words = reader.readDocument(path);
		return words.stream().map(s -> stemmer.stem(s)).collect(Collectors.toList());
	}
}
