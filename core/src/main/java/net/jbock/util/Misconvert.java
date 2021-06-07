package net.jbock.util;

/**
 * An instance of this class represents converter failure.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link ConverterThrewException}</li>
 *   <li>{@link ConverterReturnedNull}</li>
 * </ul>
 */
public abstract class Misconvert {

  Misconvert() {
  }

  /**
   * Returns an error message to describe the failure.
   *
   * @return error message
   */
  public abstract String converterMessage();
}
