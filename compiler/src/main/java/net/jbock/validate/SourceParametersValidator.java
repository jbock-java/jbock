package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.Parameters;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.MappingFinder;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toOptionalList;
import static io.jbock.util.Eithers.toValidListAll;

@ValidateScope
public class SourceParametersValidator {

    private final MappingFinder converterFinder;
    private final SourceElement sourceElement;

    @Inject
    SourceParametersValidator(
            MappingFinder converterFinder,
            SourceElement sourceElement) {
        this.converterFinder = converterFinder;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, ContextBuilder.Step3> wrapRepeatablePositionalParams(
            ContextBuilder.Step2 step) {
        return validateDuplicateParametersAnnotation(step.repeatablePositionalParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(repeatablePositionalParameters -> repeatablePositionalParameters.stream()
                        .map(converterFinder::findMapping)
                        .collect(toValidListAll()))
                .map(step::accept);
    }

    private Either<List<ValidationFailure>, List<AnnotatedParameters>> validateDuplicateParametersAnnotation(
            List<AnnotatedParameters> repeatablePositionalParameters) {
        return repeatablePositionalParameters.stream()
                .skip(1)
                .map(param -> param.fail("duplicate @" + Parameters.class.getSimpleName() + " annotation"))
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
                .map(param -> param.fail("@" + Parameters.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(toOptionalList());
    }
}
