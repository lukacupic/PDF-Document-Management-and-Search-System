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
 * A recursive directory visitor responsible for calculating the
 * MD5 hash of an arbitrary directory.
 * <p>
 * The visitor automatically starts traversing the given directory
 * upon instantiation; there is no need (nor way) to perform this
 * manually.
 *
 * @author Luka Cupic
 */
public class MD5Visitor extends SimpleFileVisitor<Path> {

	/**
	 * The MD5 hash value of the whole directory.
	 */
	private StringBuilder md5 = new StringBuilder();

	/**
	 * Creates a new {@link MD5Visitor}.
	 *
	 * @param directory the directory to visit
	 * @throws IOException if an error occurs while performing
	 *                     the traversal of the directory
	 */
	public MD5Visitor(Path directory) throws IOException {
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

	/**
	 * Returns the calculated MD5 hash value.
	 *
	 * @return the MD5 hash
	 */
	public String getMd5() {
		return md5.toString();
	}
}