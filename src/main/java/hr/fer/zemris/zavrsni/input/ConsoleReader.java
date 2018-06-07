package hr.fer.zemris.zavrsni.input;

import hr.fer.zemris.zavrsni.utils.TextUtils;

import java.util.List;

public class ConsoleReader implements DocumentReader {

	private String text;

	/**
	 * Creates a new TextReader.
	 */
	public ConsoleReader(String text) {
		this.text = text;
	}

	@Override
	public List<String> readDocument() {
		return TextUtils.getWordsFromText(text);
	}
}