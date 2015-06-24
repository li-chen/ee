package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.UimaUtil;

import java.io.*;
import java.util.List;

public class Classify {

	public static void main(String[] args) {
		File trainFile = new File("./model/instances.trigger.txt");
		File develFile = new File("./model/instances.trigger.dev.txt");
		List<Instance> instances;
		Instance instance;
		try {
			InputStreamReader trainFileStream = new InputStreamReader(
					new FileInputStream(trainFile), "UTF8");
			BufferedReader trainFileBuffer = new BufferedReader(trainFileStream);
			
			InputStreamReader develFileStream = new InputStreamReader(
					new FileInputStream(develFile), "UTF8");
			BufferedReader develFileBuffer = new BufferedReader(develFileStream);
			
			String trainFileCh;
			while ((trainFileCh = trainFileBuffer.readLine()) != null) {
				String[] trainInstance = trainFileCh.split("\t");
				//instance.setLabel(Integer.parseInt(trainInstance[0]));
				//instance.getFeaturesNumeric();
				//instances.add(instance);
				
			}
			trainFileBuffer.close();
			trainFileStream.close();
			develFileStream.close();
			develFileBuffer.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
