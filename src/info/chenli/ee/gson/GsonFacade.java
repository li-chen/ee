package info.chenli.ee.gson;

import java.io.File;
import java.io.IOException;

import org.apache.uima.util.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GsonFacade {

	public static GsonFacade instance = new GsonFacade();
	private static Gson gson = new Gson();

	// prevent default constructor
	private GsonFacade() {
	}

	public GsonDocument getDocument(File f) {
		try {
			return gson.fromJson(FileUtils.file2String(f),
					GsonDocument.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(instance.getDocument(new File(args[0])));
	}
}
