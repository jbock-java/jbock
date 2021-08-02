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
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        List<Class<? extends Annotation>> annotations = methodLevelAnnotations();
        ExecutableElement method = sourceMethod.method();
        return util.checkNoDuplicateAnnotations(method, annotations)
                .<Either<String, Annotation>>map(Either::left)
                .orElseGet(() -> Either.right(sourceMethod.annotation()))
                .map(a -> AnnotatedMethod.create(method, a, enumNames.get(sourceMethod.getSimpleName())))
                .filter(a -> checkAccessibleType(method.getReturnType()))
                .mapLeft(msg -> new ValidationFailure(msg, method));
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
