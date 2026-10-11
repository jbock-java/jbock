package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

import static io.jbock.util.Eithers.allFailures;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.CONVERTERS;

final class ItemListFactory {

    private final UniqueNameSet uniqueNameSet = new UniqueNameSet();

    private ItemListFactory() {
        uniqueNameSet.reserve("_");
        uniqueNameSet.reserve(CONVERTERS);
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
        String enumName = sourceMethodName.toString();
        return uniqueNameSet.getUniqueName(enumName);
    }

    private static Either<ValidationFailure, Annotation> getMethodAnnotation(
            ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .findFirst()
                .<Either<ValidationFailure, Annotation>>map(Either::right)
                .orElseGet(() -> Either.left(new ValidationFailure("@Option or @Parameter annotation expected", method)));
    }
}
