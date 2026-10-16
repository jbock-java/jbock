package net.jbock.annotated;

import io.jbock.util.Eithers;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Util.checkNoDuplicateAnnotations;

final class ItemListValidator {

    private ItemListValidator() {
    }

    static Optional<List<ValidationFailure>> validate(
            List<SimpleItem> items) {
        return Eithers.optionalList(items.stream()
                .map(ItemListValidator::createAnnotatedMethod)
                .flatMap(Optional::stream)
                .toList());
    }

    private static Optional<ValidationFailure> createAnnotatedMethod(
            SimpleItem item) {
        ExecutableElement method = item.method();
        return checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .or(() -> ItemListValidator.checkNullary(item));
    }

    private static Optional<ValidationFailure> checkNullary(SimpleItem item) {
        if (item.method().getParameters().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValidationFailure("invalid method parameters: abstract method '" +
                item.method().getSimpleName() +
                "' may not have any parameters, but found: " +
                item.method().getParameters().stream()
                        .map(VariableElement::getSimpleName)
                        .map(Name::toString)
                        .toList(), item.method()));
    }
}
