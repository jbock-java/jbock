package net.jbock.result;

/**
 * This class represents parsing success for a
 * {@link net.jbock.SuperCommand @SuperCommand}.
 *
 * @param <C> type of the annotated class
 */
public final class SuperResult<C> {

  private final C result;
  private final String[] rest;

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
