package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Util.checkNoDuplicateAnnotations;

final class ItemListValidator {

    private ItemListValidator() {
    }

    static Either<List<ValidationFailure>, List<Item>> validate(
            List<Item> items) {
        return items.stream()
                .map(ItemListValidator::createAnnotatedMethod)
                .collect(allFailures());
    }

    private static Either<ValidationFailure, Item> createAnnotatedMethod(
            Item item) {
        ExecutableElement method = item.method();
        return checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .<Either<ValidationFailure, Item>>map(Either::left)
                .orElseGet(() -> right(item))
                .filter(ItemListValidator::checkNullary);
    }

    private static Optional<ValidationFailure> checkNullary(Item item) {
        if (item.method().getParameters().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(item.fail("invalid method parameters: abstract method '" +
                item.method().getSimpleName() +
                "' may not have any parameters, but found: " +
                item.method().getParameters().stream()
                        .map(VariableElement::getSimpleName)
                        .map(Name::toString)
                        .toList()));
    }
}
