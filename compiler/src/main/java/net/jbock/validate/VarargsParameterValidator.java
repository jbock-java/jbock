package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.VarargsParameter;
import net.jbock.common.TypeTool;
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
    private final TypeTool tool;

    @Inject
    VarargsParameterValidator(
            MappingFinder mappingFinder,
            SourceElement sourceElement,
            TypeTool tool) {
        this.mappingFinder = mappingFinder;
        this.sourceElement = sourceElement;
        this.tool = tool;
    }

    Either<List<ValidationFailure>, Optional<Mapping<VarargsParameter>>> wrapVarargsParameters(
            Items items) {
        return validateDuplicateParametersAnnotation(items.varargsParameters())
                .filter(this::validateVarargsParameterInSuperCommand)
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
    private Optional<List<ValidationFailure>> validateVarargsParameterInSuperCommand(
            List<VarargsParameter> parameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        if (parameters.isEmpty()) {
            return Optional.of(List.of(sourceElement.fail("At least one @VarargsParameter must be defined" +
                    " in a @SuperCommand")));
        }
        if (!tool.isListOfString(parameters.get(0).returnType())) {
            return Optional.of(List.of(sourceElement.fail("The @VarargsParameter" +
                    " in a @SuperCommand must return List<String>")));
        }
        return Optional.empty();
    }
}
