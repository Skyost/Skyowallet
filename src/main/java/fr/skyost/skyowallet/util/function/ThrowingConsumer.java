package fr.skyost.skyowallet.util.function;

import java.util.function.Consumer;

/**
 * Allows consumers to throw errors.
 *
 * @param <T> Argument type.
 */

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

	@Override
	default void accept(final T elem) {
		try {
			acceptThrows(elem);
		}
		catch(final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Accepts but may throws exceptions.
	 *
	 * @param argument The argument.
	 *
	 * @throws Exception The exception, if thrown.
	 */

	void acceptThrows(final T argument) throws Exception;

}