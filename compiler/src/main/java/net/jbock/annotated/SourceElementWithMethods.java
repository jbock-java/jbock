package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.SnakeName;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.common.Util.checkNoDuplicateAnnotations;

final class SourceElementWithMethods {

    private final SourceElement sourceElement;
    private final List<Executable> methods;
    private final UniqueNameSet uniqueNameSet = new UniqueNameSet();

    SourceElementWithMethods(SourceElement sourceElement, List<Executable> methods) {
        this.methods = methods;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, List<AnnotatedMethod<?>>> validListOfAnnotatedMethods() {
        return methods.stream()
                .map(this::createAnnotatedMethod)
                .collect(allFailures());
    }

    private Either<ValidationFailure, AnnotatedMethod<?>> createAnnotatedMethod(
            Executable sourceMethod) {
        String enumName = enumNameFor(sourceMethod.simpleName());
        ExecutableElement method = sourceMethod.method();
        return checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .<Either<ValidationFailure, AnnotatedMethod<?>>>map(Either::left)
                .orElseGet(() -> right(sourceMethod.annotatedMethod(sourceElement, enumName)))
                .filter(this::checkAccessibleReturnType);
    }

    private Optional<ValidationFailure> checkAccessibleReturnType(
            AnnotatedMethod<?> annotatedMethod) {
        return AS_DECLARED.visit(annotatedMethod.returnType())
                .filter(this::isInaccessible)
                .map(type -> annotatedMethod.fail("inaccessible type: " +
                        Util.typeToString(type)));
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

    private String enumNameFor(Name sourceMethodName) {
        String enumName = "_".contentEquals(sourceMethodName) ?
                "_1" : // avoid potential keyword issue
                SnakeName.create(sourceMethodName).snake('_').toUpperCase(Locale.ROOT);
        return uniqueNameSet.getUniqueName(enumName);
    }
}
