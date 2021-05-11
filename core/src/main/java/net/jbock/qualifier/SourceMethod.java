package net.jbock.qualifier;

import net.jbock.compiler.ValidationFailure;

import javax.lang.model.element.ExecutableElement;

public class SourceMethod {

  private final ExecutableElement sourceMethod;

  public SourceMethod(ExecutableElement sourceMethod) {
    this.sourceMethod = sourceMethod;
  }

  public ExecutableElement method() {
    return sourceMethod;
  }

  public ValidationFailure fail(String message) {
    return new ValidationFailure(message, sourceMethod);
  }
}
