package net.jbock.coerce.reference;

public class TypecheckFailure {
  
  private final String message;
  private final boolean fatal;

  private TypecheckFailure(String message, boolean fatal) {
    this.message = message;
    this.fatal = fatal;
  }

  public static TypecheckFailure fatal(String message) {
    return new TypecheckFailure(message, true);
  }

  public static TypecheckFailure nonFatal(String message) {
    return new TypecheckFailure(message, false);
  }

  public String getMessage() {
    return message;
  }

  public boolean isFatal() {
    return fatal;
  }
}
