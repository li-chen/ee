package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class Word2vec {

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = ti.getInstances(new File(args[0]));
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		int i = 0;
		String fileDir = instances.get(0).getFileId();
		String[] fileNames = instances.get(0).getFileId().split("/");
		String fileName = fileNames[fileNames.length - 1];
		File file = new File("./word2vec/GE13/".concat(fileName));
		
		for (Instance instance : instances) {
			
			if(instance.getSentenceId() == i){
				sb.append(instance.getId());
				sb.append(" ");
			}else {
				sb.append("\n");
				i = instance.getSentenceId();
				sb.append(instance.getId());
				sb.append(" ");
			}
			
			if(instance.getLabelString() != String.valueOf(EventType.Non_trigger)){
				sb2.append(instance.getId());
				sb2.append(" ");
				sb2.append(instance.getLabelString().toLowerCase());
				sb2.append("\n");				
			}	
			
			if(!fileDir.equals(instance.getFileId()) ) {				
				sb.append("\n");
				String instancesStr = sb2.toString();
				sb.append(instancesStr);
				instancesStr = sb.toString();
				FileUtil.saveFile(instancesStr, file);	
				fileDir=instance.getFileId();
				
				i = 0;
				sb.delete(0, sb.length()-1);
				sb2.delete(0, sb2.length()-1);
				fileNames = instance.getFileId().split("/");
				fileName = fileNames[fileNames.length - 1];
				file = new File("./word2vec/GE13/".concat(fileName));
				sb.append(instance.getId());
				sb.append(" ");
				}
		}	
		
		sb.append("\n");
		String instancesStr = sb2.toString();
		sb.append(instancesStr);
		instancesStr = sb.toString();
		FileUtil.saveFile(instancesStr, file);			
	}

}
