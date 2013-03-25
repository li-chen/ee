package info.chenli.ee.util;

import java.util.concurrent.TimeUnit;

public class Timer {

	private boolean isRunning = false;
	private long start;
	private long end;

	/**
	 * Start the timer.
	 */
	public void start() {

		this.isRunning = true;
		start = System.currentTimeMillis();
	}

	/**
	 * stop the timer.
	 */
	public void stop() {

		this.isRunning = false;
		end = System.currentTimeMillis();
	}

	/**
	 * The passed time, it basically is the time calling {@link Timer#start()}
	 * subtract the time calling {@link Timer#stop()}.
	 * 
	 * @return The time in milliseconds.
	 */
	public long getRunningTimeMillis() {

		if (isRunning) {
			stop();
		}

		return end - start;
	}

	/**
	 * The passed time, it basically is the time calling {@link Timer#start()}
	 * subtract the time calling {@link Timer#stop()}.
	 * 
	 * @return The time in a user friendly format.
	 */
	public String getRunningTime() {

		if (isRunning) {
			stop();
		}

		long millis = end - start;
		String time = String.format(
				"%d min, %d sec",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(millis)));
		return time;
	}
}