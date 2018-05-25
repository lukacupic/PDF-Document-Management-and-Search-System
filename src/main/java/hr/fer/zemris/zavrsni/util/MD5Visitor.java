package hr.fer.zemris.zavrsni.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Luka Cupic
 */
public class MD5Visitor extends SimpleFileVisitor<Path> {

	/**
	 * The MD5 hash value of the whole directory.
	 */
	private StringBuilder md5 = new StringBuilder();

	/**
	 * The path of the directory to visit.
	 */
	private Path directory;

	public MD5Visitor(Path directory) throws IOException {
		this.directory = directory;
		Files.walkFileTree(directory, this);
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
		try {
			FileInputStream fis = new FileInputStream(path.toFile());
			md5.append(DigestUtils.md5Hex(fis));
			fis.close();
		} catch (Exception e) {
		}
		return FileVisitResult.CONTINUE;
	}

	public String getMd5() {
		return md5.toString();
	}
}