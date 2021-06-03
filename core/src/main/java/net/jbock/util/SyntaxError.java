package net.jbock.util;

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
public final class SyntaxError implements Failure {

  private final String message;

  public SyntaxError(String message) {
    this.message = message;
  }

  public String message() {
    return message;
  }
}
