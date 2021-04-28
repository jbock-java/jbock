package net.jbock.compiler.parameter;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Param;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.coerce.Util;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

/**
 * This class represents an {@code abstract} Method in the command class,
 * which can be either an {@link Option} or an {@link Param}.
 */
public abstract class Parameter {

  private final ExecutableElement sourceMethod;

  private final String bundleKey;

  private final Coercion coercion;

  private final List<String> description;

  Parameter(ExecutableElement sourceMethod, String bundleKey,
            Coercion coercion, List<String> description) {
    this.sourceMethod = sourceMethod;
    this.bundleKey = bundleKey;
    this.coercion = coercion;
    this.description = description;
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
    return enumName().enumConstant();
  }

  public abstract boolean isPositional();

  public abstract OptionalInt positionalIndex();

  public boolean isRequired() {
    return coercion.skew() == Skew.REQUIRED;
  }

  public boolean isRepeatable() {
    return coercion.skew() == Skew.REPEATABLE;
  }

  public boolean isOptional() {
    return coercion.skew() == Skew.OPTIONAL;
  }

  public boolean isFlag() {
    return coercion.skew() == Skew.FLAG;
  }

  public Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  public EnumName enumName() { // TODO
    return coercion.enumName();
  }

  public Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public abstract List<String> dashedNames();

  public abstract String sample();

  public abstract String optionName();

  public abstract char mnemonic();

  public ExecutableElement sourceMethod() {
    return sourceMethod;
  }

  public CodeBlock getNames() {
    List<String> names = dashedNames();
    switch (names.size()) {
      case 0:
        return CodeBlock.of("$T.emptyList()", Collections.class);
      case 1:
        return CodeBlock.of("$T.singletonList($S)", Collections.class, names.get(0));
      default:
        return Util.arraysOfStringInvocation(names);
    }
  }
}
