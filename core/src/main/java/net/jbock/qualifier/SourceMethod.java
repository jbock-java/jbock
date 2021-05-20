package net.jbock.qualifier;

import net.jbock.compiler.ValidationFailure;
import net.jbock.compiler.parameter.ParameterStyle;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ACCESS_MODIFIERS;

public class SourceMethod {

  private final ExecutableElement sourceMethod;
  private final ParameterStyle parameterStyle;
  private final Set<Modifier> accessModifiers;

  private SourceMethod(
      ExecutableElement sourceMethod,
      ParameterStyle parameterStyle,
      Set<Modifier> accessModifiers) {
    this.sourceMethod = sourceMethod;
    this.parameterStyle = parameterStyle;
    this.accessModifiers = accessModifiers;
  }

  public static SourceMethod create(ExecutableElement sourceMethod) {
    Set<Modifier> accessModifiers = sourceMethod.getModifiers().stream()
        .filter(ACCESS_MODIFIERS::contains)
        .collect(Collectors.toSet());
    ParameterStyle parameterStyle = ParameterStyle.getStyle(sourceMethod);
    return new SourceMethod(sourceMethod, parameterStyle, accessModifiers);
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
    return parameterStyle.description(sourceMethod, elements);
  }

  public Optional<String> paramLabel() {
    return parameterStyle.paramLabel(sourceMethod);
  }

  public Set<Modifier> accessModifiers() {
    return accessModifiers;
  }
}
