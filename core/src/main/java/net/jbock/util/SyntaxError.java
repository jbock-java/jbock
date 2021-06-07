package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates a parsing syntax error, for example:
 *
 * <ul>
 *   <li>missing required parameter</li>
 *   <li>missing argument</li>
 *   <li>unknown option name</li>
 *   <li>excess parameter</li>
 * </ul>
 */
public final class SyntaxError extends NotSuccess implements HasMessage {

  private final String message;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param commandModel command model
   * @param message error message
   */
  public SyntaxError(CommandModel commandModel, String message) {
    super(commandModel);
    this.message = message;
  }

  @Override
  public String message() {
    return message;
  }
}
