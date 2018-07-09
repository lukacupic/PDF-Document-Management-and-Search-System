package hr.fer.zemris.zavrsni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author Luka Cupic
 */
public class IOUtils {

	/**
	 * Gets the user home directory.
	 *
	 * @return the user home directory
	 */
	public static String getUserHomeDir() {
		String s = File.separator;
		return System.getProperty("user.home") + s + ".zavrsni" + s;
	}

	/**
	 * Creates
	 */
	public static void createUserHomeDir() {
		File dir = new File(IOUtils.getUserHomeDir());
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * Serializes the given object and stores it on the given path.
	 *
	 * @param object the object to serialize
	 * @param path   the path
	 */
	public static <T> void serialize(T object, String path) {
		try (FileOutputStream fos = new FileOutputStream(path);
		     ObjectOutputStream out = new ObjectOutputStream(fos)) {
			out.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deserializes and returns the object at the given path.
	 *
	 * @param path the path the object is located at
	 * @return the serialized object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String path) {
		T object = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			object = (T) in.readObject(); // if something fails, this could be the place!
			in.close();
		} catch (Exception e) {
		}
		return object;
	}

	public static String readFromTextFile(String path) throws IOException {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}

	public static void writeToTextFile(String path, String data) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.write(data);
		pw.flush();
		pw.close();
	}

	public static InputStream getResource(String name) {
		return IOUtils.class.getClassLoader().getResourceAsStream(name);
	}

	public static String readFromInputStream(InputStream is) {
		try (Scanner s = new Scanner(is)) {
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}
}
