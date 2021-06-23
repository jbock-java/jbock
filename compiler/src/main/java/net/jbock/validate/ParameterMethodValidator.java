package net.jbock.validate;

import net.jbock.common.Annotations;
import net.jbock.common.Util;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class ParameterMethodValidator {

  private final Util util;

  @Inject
  ParameterMethodValidator(Util util) {
    this.util = util;
  }

  Optional<String> validateParameterMethod(ExecutableElement sourceMethod) {
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
    if (isUnreachable(sourceMethod.getReturnType())) {
      return Optional.of("unreachable type: " + util.typeToString(sourceMethod.getReturnType()));
    }
    return Optional.empty();
  }

  private boolean isUnreachable(TypeMirror mirror) {
    return AS_DECLARED.visit(mirror)
        .map(declared -> {
          Element el = declared.asElement();
          if (el.getModifiers().contains(PRIVATE)) {
            return true;
          }
          boolean badNesting = AS_TYPE_ELEMENT.visit(el)
              .map(t -> t.getNestingKind() == MEMBER
                  && !t.getModifiers().contains(STATIC))
              .orElse(false);
          return badNesting || declared.getTypeArguments().stream()
              .anyMatch(this::isUnreachable);
        })
        .orElse(false);
  }
}
