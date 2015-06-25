package info.chenli.litway.config;

public interface Argument {

	/**
	 * Whether the argument is an event. It is impossible that an argument is
	 * neither an entity nor an event.
	 * 
	 * @return If <em>True</em>, it is an event instead of an entity. If
	 *         <em>False</em>, it is an entity instead of an event.
	 */
	public boolean isEvent();

}
