package net.jbock.validate;

import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.common.Annotations;
import net.jbock.common.Util;
import net.jbock.compiler.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.TypeTool.AS_DECLARED;

@ValidateScope
public class ParameterMethodValidator {

  private final SourceElement sourceElement;
  private final Util util;

  @Inject
  ParameterMethodValidator(SourceElement sourceElement, Util util) {
    this.sourceElement = sourceElement;
    this.util = util;
  }

  public Optional<String> validateParameterMethod(ExecutableElement sourceMethod) {
    Optional<String> noAnnotationsError = util.assertAtLeastOneAnnotation(sourceMethod,
        Annotations.methodLevelAnnotations());
    if (noAnnotationsError.isPresent()) {
      return noAnnotationsError;
    }
    Optional<String> duplicateAnnotationsError = util.assertNoDuplicateAnnotations(sourceMethod,
        Annotations.methodLevelAnnotations());
    if (duplicateAnnotationsError.isPresent()) {
      return duplicateAnnotationsError;
    }
    if (sourceElement.element().getAnnotation(SuperCommand.class) != null &&
        sourceMethod.getAnnotation(Parameters.class) != null) {
      return Optional.of("@" + Parameters.class.getSimpleName()
          + " cannot be used in a @" + SuperCommand.class.getSimpleName());
    }
    if (isUnreachable(sourceMethod.getReturnType())) {
      return Optional.of("unreachable type: " + util.typeToString(sourceMethod.getReturnType()));
    }
    return Optional.empty();
  }

  private boolean isUnreachable(TypeMirror mirror) {
    TypeKind kind = mirror.getKind();
    if (kind != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declared = AS_DECLARED.visit(mirror);
    if (declared.asElement().getModifiers().contains(PRIVATE)) {
      return true;
    }
    List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
    for (TypeMirror typeArgument : typeArguments) {
      if (isUnreachable(typeArgument)) {
        return true;
      }
    }
    return false;
  }
}
