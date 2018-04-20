package hr.fer.zemris.zavrsni.readers.decorators;

import hr.fer.zemris.zavrsni.readers.DocumentReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StopFilter implements DocumentReader {

	private DocumentReader reader;
	private Set<String> stopWords;

	public StopFilter(DocumentReader reader, String path) throws IOException {
		this.reader = reader;
		this.stopWords = new HashSet<>();
		stopWords.addAll(Files.readAllLines(Paths.get(path)));
	}

	@Override
	public List<String> readDocument(Path path) throws IOException {
		List<String> words = reader.readDocument(path);
		return words.stream().filter(s -> !stopWords.contains(s)).collect(Collectors.toList());
	}
}
