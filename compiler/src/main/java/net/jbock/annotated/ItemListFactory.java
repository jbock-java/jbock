package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
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

    static Either<List<ValidationFailure>, List<SimpleItem>> createItemList(
            List<ExecutableElement> methods) {
        return new ItemListFactory().validParameterlessAbstract(methods);
    }

    private Either<List<ValidationFailure>, List<SimpleItem>> validParameterlessAbstract(
            List<ExecutableElement> methods) {
        return methods.stream()
                .map(this::createItem)
                .collect(allFailures());
    }

    private Either<ValidationFailure, SimpleItem> createItem(
            ExecutableElement method) {
        String safeName = uniqueNameSet.getUniqueName(method.getSimpleName().toString());
        return getMethodAnnotation(method)
                .map(a -> SimpleItem.create(method, a, safeName));
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
