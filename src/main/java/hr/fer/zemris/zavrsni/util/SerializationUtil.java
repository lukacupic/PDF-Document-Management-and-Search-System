package hr.fer.zemris.zavrsni.util;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Luka Cupic
 */
public class SerializationUtil {

	public static void serialize(Serializable object, String path) {
		try (FileOutputStream fos = new FileOutputStream(path);
		     ObjectOutputStream out = new ObjectOutputStream(fos)) {
			out.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@SuppressWarnings("unchecked")
//	public static <T> T deserialize(String path, Class<T> type) {
//		T object = null;
//		try {
//			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
//			object = type.cast(in.readObject());
//			in.close();
//		} catch (Exception e) {
//		}
//		return object;
//	}

//	@SuppressWarnings("unchecked")
//	public static <T> T deserialize(String path) {
//		T object = null;
//		try {
//			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
//			object = (T) in.readObject();
//			in.close();
//		} catch (Exception e) {
//		}
//		return object;
//	}

	@SuppressWarnings("unchecked")
	public static Object deserialize(String path) {
		Object object = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			object = in.readObject();
			in.close();
		} catch (Exception e) {
		}
		return object;
	}

	public static void serialize2(java.lang.Object object, String path) {
		Gson gson = new Gson();
		String str = gson.toJson(object);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
			writer.write(str);
		} catch (Exception ex) {
		}
	}
}
