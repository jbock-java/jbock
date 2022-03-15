package net.jbock.validate;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.MappingFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;

@ValidateScope
class SourceParameterValidator {

    private final MappingFinder mappingFinder;

    @Inject
    SourceParameterValidator(MappingFinder mappingFinder) {
        this.mappingFinder = mappingFinder;
    }

    Either<List<ValidationFailure>, ContextBuilder.Step2> wrapPositionalParams(
            ContextBuilder.Step1 step) {
        return validatePositions(step.positionalParameters())
                .flatMap(positionalParameters -> positionalParameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .filter(this::checkNoRequiredAfterOptional)
                .map(step::accept);
    }

    private Either<List<ValidationFailure>, List<AnnotatedParameter>> validatePositions(
            List<AnnotatedParameter> positionalParameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < positionalParameters.size(); i++) {
            AnnotatedParameter sourceParameter = positionalParameters.get(i);
            int index = sourceParameter.index();
            if (index != i) {
                failures.add(sourceParameter.fail("invalid position: expecting " + i + " but found " + index));
            }
        }
        return optionalList(failures)
                .<Either<List<ValidationFailure>, List<AnnotatedParameter>>>map(Either::left)
                .orElseGet(() -> right(positionalParameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(
            List<Mapping<AnnotatedParameter>> positionalParameters) {
        return positionalParameters.stream()
                .filter(Mapping::isOptional)
                .findFirst()
                .map(Mapping::sourceMethod)
                .flatMap(firstOptional -> positionalParameters.stream()
                        .filter(Mapping::isRequired)
                        .map(Mapping::sourceMethod)
                        .filter(sourceMethod -> sourceMethod.index()
                                > firstOptional.index())
                        .map(item -> item.fail("position of required parameter '" +
                                item.method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }
}
