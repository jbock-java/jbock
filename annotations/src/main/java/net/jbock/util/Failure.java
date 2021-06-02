package net.jbock.util;

/**
 * An implementation of this interface represents parsing failure.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link SyntaxError}</li>
 *   <li>{@link ConversionError}</li>
 * </ul>
 *
 * This interface will be a sealed interface in later versions.
 */
public interface Failure extends NotSuccess {

  String message();
}
