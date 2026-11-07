package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.annotated.Items;
import net.jbock.annotated.Parameter;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;

@Inject
record ParameterValidator(
        SourceElement sourceElement,
        TypeTool tool) {

    Either<List<ValidationFailure>, List<Parameter>> wrapPositionalParams(
            Items items) {
        return validateIndex(items.parameters())
                .filter(this::validateSuperCommandHasRepeatable)
                .filter(this::validateOnlyOneRepeatable)
                .filter(this::validateConverterError)
                .filter(this::checkRepeatableLast)
                .filter(this::checkRequiredBeforeOptional);
    }

    private Either<List<ValidationFailure>, List<Parameter>> validateIndex(
            List<Parameter> parameters) {
        List<ValidationFailure> failures = null;
        for (Parameter parameter : parameters) {
            if (parameter.index() < 0) {
                continue;
            }
            if (parameter.index() != parameter.sourceIndex()) {
                if (failures == null) {
                    failures = new ArrayList<>();
                }
                failures.add(parameter.fail("invalid index: expecting " +
                        parameter.sourceIndex() +
                        " but found " +
                        parameter.index()));
            }
        }
        return optionalList(failures == null ? List.of() : failures)
                .map(Either::<List<ValidationFailure>, List<Parameter>>left)
                .orElseGet(() -> right(parameters));
    }

    private Optional<List<ValidationFailure>> checkRequiredBeforeOptional(
            List<Parameter> positionalParameters) {
        return positionalParameters.stream()
                .filter(Item::isOptional)
                .findFirst()
                .flatMap(firstOptional -> positionalParameters.stream()
                        .filter(Item::isRequired)
                        .filter(sourceMethod -> sourceMethod.sourceIndex()
                                > firstOptional.sourceIndex())
                        .map(item -> item.fail("position of required parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }

    private Optional<List<ValidationFailure>> checkRepeatableLast(
            List<Parameter> positionalParameters) {
        return positionalParameters.stream()
                .filter(Item::isRepeatable)
                .findFirst()
                .flatMap(firstRepeatable -> positionalParameters.stream()
                        .filter(m -> !m.isRepeatable())
                        .filter(sourceMethod -> sourceMethod.sourceIndex()
                                > firstRepeatable.sourceIndex())
                        .map(item -> item.fail("position of parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of repeatable parameter '" +
                                firstRepeatable.method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }

    private Optional<List<ValidationFailure>> validateOnlyOneRepeatable(
            List<Parameter> parameters) {
        return parameters.stream()
                .filter(param -> tool.isList(param.returnType()))
                .skip(1)
                .map(param -> param.fail("Only one parameter can be repeatable"))
                .collect(toOptionalList());
    }

    private Optional<List<ValidationFailure>> validateConverterError(
            List<Parameter> parameters) {
        return parameters.stream()
                .map(Parameter::validateConverterExpression)
                .flatMap(Optional::stream)
                .collect(toOptionalList());
    }

    private Optional<List<ValidationFailure>> validateSuperCommandHasRepeatable(
            List<Parameter> parameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        Optional<Parameter> repeatable = parameters.stream().filter(param -> tool.isList(param.returnType())).findFirst();
        if (repeatable.isEmpty()) {
            return Optional.of(List.of(sourceElement.fail("In a super command, a repeatable parameter must be defined")));
        }
        if (!tool.isListOfString(repeatable.orElseThrow().returnType())) {
            return Optional.of(List.of(sourceElement.fail("Only List<String> allowed in a super command")));
        }
        return Optional.empty();
    }
}
