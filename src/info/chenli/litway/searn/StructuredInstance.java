package info.chenli.litway.searn;

import info.chenli.classifier.Instance;

import java.util.List;

public class StructuredInstance {

	// each node's id and the node's attributes, e.g. a token of a sentence and
	// the token's pos, lemma.
	private List<Instance> nodes;

	public List<Instance> getNodes() {
		return nodes;
	}

	public void setNodes(List<Instance> nodes) {
		this.nodes = nodes;
	}

}
