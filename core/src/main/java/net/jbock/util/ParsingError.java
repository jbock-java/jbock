package net.jbock.util;

/**
 * An implementation of this interface represents parsing failure.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link SyntaxError}</li>
 *   <li>{@link ConverterError}</li>
 * </ul>
 *
 * This interface will be a sealed interface in later versions.
 */
public interface ParsingError extends NotSuccess {

  /**
   * Returns an error message to describe the failure.
   *
   * @return error message
   */
  String message();
}
