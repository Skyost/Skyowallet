package fr.skyost.skyowallet.economy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a manager.
 *
 * @param <T> The kind of data to manager.
 */

public class SkyowalletManager<T extends EconomyObject> {

	/**
	 * The managed data.
	 */

	private final HashMap<String, T> data = new HashMap<>();

	/**
	 * Creates a new Skyowallet manager instance.
	 *
	 * @param objects Objects to add by default.
	 */

	@SafeVarargs
	public SkyowalletManager(final T... objects) {
		for(final T object : objects) {
			add(object);
		}
	}

	/**
	 * Adds an object to this manager.
	 *
	 * @param object The object.
	 *
	 * @return The added object.
	 */

	public T add(final T object) {
		data.put(object.getIdentifier(), object);
		return object;
	}

	/**
	 * Removes an object from this manager.
	 *
	 * @param object The object.
	 *
	 * @return The result of <em>HashMap.remove(...)</em> by default (unless overrode).
	 */

	public Object remove(final T object) {
		return remove(object == null ? null : object.getIdentifier());
	}

	/**
	 * Removes an object from this manager.
	 *
	 * @param identifier The object identifier.
	 *
	 * @return The result of <em>HashMap.remove(...)</em> by default (unless overrode).
	 */

	public Object remove(final String identifier) {
		if(identifier == null) {
			return null;
		}

		final T object = data.get(identifier);
		if(object == null) {
			return null;
		}

		object.setDeleted(true);
		return object;
	}

	/**
	 * Returns the object that corresponds to the specified identifier.
	 *
	 * @param identifier The identifier.
	 *
	 * @return The corresponding object.
	 */

	public T get(final String identifier) {
		final T object = data.get(identifier);
		return object == null || object.isDeleted() ? null : object;
	}

	/**
	 * Returns whether the manager has the specified object.
	 *
	 * @param object The object.
	 *
	 * @return Whether the manager has the specified object.
	 */

	public boolean has(final T object) {
		return has(object.getIdentifier());
	}

	/**
	 * Returns whether the manager has the specified identifier.
	 *
	 * @param identifier The identifier.
	 *
	 * @return Whether the manager has the specified identifier.
	 */

	public boolean has(final String identifier) {
		final T object = data.get(identifier);
		return object != null && !object.isDeleted();
	}

	/**
	 * Returns a list of managed objects.
	 *
	 * @return A list of managed objects.
	 */

	public Set<T> list() {
		return new HashSet<>(data.values());
	}

	/**
	 * Returns the managed data.
	 *
	 * @return The managed data.
	 */

	public Map<String, T> getData() {
		return data;
	}

}