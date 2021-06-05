package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * An instance of this class represents parsing failure.
 * There are a fixed number of subclasses:
 *
 * <ul>
 *   <li>{@link SyntaxError}</li>
 *   <li>{@link ConverterError}</li>
 * </ul>
 *
 */
public abstract class ParsingError extends NotSuccess {

  public ParsingError(CommandModel commandModel) {
    super(commandModel);
  }

  /**
   * Returns an error message to describe the failure.
   *
   * @return error message
   */
  public abstract String message();
}
