package hr.fer.zemris.zavrsni;

import hr.fer.zemris.zavrsni.ranking.CosineSimilarity;
import hr.fer.zemris.zavrsni.ranking.RankingFunction;
import hr.fer.zemris.zavrsni.utils.IOUtils;
import hr.fer.zemris.zavrsni.utils.MD5Visitor;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represent a CLI document search engine. It uses
 * the TF-IDF vector representation of documents, which are then
 * used for matching the user's input and finding the best results.
 * The program expects a single argument: the path to the folder
 * containing the documents which will be searched (and by which the
 * vocabulary will be created).
 *
 * @author Luka Cupic
 */
public class Initializer {

	/**
	 * The path to the file containing the stop words.
	 */
	public static final String STOP_WORDS_PATH = "src/main/resources/stop_words.txt";

	/**
	 * The user home directory.
	 */
	private static final String HOME_DIR = IOUtils.getUserHomeDir();

	/**
	 * The path to the serialized dataset file.
	 */
	private static final String DATASET_INFO_PREFIX = "info";

	/**
	 * The path to the dataset directory MD5 hash file.
	 */
	private static final String MD5_PREFIX = "md5";

	public static String datasetInfoFilename;

	public static String md5Filename;

	/**
	 * The function to perform the ranking of the documents.
	 */
	private static RankingFunction function;

	/**
	 * Initializes the dataset information.
	 * The method recovers the dataset info from the serialized file,
	 * if any, or recreates the dataset info from scratch and stores
	 * it in the serialized file for later use.
	 *
	 * @param dataset the path to the dataset
	 * @return the ranking function responsible for comparing the documents
	 * @throws IOException if an error occurs while initializing the dataset
	 */
	public static RankingFunction init(Path dataset) throws IOException {
		IOUtils.createUserHomeDir(); // create the user home directory if it doesn't exist
		constructFilenames(dataset); // construct dataset info and md5 filenames

		RankingFunction function;
		if (isDatasetCorrect(dataset)) {
			function = new CosineSimilarity();
			RankingFunction.datasetInfo = IOUtils.deserialize(datasetInfoFilename);
		} else {
			System.out.println("You changed the dataset!");
			function = new CosineSimilarity(dataset);
		}
		return function;
	}

	/**
	 * Creates a unique name for each of the dataset info and md5 files.
	 *
	 * @param dataset the path to the dataset
	 */
	private static void constructFilenames(Path dataset) {
		String filename = DigestUtils.md5Hex(dataset.toString());

		datasetInfoFilename = DATASET_INFO_PREFIX + "_" + filename + ".ser";
		datasetInfoFilename = Paths.get(HOME_DIR).resolve(datasetInfoFilename).toString();

		md5Filename = MD5_PREFIX + "_" + filename + ".hash";
		md5Filename = Paths.get(HOME_DIR).resolve(md5Filename).toString();
	}

	/**
	 * Checks if the dataset serialization file exists and whether the
	 * MD5 hash file matches the current hash.
	 *
	 * @param dataset the path to the dataset
	 * @return true iff the serialized file exists and the MD5 hashes
	 * match
	 * @throws IOException if an error occurs while reading the files
	 */
	private static boolean isDatasetCorrect(Path dataset) throws IOException {
		String md5 = new MD5Visitor(dataset).getMd5();
		String md5Real;

		if (new File(md5Filename).exists()) {
			md5Real = IOUtils.readFromTextFile(md5Filename);
		} else {
			IOUtils.writeToTextFile(md5Filename, md5);
			return false;
		}

		if (new File(datasetInfoFilename).exists()) {
			if (md5.equals(md5Real)) {
				return true;
			} else {
				IOUtils.writeToTextFile(md5Filename, md5);
				return false;
			}
		} else {
			return false;
		}
	}
}
