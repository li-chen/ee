package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.Combinations;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class BindingInstances extends AbstractInstances {

	public BindingInstances() {

		super(new int[] { Event.type });
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<String> getLabelsString() {

		return null;
	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> annoIter) {

		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		AnnotationIndex<Annotation> sentenceIndex = jcas
				.getAnnotationIndex(Sentence.type);

		FSIterator<Annotation> sentenceIter = sentenceIndex.iterator();
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> bindingEventCandidates = new LinkedList<Instance>();
			si.setNodes(bindingEventCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			for (Event event : events) {

				if (event.getTrigger().getEventType()
						.equals(String.valueOf(EventType.Binding))) {

					Combinations<Protein> combs = new Combinations<Protein>(
							proteins);
					for (List<Protein> themes : combs.getCombinations()) {
						bindingEventCandidates.add(bindingEventToInstance(jcas,
								sentence, event, themes, dependencyExtractor));
					}
				}
			}

			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		BindingInstances ti = new BindingInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(new File(args[0]));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		String classifierName = "liblinear";

		ti.saveInstances(new File("./model/instances.binding.txt"));
		ti.saveSvmLightInstances(new File(
				"./model/instances.binding.svm.no_dum.txt"));

		if (args.length == 2 && args[1].equals("dev")) {
			dict.saveDictionary(new File("./model/binding.".concat(
					classifierName).concat(".dict")));

			BindingInstances testInstances = new BindingInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			instances = testInstances.getInstances(new File(
					"./data/development/"));

			instances = dict.instancesToNumeric(instances);

			ti.saveInstances(new File("./model/instances.binding.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.binding.svm.dev.no_dum.txt"));
		}
	}
}
