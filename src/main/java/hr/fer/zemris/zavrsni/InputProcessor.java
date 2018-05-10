package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.readers.DocumentReader;
import hr.fer.zemris.zavrsni.util.Stemmer2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputProcessor {

	private List<String> stopWords = new ArrayList<>();
	private Stemmer2 stemmer = new Stemmer2();
	private DocumentReader reader;

	public InputProcessor(String stopWordsPath) throws IOException {
		stopWords.addAll(Files.readAllLines(Paths.get(stopWordsPath)));
	}

	public List<String> process() throws IOException {
		List<String> words = reader.readDocument();
		words = words.stream().map(stemmer::stripAffixes).collect(Collectors.toList());
		words = words.stream().filter(s -> !stopWords.contains(s)).collect(Collectors.toList());
		return words;
	}

	public void setReader(DocumentReader reader) {
		this.reader = reader;
	}
}
