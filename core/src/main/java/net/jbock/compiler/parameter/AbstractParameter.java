package net.jbock.compiler.parameter;

import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ValidationFailure;
import net.jbock.qualifier.SourceMethod;

import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

/**
 * This class represents an {@code abstract} Method in the command class,
 * which can be either an {@link Option} or a {@link Parameter}.
 */
public abstract class AbstractParameter {

  private final SourceMethod sourceMethod;
  private final EnumName enumName; // unique internal name

  AbstractParameter(
      SourceMethod sourceMethod,
      EnumName enumName) {
    this.sourceMethod = sourceMethod;
    this.enumName = enumName;
  }

  public final List<String> description(Elements elements) {
    return sourceMethod.description(elements);
  }

  public final String methodName() {
    return sourceMethod.method().getSimpleName().toString();
  }

  public final TypeName returnType() {
    return TypeName.get(sourceMethod.method().getReturnType());
  }

  public final Optional<String> descriptionKey() {
    return sourceMethod.descriptionKey();
  }

  public final Set<Modifier> getAccessModifiers() {
    return sourceMethod.method().getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public final ValidationFailure fail(String message) {
    return sourceMethod.fail(message);
  }

  final EnumName enumName() {
    return enumName;
  }
}
