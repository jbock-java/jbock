package net.jbock.util;

/**
 * This class represents parsing success for a
 * {@link net.jbock.SuperCommand @SuperCommand}.
 *
 * @param <C> command type
 */
public final class SuperResult<C> {

  private final C result;
  private final String[] rest;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param result
   * @param rest
   */
  public SuperResult(C result, String[] rest) {
    this.result = result;
    this.rest = rest;
  }

  public C result() {
    return result;
  }

  public String[] rest() {
    return rest;
  }
}
