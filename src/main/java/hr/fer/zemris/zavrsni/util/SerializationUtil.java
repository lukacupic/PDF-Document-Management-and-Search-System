package hr.fer.zemris.zavrsni.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Luka Cupic
 */
public class SerializationUtil {

	public static final String DATASET_INFO_PATH = "src/main/resources/info.ser";

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
}
