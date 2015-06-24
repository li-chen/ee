package info.chenli.litway.bionlp13.ge;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class ReadWord2vec {


	public static Map<String,double[]>  word2vec(File word2vecFile) {
		Map<String,double[]> word2vec = new HashMap<String,double[]>();
		//File word2vecFile = new File("./model/proteinKept_skipG.txt");
		//File word2vecFile = new File("./model/ge13train.SkipGram");
		try {
			InputStreamReader word2vecFileStream = new InputStreamReader(
					new FileInputStream(word2vecFile), "UTF8");
			BufferedReader word2vecFileBuffer = new BufferedReader(word2vecFileStream);
			String word2vecTextCh;
			//List<String> word = new ArrayList<String>();
			//List<String[]> vec = new ArrayList<String[]>();
			word2vecFileBuffer.readLine();
			while ((word2vecTextCh = word2vecFileBuffer.readLine()) != null) {
				String[] wordSb = word2vecTextCh.split(" ");
				String[] wordSb2 = new String[wordSb.length-1];
				double[] wordSb3 = new double[wordSb.length-1];
				System.arraycopy(wordSb, 1, wordSb2, 0, wordSb.length-1);
				
				for (int i=0; i<wordSb2.length; i++) {
					wordSb3[i] = Double.parseDouble(wordSb2[i]);
				}
				//word.add(wordSb[0]);
				//vec.add(wordSb2);
				word2vec.put(wordSb[0], wordSb3);
			}
			word2vecFileBuffer.close();
			
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return word2vec;
	}
}
