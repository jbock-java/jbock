package net.jbock.qualifier;

import net.jbock.compiler.ValidationFailure;
import net.jbock.compiler.parameter.ParameterStyle;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class SourceMethod {

  private final ExecutableElement sourceMethod;
  private final ParameterStyle parameterStyle;

  private SourceMethod(ExecutableElement sourceMethod, ParameterStyle parameterStyle) {
    this.sourceMethod = sourceMethod;
    this.parameterStyle = parameterStyle;
  }

  public static SourceMethod create(ExecutableElement sourceMethod) {
    ParameterStyle parameterStyle = ParameterStyle.getStyle(sourceMethod);
    return new SourceMethod(sourceMethod, parameterStyle);
  }

  public ExecutableElement method() {
    return sourceMethod;
  }

  public TypeMirror returnType() {
    return sourceMethod.getReturnType();
  }

  public ParameterStyle style() {
    return parameterStyle;
  }

  public OptionalInt index() {
    return parameterStyle.index(sourceMethod);
  }

  public Optional<String> descriptionKey() {
    return parameterStyle.descriptionKey(sourceMethod);
  }

  public ValidationFailure fail(String message) {
    return new ValidationFailure(message, sourceMethod);
  }

  public List<String> names() {
    return parameterStyle.names(sourceMethod);
  }

  public List<String> description(Elements elements) {
    return Arrays.asList(parameterStyle.description(sourceMethod, elements));
  }
}
