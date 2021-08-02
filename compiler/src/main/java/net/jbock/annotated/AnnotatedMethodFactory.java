package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.EnumName;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class AnnotatedMethodFactory {

    private final Util util;

    @Inject
    AnnotatedMethodFactory(Util util) {
        this.util = util;
    }

    Either<ValidationFailure, AnnotatedMethod> createAnnotatedMethod(
            SimpleAnnotated sourceMethod,
            Map<Name, EnumName> enumNames) {
        ExecutableElement method = sourceMethod.method();
        EnumName enumName = enumNames.get(sourceMethod.getSimpleName());
        Annotation annotation = sourceMethod.annotation();
        return util.checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .<Either<ValidationFailure, AnnotatedMethod>>map(Either::left)
                .orElseGet(() -> right(AnnotatedMethod.create(method, annotation, enumName)))
                .filter(this::checkAccessibleReturnType);
    }

    /* Left-Optional
     */
    private Optional<ValidationFailure> checkAccessibleReturnType(
            AnnotatedMethod annotatedMethod) {
        ExecutableElement method = annotatedMethod.method();
        return AS_DECLARED.visit(method.getReturnType())
                .filter(this::isInaccessible)
                .map(type -> "inaccessible type: " + util.typeToString(type))
                .map(message -> new ValidationFailure(message, method));
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
