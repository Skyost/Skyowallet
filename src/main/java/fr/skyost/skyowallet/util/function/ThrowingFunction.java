package fr.skyost.skyowallet.util.function;

import java.util.function.Function;

/**
 * Allows functions to throw errors.
 *
 * @param <A> Argument type.
 * @param <V> Value type.
 */

@FunctionalInterface
public interface ThrowingFunction<A, V> extends Function<A, V> {

	@Override
	default V apply(final A argument) {
		try {
			return applyThrows(argument);
		}
		catch(final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Applies but may throws exceptions.
	 *
	 * @param argument The argument.
	 *
	 * @return The return type.
	 *
	 * @throws Exception The exception, if thrown.
	 */

	V applyThrows(final A argument) throws Exception;

}
