package net.jbock.compiler.parameter;

import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.compiler.Description;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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

  private final ExecutableElement sourceMethod;

  private final EnumName enumName; // unique internal name

  private final DescriptionKey descriptionKey;

  private final Description description;

  private final ParamLabel paramLabel;

  AbstractParameter(
      SourceMethod sourceMethod,
      EnumName enumName,
      DescriptionKey descriptionKey,
      Description description,
      ParamLabel paramLabel) {
    this.sourceMethod = sourceMethod.method();
    this.enumName = enumName;
    this.descriptionKey = descriptionKey;
    this.description = description;
    this.paramLabel = paramLabel;
  }

  public final List<String> description() {
    return description.lines();
  }

  public final String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  public final TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  public final boolean isOption() {
    return style() == ParameterStyle.OPTION;
  }

  public final Optional<String> descriptionKey() {
    return descriptionKey.key();
  }

  final Optional<String> label() {
    return paramLabel.label();
  }

  public final Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }

  public final ExecutableElement sourceMethod() {
    return sourceMethod;
  }

  public final EnumName enumName() {
    return enumName;
  }

  public abstract ParameterStyle style();
}
