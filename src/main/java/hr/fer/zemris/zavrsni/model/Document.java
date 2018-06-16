package hr.fer.zemris.zavrsni.model;

import hr.fer.zemris.zavrsni.ranking.RankingFunction;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * This class represents a single document from the collection.
 * The exact contents of the document are not stored in memory,
 * as they are represented by the TF-IDF vector. Each document
 * is represented by it's absolute file system path.
 *
 * @author Luka Čupić
 */
public class Document implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Represents the file system path to the document.
	 */
	private String path;

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
	 * The length of the document (in words).
	 */
	private long length;

	private transient int cluster;

	public int getCluster() {
		return cluster;
	}

	public void setCluster(int cluster) {
		this.cluster = cluster;
	}

	/**
	 * Creates a new document object.
	 *
	 * @param path   the path to the represented document
	 * @param tf     the TF vector component
	 * @param tfidf  the full TF-IDF vector representing the document
	 * @param length the length of the document (in words)
	 */
	public Document(Path path, Vector tf, Vector tfidf, long length) {
		this.path = path != null ? path.toAbsolutePath().toString() : null;
		this.tfVector = tf;
		this.vector = tfidf;
		this.length = length;
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
		return RankingFunction.getCurrent().sim(this, other);
	}

	/**
	 * Gets the path representing this document.
	 *
	 * @return the path to the document
	 */
	public Path getPath() {
		return path != null ? Paths.get(path) : null;
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
	 * Gets the length (in words) of this document.
	 *
	 * @return the length of this document
	 */
	public long getLength() {
		return length;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Document document = (Document) o;
		return Objects.equals(path, document.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public String toString() {
		return Paths.get(path).getFileName().toString();
	}
}
