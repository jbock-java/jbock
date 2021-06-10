package net.jbock.util;

import net.jbock.SuperCommand;

/**
 * This class represents successful parsing for a
 * {@link SuperCommand}.
 *
 * @param <C> command type
 */
public final class SuperResult<C> {

  private final C command;
  private final String[] rest;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param command command instance
   * @param rest remaining tokens from the input array
   */
  public SuperResult(C command, String[] rest) {
    this.command = command;
    this.rest = rest;
  }

  /**
   * Get the result of parsing.
   *
   * @return the result
   */
  public C result() {
    return command;
  }

  /**
   * Get the remaining tokens from the input array,
   * after the last known parameter was read by
   * the SuperCommand parser.
   *
   * @return remaining tokens
   */
  public String[] rest() {
    return rest;
  }
}
