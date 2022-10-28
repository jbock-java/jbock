package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
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
                .filter(ItemListValidator::validateParameterless)
                .filter(ItemListValidator::checkAccessibleReturnType);
    }

    private static Optional<ValidationFailure> validateParameterless(
            Item method) {
        if (method.method().getParameters().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(method.fail("invalid method parameters: abstract method '" +
                method.method().getSimpleName() +
                "' may not have any parameters, but found: " +
                method.method().getParameters().stream()
                        .map(VariableElement::getSimpleName)
                        .map(Name::toString)
                        .collect(toList())));
    }

    private static Optional<ValidationFailure> checkAccessibleReturnType(Item item) {
        return AS_DECLARED.visit(item.returnType())
                .filter(ItemListValidator::isInaccessible)
                .map(type -> item.fail("inaccessible type: " +
                        Util.typeToString(type)));
    }

    private static boolean isInaccessible(DeclaredType declared) {
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
                .anyMatch(ItemListValidator::isInaccessible);
    }
}
