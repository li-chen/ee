package info.chenli.ee.util;

public class FileUtil {

	public static String removeFileNameExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
}
