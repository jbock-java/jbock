package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.VarargsParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.MappingFinder;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.toOptionalList;

final class VarargsParameterValidator {

    private final MappingFinder mappingFinder;
    private final SourceElement sourceElement;

    @Inject
    VarargsParameterValidator(
            MappingFinder mappingFinder,
            SourceElement sourceElement) {
        this.mappingFinder = mappingFinder;
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, Optional<Mapping<VarargsParameter>>> wrapVarargsParameters(
            Items items) {
        return validateDuplicateParametersAnnotation(items.varargsParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(parameters -> parameters.stream()
                        .map(mappingFinder::findMapping)
                        .collect(allFailures()))
                .map(mappings -> mappings.stream().findAny());
    }

    private Either<List<ValidationFailure>, List<VarargsParameter>> validateDuplicateParametersAnnotation(
            List<VarargsParameter> parameters) {
        return parameters.stream()
                .skip(1)
                .map(param -> param.fail("duplicate @" + net.jbock.VarargsParameter.class.getSimpleName() + " annotation"))
                .collect(toOptionalList())
                .<Either<List<ValidationFailure>, List<VarargsParameter>>>map(Either::left)
                .orElseGet(() -> right(parameters));
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateNoRepeatableParameterInSuperCommand(
            List<VarargsParameter> parameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        return parameters.stream()
                .map(param -> param.fail("@" + net.jbock.VarargsParameter.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(toOptionalList());
    }
}
