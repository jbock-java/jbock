package net.jbock.util;

/**
 * This class represents parsing success for a
 * {@link net.jbock.SuperCommand @SuperCommand}.
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

  public C result() {
    return command;
  }

  public String[] rest() {
    return rest;
  }
}
