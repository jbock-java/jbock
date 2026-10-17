package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.jbock.util.Eithers.optionalList;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.CONVERTERS;

final class ItemListFactory {

    static Either<List<ValidationFailure>, List<SimpleItem>> createItemList(
            List<ExecutableElement> methods) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        uniqueNameSet.reserve("_");
        uniqueNameSet.reserve(CONVERTERS);
        List<ValidationFailure> failures = new ArrayList<>();
        List<SimpleItem> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            String safeName = uniqueNameSet.getUniqueName(method.getSimpleName().toString());
            getMethodAnnotation(method).ifLeftOrElse(
                    failures::add,
                    a -> result.add(SimpleItem.create(method, a, safeName)));
        }
        return optionalList(failures)
                .map(Either::<List<ValidationFailure>, List<SimpleItem>>left)
                .orElseGet(() -> Either.right(result));
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

    private ItemListFactory() {
    }
}
