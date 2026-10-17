package net.jbock.annotated;

import io.jbock.util.Eithers;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Util.checkNoDuplicateAnnotations;

final class ItemListValidator {

    static Optional<List<ValidationFailure>> validate(Items items) {
        List<ValidationFailure> result = new ArrayList<>();
        for (Option option : items.namedOptions()) {
            basicChecks(option).ifPresent(result::add);
        }
        for (Parameter parameter : items.positionalParameters()) {
            basicChecks(parameter).ifPresent(result::add);
        }
        return Eithers.optionalList(result);
    }

    private static Optional<ValidationFailure> basicChecks(Item item) {
        ExecutableElement method = item.method();
        return checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .or(() -> ItemListValidator.checkNullary(item));
    }

    private static Optional<ValidationFailure> checkNullary(Item item) {
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

    private ItemListValidator() {
    }
}
