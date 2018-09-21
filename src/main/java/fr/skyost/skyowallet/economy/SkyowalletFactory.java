package fr.skyost.skyowallet.economy;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import fr.skyost.skyowallet.Skyowallet;

/**
 * Represents a Skyowallet factory.
 *
 * @param <T> The type of object to create.
 * @param <I> Type of the identifier.
 */

public abstract class SkyowalletFactory<T extends EconomyObject, I> {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * Creates a new Skyowallet account factory instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletFactory(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public final void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Creates a new object instance from a JSON string.
	 *
	 * @param file The JSON file.
	 *
	 * @return The object instance.
	 *
	 * @throws IOException If an error occurs while reading the file.
	 */

	public T createFromJSON(final File file) throws IOException {
		return createFromJSON(Files.readFirstLine(file, StandardCharsets.UTF_8));
	}

	/**
	 * Creates a new object instance from a JSON string.
	 *
	 * @param json The JSON string.
	 *
	 * @return The object instance.
	 */

	public abstract T createFromJSON(final String json);

	/**
	 * Creates a new object instance.
	 *
	 * @param identifier The identifier.
	 *
	 * @return The object instance.
	 */

	public abstract T create(final I identifier);

	/**
	 * Creates a new object instance and adds it to the specified manager.
	 *
	 * @param identifier The identifier
	 * @param manager The manager.
	 *
	 * @return The object instance.
	 */

	public T create(final I identifier, final SkyowalletManager<T> manager) {
		final T object = create(identifier);
		manager.add(object);
		return object;
	}

}