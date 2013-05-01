package info.chenli.litway.corpora;

import java.util.Comparator;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * Sort the annotations in a Java Collection.
 * 
 * @author Chen Li
 * 
 */
public class AnnotationSorter implements Comparator<Annotation> {

	@Override
	public int compare(Annotation anno1, Annotation anno2) {
		return anno1.getBegin() - anno2.getBegin();
	}

}
