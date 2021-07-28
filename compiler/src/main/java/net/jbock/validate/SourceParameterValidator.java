package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConverterFinder;
import net.jbock.convert.Mapped;
import net.jbock.source.SourceParameter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;
import static io.jbock.util.Eithers.toValidListAll;

@ValidateScope
public class SourceParameterValidator {

    private final ConverterFinder converterFinder;

    @Inject
    SourceParameterValidator(ConverterFinder converterFinder) {
        this.converterFinder = converterFinder;
    }

    Either<List<ValidationFailure>, ContextBuilder.Step2> wrapPositionalParams(
            ContextBuilder.Step1 step) {
        return validatePositions(step.positionalParameters())
                .flatMap(positionalParameters -> positionalParameters.stream()
                        .map(sourceMethod -> converterFinder.findConverter(sourceMethod)
                                .mapLeft(sourceMethod::fail))
                        .collect(toValidListAll()))
                .filter(this::checkNoRequiredAfterOptional)
                .map(step::accept);
    }

    private Either<List<ValidationFailure>, List<SourceParameter>> validatePositions(
            List<SourceParameter> allPositionalParameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < allPositionalParameters.size(); i++) {
            SourceParameter item = allPositionalParameters.get(i);
            int index = item.annotatedMethod().index();
            if (index != i) {
                failures.add(item.fail("invalid position: expecting " + i + " but found " + index));
            }
        }
        return optionalList(failures)
                .<Either<List<ValidationFailure>, List<SourceParameter>>>map(Either::left)
                .orElseGet(() -> right(allPositionalParameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(
            List<Mapped<AnnotatedParameter>> allPositionalParameters) {
        return allPositionalParameters.stream()
                .filter(Mapped::isOptional)
                .findFirst()
                .map(Mapped::item)
                .flatMap(firstOptional -> allPositionalParameters.stream()
                        .filter(Mapped::isRequired)
                        .map(Mapped::item)
                        .filter(item -> item.annotatedMethod().index() > firstOptional.annotatedMethod().index())
                        .map(item -> item.fail("position of required parameter '" +
                                item.annotatedMethod().method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.annotatedMethod().method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }
}
