package hr.fer.zemris.zavrsni.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Luka Cupic
 */
public class IOUtils {

	public static <T> void serialize(T object, String path) {
		try (FileOutputStream fos = new FileOutputStream(path);
		     ObjectOutputStream out = new ObjectOutputStream(fos)) {
			out.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
}
