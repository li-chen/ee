package info.chenli.ee.searn;

import info.chenli.classifier.Instance;

import java.util.Map;

/**
 * 
 * @author Chen Li
 *
 */
public class State {

	private Map<Instance, String> predictions = null;

	public Map<Instance, String> getPredictions() {
		return predictions;
	}

	public void setPredictions(Map<Instance, String> predictions) {
		this.predictions = predictions;
	}

}
