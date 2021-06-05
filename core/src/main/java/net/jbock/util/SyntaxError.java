package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates a parsing syntax error, for example:
 *
 * <ul>
 *   <li>missing parameter</li>
 *   <li>missing argument</li>
 *   <li>unknown option name</li>
 *   <li>excess parameter</li>
 * </ul>
 */
public final class SyntaxError extends ParsingError {

  private final String message;

  public SyntaxError(CommandModel commandModel, String message) {
    super(commandModel);
    this.message = message;
  }

  public String message() {
    return message;
  }
}
