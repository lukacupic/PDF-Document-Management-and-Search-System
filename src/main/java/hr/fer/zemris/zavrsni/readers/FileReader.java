package hr.fer.zemris.zavrsni.readers;

import java.nio.file.Path;

/**
 * @author Luka Cupic
 */
public abstract class FileReader implements DocumentReader {

	protected Path path;

	public FileReader() {
	}

	public FileReader(Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
}
