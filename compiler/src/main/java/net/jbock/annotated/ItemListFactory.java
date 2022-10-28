package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static io.jbock.util.Either.left;
import static io.jbock.util.Eithers.allFailures;
import static java.util.stream.Collectors.toList;
import static net.jbock.common.Annotations.methodLevelAnnotations;

final class ItemListFactory {

    private final UniqueNameSet uniqueNameSet = new UniqueNameSet();

    private ItemListFactory() {
    }

    static Either<List<ValidationFailure>, List<Item>> createItemList(
            List<ExecutableElement> methods) {
        return new ItemListFactory().validParameterlessAbstract(methods);
    }

    private Either<List<ValidationFailure>, List<Item>> validParameterlessAbstract(
            List<ExecutableElement> methods) {
        return methods.stream()
                .map(this::createItem)
                .collect(allFailures());
    }

    private Either<ValidationFailure, Item> createItem(
            ExecutableElement method) {
        String enumName = enumNameFor(method.getSimpleName());
        return getMethodAnnotation(method)
                .map(a -> Item.create(method, a, enumName));
    }

    private String enumNameFor(Name sourceMethodName) {
        String enumName = "_".contentEquals(sourceMethodName) ?
                "_1" : // avoid potential keyword issue
                SnakeName.create(sourceMethodName).snake('_').toUpperCase(Locale.ROOT);
        return uniqueNameSet.getUniqueName(enumName);
    }

    private static Either<ValidationFailure, Annotation> getMethodAnnotation(
            ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .findFirst()
                .<Either<ValidationFailure, Annotation>>map(Either::right)
                .orElseGet(() -> left(missingAnnotationError(method)));
    }

    private static ValidationFailure missingAnnotationError(
            ExecutableElement method) {
        String message = "missing annotation: add one of these annotations: " + methodLevelAnnotations().stream()
                .map(Class::getSimpleName).collect(toList());
        message = message + " to method '" + method.getSimpleName() + "'";
        return new ValidationFailure(message, method);
    }
}
