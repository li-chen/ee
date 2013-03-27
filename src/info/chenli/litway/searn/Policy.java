package info.chenli.litway.searn;

import info.chenli.litway.util.MathUtil;

import java.util.Vector;

public class Policy implements Comparable<Policy> {

	// parameters of the policy. e.g. weights etc.
	private Vector<Double> param = null;

	public Vector<Double> getParam() {
		return param;
	}

	public void setParam(Vector<Double> param) {
		this.param = param;
	}

	/**
	 * Compare two policies
	 * 
	 * @param another
	 *            policy.
	 * @return dot product of two sets of the policies' parameter
	 */
	@Override
	public int compareTo(Policy o) {

		return (int) Math.round(Math.abs(MathUtil.dot(o.getParam(),
				this.getParam())));
	}

	public int getPossibleActions() {
		// TODO Auto-generated method stub
		return 0;
	}

}
