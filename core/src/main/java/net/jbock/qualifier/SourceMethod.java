package net.jbock.qualifier;

import net.jbock.compiler.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class SourceMethod {

  private final ExecutableElement sourceMethod;

  private SourceMethod(ExecutableElement sourceMethod) {
    this.sourceMethod = sourceMethod;
  }

  public static SourceMethod create(ExecutableElement sourceMethod) {
    return new SourceMethod(sourceMethod);
  }

  public ExecutableElement method() {
    return sourceMethod;
  }

  public final TypeMirror returnType() {
    return sourceMethod.getReturnType();
  }

  public ValidationFailure fail(String message) {
    return new ValidationFailure(message, sourceMethod);
  }
}
