package hr.fer.zemris.zavrsni.model;

/**
 * This class represents a result of the searching algorithm. It encapsulates
 * a {@code Document} object and the {@code Similarity coefficient} (formatted
 * to four decimal digits) which represents how much the document matches the
 * input provided by the user.
 *
 * @author Luka Čupić
 */
public class Result implements Comparable<Result> {

	/**
	 * The similarity coefficient between the {@link #document} and the
	 * user's input (interpreted as another document).
	 */
	private double sim;

	/**
	 * The document represented by this result object.
	 */
	private Document document;

	/**
	 * Creates a new result.
	 *
	 * @param sim      the similarity coefficient
	 * @param document the document
	 */
	public Result(double sim, Document document) {
		this.sim = sim;
		this.document = document;
	}

	/**
	 * Gets the similarity coefficient.
	 *
	 * @return the similarity coefficient
	 */
	public double getSim() {
		return sim;
	}


	/**
	 * Gets the document represented by this object.
	 *
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	@Override
	public int compareTo(Result o) {
		return Double.compare(sim, o.getSim());
	}
}