package net.jbock.compiler;

import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Param;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

/**
 * This class represents either an {@link Option} or a {@link Param}.
 */
public final class Parameter {

  // null iff this is a param
  final String optionName;

  // ' ' if this is a param
  final char mnemonic;

  private final ExecutableElement sourceMethod;

  final String bundleKey;

  private final String sample;

  private final List<String> dashedNames;

  private final Coercion coercion;

  private final List<String> description;

  private final Integer positionalIndex;

  Parameter(char mnemonic, String optionName, ExecutableElement sourceMethod, String bundleKey, String sample,
            List<String> dashedNames, Coercion coercion, List<String> description, Integer positionalIndex) {
    this.mnemonic = mnemonic;
    this.optionName = optionName;
    this.sourceMethod = sourceMethod;
    this.bundleKey = bundleKey;
    this.sample = sample;
    this.dashedNames = dashedNames;
    this.coercion = coercion;
    this.description = description;
    this.positionalIndex = positionalIndex;
  }

  public Coercion coercion() {
    return coercion;
  }

  public List<String> description() {
    return description;
  }

  public String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  public TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  public String enumConstant() {
    return paramName().enumConstant();
  }

  public boolean isPositional() {
    return positionalIndex != null;
  }

  public OptionalInt positionalIndex() {
    return positionalIndex != null ? OptionalInt.of(positionalIndex) : OptionalInt.empty();
  }

  public boolean isRequired() {
    return coercion.getSkew() == Skew.REQUIRED;
  }

  public boolean isRepeatable() {
    return coercion.getSkew() == Skew.REPEATABLE;
  }

  public boolean isOptional() {
    return coercion.getSkew() == Skew.OPTIONAL;
  }

  public boolean isFlag() {
    return coercion.getSkew() == Skew.FLAG;
  }

  public Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  OptionalInt positionalOrder() {
    if (positionalIndex == null) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(isRepeatable() ? 2 : isOptional() ? 1 : 0);
  }

  public EnumName paramName() {
    return coercion.paramName();
  }

  ValidationException validationError(String message) {
    return ValidationException.create(sourceMethod, message);
  }

  public Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public List<String> dashedNames() {
    return dashedNames;
  }

  public String sample() {
    return sample;
  }
}
