package hr.fer.zemris.zavrsni;

import java.nio.file.Path;

/**
 * This class represents a single document from the collection.
 * The exact contents of the document are not stored in memory,
 * as they are represented by the TF-IDF vector. Each document
 * is represented by it's absolute file system path.
 *
 * @author Luka Čupić
 */
public class Document {

	/**
	 * Represents the file system path to the document.
	 */
	private Path path;

	/**
	 * Represents the TF-IDF vector for the document.
	 */
	private Vector vector;

	/**
	 * Represents the TF vector component for the document. This
	 * vector is used as a helper-vector while creating the full
	 * {@link #vector} object.
	 */
	private Vector tfVector;

	/**
	 * Creates a new document object.
	 *
	 * @param path  the path to the represented document
	 * @param tf    the TF vector component
	 * @param tfidf the full TF-IDF vector representing the document
	 */
	public Document(Path path, Vector tf, Vector tfidf) {
		this.path = path != null ? path.toAbsolutePath() : null;
		this.tfVector = tf;
		this.vector = tfidf;
	}

	/**
	 * Returns the similarity coefficient between this and the specified
	 * document. In reality, the similarity coefficient is nothing other
	 * than the scalar product of this and the specified vector. The
	 * coefficient is a value on the interval [0, 1] where 0 means that
	 * the documents share no similarity whereas 1 means that the documents
	 * are identical.
	 *
	 * @param other the document to compare to
	 * @return the similarity between this and the provided document
	 */
	public double sim(Document other) {
		return this.getVector().cos(other.getVector());
	}

	/**
	 * Gets the path representing this document.
	 *
	 * @return the path to the document
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Sets the path for this document.
	 *
	 * @param path the path to the document
	 */
	public void setPath(Path path) {
		this.path = path.toAbsolutePath();
	}

	/**
	 * Gets the TF-IDF vector representing this document.
	 *
	 * @return the TF-IDF vector
	 */
	public Vector getVector() {
		return vector;
	}

	/**
	 * Sets the TF-IDF vector representing this document.
	 *
	 * @param vector the TF-IDF vector
	 */
	public void setVector(Vector vector) {
		this.vector = vector;
	}

	/**
	 * Gets the TF vector component of this document.
	 *
	 * @return the TF vector component
	 */
	public Vector getTFVector() {
		return tfVector;
	}

	/**
	 * Sets the TF vector component of this document.
	 *
	 * @param tfVector the TF vector component
	 */
	public void setTFVector(Vector tfVector) {
		this.tfVector = tfVector;
	}
}
