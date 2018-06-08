package hr.fer.zemris.zavrsni.input;

import hr.fer.zemris.zavrsni.utils.TextUtils;

import java.util.List;

public class QueryReader implements DocumentReader {

	private String text;

	/**
	 * Creates a new TextReader.
	 */
	public QueryReader(String text) {
		this.text = text;
	}

	@Override
	public List<String> readDocument() {
		return TextUtils.getWordsFromText(text);
	}
}