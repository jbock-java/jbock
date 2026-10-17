package net.jbock.annotated;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.jbock.util.Eithers.optionalList;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.CONVERTERS;
import static net.jbock.common.Util.checkNoDuplicateAnnotations;

public record Items(
        List<Option> options,
        List<Parameter> parameters) {

    static Either<List<ValidationFailure>, Items> createItems(
            List<ExecutableElement> methods) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet(methods.size() + 1);
        uniqueNameSet.reserve(CONVERTERS);
        List<ValidationFailure> failures = new ArrayList<>();
        int io = 0, ip = 0;
        List<Option> options = new ArrayList<>(methods.size());
        List<Parameter> parameters = new ArrayList<>(methods.size());
        for (ExecutableElement method : methods) {
            String safeName = uniqueNameSet.getUniqueName(method.getSimpleName().toString());
            Either<ValidationFailure, Annotation> e = getMethodAnnotation(method);
            if (e.isLeft()) {
                failures.add(e.getLeft().orElseThrow());
                continue;
            }
            Annotation a = e.getRight().orElseThrow();
            switch (a) {
                case net.jbock.Option option -> //
                        options.add(Option.createOption(method, option, safeName, io++));
                case net.jbock.Parameter parameter -> //
                        parameters.add(Parameter.createParameter(method, parameter, safeName, ip++));
                default -> throw new AssertionError("unknown annotation " + a);
            }

        }
        return optionalList(failures)
                .map(Either::<List<ValidationFailure>, Items>left)
                .orElseGet(() -> Either.right(new Items(options, parameters)));
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

    Optional<List<ValidationFailure>> validate() {
        List<ValidationFailure> result = new ArrayList<>();
        for (Option option : options()) {
            basicChecks(option).ifPresent(result::add);
        }
        for (Parameter parameter : parameters()) {
            basicChecks(parameter).ifPresent(result::add);
        }
        return Eithers.optionalList(result);
    }

    private static Optional<ValidationFailure> basicChecks(Item item) {
        ExecutableElement method = item.method();
        return checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .or(() -> Items.checkNullary(item));
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
