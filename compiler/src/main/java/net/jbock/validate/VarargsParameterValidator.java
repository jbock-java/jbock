package net.jbock.validate;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.VarargsParameter;
import net.jbock.annotated.AnnotatedVarargsParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.MappingFinder;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.toOptionalList;

@ValidateScope
class VarargsParameterValidator {

    private final MappingFinder mappingFinder;
    private final SourceElement sourceElement;

    @Inject
    VarargsParameterValidator(
            MappingFinder mappingFinder,
            SourceElement sourceElement) {
        this.mappingFinder = mappingFinder;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, ContextBuilder.Step3> wrapRepeatablePositionalParams(
            ContextBuilder.Step2 step) {
        return validateDuplicateParametersAnnotation(step.varargsParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(parameters -> parameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .map(step::accept);
    }

    private Either<List<ValidationFailure>, List<AnnotatedVarargsParameter>> validateDuplicateParametersAnnotation(
            List<AnnotatedVarargsParameter> parameters) {
        return parameters.stream()
                .skip(1)
                .map(param -> param.fail("duplicate @" + VarargsParameter.class.getSimpleName() + " annotation"))
                .collect(toOptionalList())
                .<Either<List<ValidationFailure>, List<AnnotatedVarargsParameter>>>map(Either::left)
                .orElseGet(() -> right(parameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateNoRepeatableParameterInSuperCommand(
            List<AnnotatedVarargsParameter> parameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        return parameters.stream()
                .map(param -> param.fail("@" + VarargsParameter.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(toOptionalList());
    }
}
