package net.jbock.validate;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.VarargsParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.MappingFinder;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.toOptionalList;

@ValidateScope
class SourceParametersValidator {

    private final MappingFinder mappingFinder;
    private final SourceElement sourceElement;

    @Inject
    SourceParametersValidator(
            MappingFinder mappingFinder,
            SourceElement sourceElement) {
        this.mappingFinder = mappingFinder;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, ContextBuilder.Step3> wrapRepeatablePositionalParams(
            ContextBuilder.Step2 step) {
        return validateDuplicateParametersAnnotation(step.repeatablePositionalParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(repeatablePositionalParameters -> repeatablePositionalParameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .map(step::accept);
    }

    private Either<List<ValidationFailure>, List<AnnotatedParameters>> validateDuplicateParametersAnnotation(
            List<AnnotatedParameters> repeatablePositionalParameters) {
        return repeatablePositionalParameters.stream()
                .skip(1)
                .map(param -> param.fail("duplicate @" + VarargsParameter.class.getSimpleName() + " annotation"))
                .collect(toOptionalList())
                .<Either<List<ValidationFailure>, List<AnnotatedParameters>>>map(Either::left)
                .orElseGet(() -> right(repeatablePositionalParameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateNoRepeatableParameterInSuperCommand(
            List<AnnotatedParameters> repeatablePositionalParameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        return repeatablePositionalParameters.stream()
                .map(param -> param.fail("@" + VarargsParameter.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(toOptionalList());
    }
}
