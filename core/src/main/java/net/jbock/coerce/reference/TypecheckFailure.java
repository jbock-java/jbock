package net.jbock.coerce.reference;

/**
 * Indicates a typechecking issue that is potentially recoverable .
 */
public class TypecheckFailure {

  private final String message;

  private TypecheckFailure(String message) {
    this.message = message;
  }

  public static TypecheckFailure typeFail(String message) {
    return new TypecheckFailure(message);
  }

  public String getMessage() {
    return message;
  }

}
