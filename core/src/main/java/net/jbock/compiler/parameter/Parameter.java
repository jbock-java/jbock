package net.jbock.compiler.parameter;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Param;
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

  private final List<String> description;

  Parameter(ExecutableElement sourceMethod, String bundleKey, List<String> description) {
    this.sourceMethod = sourceMethod;
    this.bundleKey = bundleKey;
    this.description = description;
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

  public abstract boolean isPositional();

  public abstract OptionalInt positionalIndex();

  public Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  public Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public abstract List<String> dashedNames();

  public abstract String sample(boolean isFlag, EnumName enumName);

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
