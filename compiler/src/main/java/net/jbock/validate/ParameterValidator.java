package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.Parameter;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.MappingFinder;
import net.jbock.processor.SourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;

final class ParameterValidator {

    private final SourceElement sourceElement;
    private final MappingFinder mappingFinder;
    private final TypeTool tool;

    @Inject
    ParameterValidator(
            SourceElement sourceElement,
            MappingFinder mappingFinder,
            TypeTool tool) {
        this.sourceElement = sourceElement;
        this.mappingFinder = mappingFinder;
        this.tool = tool;
    }

    Either<List<ValidationFailure>, List<Mapping<Parameter>>> wrapPositionalParams(
            Items items) {
        return validateIndex(items.positionalParameters())
                .filter(this::validateSuperCommandHasRepeatable)
                .filter(this::validateOnlyOneRepeatable)
                .flatMap(parameters -> parameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .filter(this::checkRepeatableLast)
                .filter(this::checkRequiredBeforeOptional);
    }

    private Either<List<ValidationFailure>, List<Parameter>> validateIndex(
            List<Parameter> parameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            int index = parameter.index();
            if (index >= 0 && index != i) {
                failures.add(parameter.fail("invalid index: expecting " + i + " but found " + index));
            }
        }
        return optionalList(failures)
                .<Either<List<ValidationFailure>, List<Parameter>>>map(Either::left)
                .orElseGet(() -> right(parameters));
    }

    private Optional<List<ValidationFailure>> checkRequiredBeforeOptional(
            List<Mapping<Parameter>> positionalParameters) {
        return positionalParameters.stream()
                .filter(Mapping::isOptional)
                .findFirst()
                .map(Mapping::item)
                .flatMap(firstOptional -> positionalParameters.stream()
                        .filter(Mapping::isRequired)
                        .map(Mapping::item)
                        .filter(sourceMethod -> sourceMethod.effectiveIndex()
                                > firstOptional.effectiveIndex())
                        .map(item -> item.fail("position of required parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }

    private Optional<List<ValidationFailure>> checkRepeatableLast(
            List<Mapping<Parameter>> positionalParameters) {
        return positionalParameters.stream()
                .filter(Mapping::isRepeatable)
                .findFirst()
                .flatMap(firstRepeatable -> positionalParameters.stream()
                        .filter(m -> !m.isRepeatable())
                        .map(Mapping::item)
                        .filter(sourceMethod -> sourceMethod.effectiveIndex()
                                > firstRepeatable.item().effectiveIndex())
                        .map(item -> item.fail("position of parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of repeatable parameter '" +
                                firstRepeatable.item().method().getSimpleName() + "'"))
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
