package net.jbock.compiler.parameter;

import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

/**
 * This class represents an {@code abstract} Method in the command class,
 * which can be either an {@link Option} or a {@link Parameter}.
 */
public abstract class AbstractParameter {

  private final ExecutableElement sourceMethod;

  private final EnumName enumName;

  private final String bundleKey;

  private final List<String> description;

  AbstractParameter(
      ExecutableElement sourceMethod,
      EnumName enumName,
      String bundleKey,
      List<String> description) {
    this.sourceMethod = sourceMethod;
    this.enumName = enumName;
    this.bundleKey = bundleKey;
    this.description = description;
  }

  public final List<String> description() {
    return description;
  }

  public final String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  public final TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  public abstract boolean isPositional();

  public abstract OptionalInt positionalIndex();

  public final Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  public final Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public abstract List<String> dashedNames();

  public abstract String sample(boolean isFlag, EnumName enumName);

  public final ExecutableElement sourceMethod() {
    return sourceMethod;
  }

  public final EnumName enumName() {
    return enumName;
  }

  public abstract ParameterStyle style();

}
