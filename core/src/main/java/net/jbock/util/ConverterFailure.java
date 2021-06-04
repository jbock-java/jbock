package net.jbock.util;

/**
 * An implementation of this interface represents converter failure.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link ConverterThrewException}</li>
 *   <li>{@link ConverterReturnedNull}</li>
 * </ul>
 *
 *  This interface will be a sealed interface in later versions.
 */
public interface ConverterFailure {

  /**
   * Returns an error message to describe the failure.
   *
   * @return error message
   */
  String message();
}
