package net.jbock.validate;

import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class SourceMethodValidator {

    private final Util util;

    @Inject
    SourceMethodValidator(Util util) {
        this.util = util;
    }

    /* Left-Optional
     */
    Optional<ValidationFailure> validateSourceMethod(ExecutableElement sourceMethod) {
        List<Class<? extends Annotation>> annotations = methodLevelAnnotations();
        return util.checkAtLeastOneAnnotation(sourceMethod, annotations)
                .or(() -> util.checkNoDuplicateAnnotations(sourceMethod, annotations))
                .or(() -> checkAccessibleType(sourceMethod.getReturnType()))
                .map(msg -> new ValidationFailure(msg, sourceMethod));
    }

    /* Left-Optional
     */
    private Optional<String> checkAccessibleType(TypeMirror returnType) {
        return AS_DECLARED.visit(returnType)
                .filter(this::isInaccessible)
                .map(type -> "inaccessible type: " + util.typeToString(type));
    }

    private boolean isInaccessible(DeclaredType declared) {
        if (declared.asElement().getModifiers().contains(PRIVATE)) {
            return true;
        }
        if (AS_TYPE_ELEMENT.visit(declared.asElement())
                .filter(t -> t.getNestingKind() == MEMBER)
                .filter(t -> !t.getModifiers().contains(STATIC))
                .isPresent()) {
            return true;
        }
        return declared.getTypeArguments().stream()
                .map(AS_DECLARED::visit)
                .flatMap(Optional::stream)
                .anyMatch(this::isInaccessible);
    }
}
