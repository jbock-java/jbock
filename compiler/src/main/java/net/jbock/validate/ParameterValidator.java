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
        return validatePositions(items.positionalParameters())
                .filter(this::validateParametersInSuperCommand)
                .filter(this::validateParameterIsNotList)
                .flatMap(parameters -> parameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Either<List<ValidationFailure>, List<Parameter>> validatePositions(
            List<Parameter> parameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            int index = parameter.index();
            if (index != i) {
                failures.add(parameter.fail("invalid position: expecting " + i + " but found " + index));
            }
        }
        return optionalList(failures)
                .<Either<List<ValidationFailure>, List<Parameter>>>map(Either::left)
                .orElseGet(() -> right(parameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(
            List<Mapping<Parameter>> positionalParameters) {
        return positionalParameters.stream()
                .filter(Mapping::isOptional)
                .findFirst()
                .map(Mapping::item)
                .flatMap(firstOptional -> positionalParameters.stream()
                        .filter(Mapping::isRequired)
                        .map(Mapping::item)
                        .filter(sourceMethod -> sourceMethod.index()
                                > firstOptional.index())
                        .map(item -> item.fail("position of required parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateParameterIsNotList(
            List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (tool.isList(parameter.returnType())) {
                return Optional.of(List.of(parameter.fail("a parameter may not be a list; drop the annotation or use @VarargsParameter")));
            }
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateParametersInSuperCommand(
            List<Parameter> parameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        if (parameters.isEmpty()) {
            return Optional.of(List.of(sourceElement.fail("In a super command, at least one parameter must be defined")));
        }
        for (Parameter parameter : parameters) {
            if (tool.isOptionalish(parameter.returnType())) {
                return Optional.of(List.of(parameter.fail("In a super command, parameters cannot be optional")));
            }
        }
        return Optional.empty();
    }
}
