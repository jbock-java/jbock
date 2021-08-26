package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethods;
import net.jbock.annotated.AnnotatedMethodsFactory;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.jbock.util.Eithers.optionalList;

/**
 * This class is responsible for item validation.
 * If validation succeeds, a {@link ContextBuilder} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final AnnotatedMethodsFactory methodsFactory;
    private final SourceElement sourceElement;
    private final SourceOptionValidator optionValidator;
    private final SourceParameterValidator parameterValidator;
    private final SourceParametersValidator parametersValidator;

    @Inject
    CommandProcessor(
            AnnotatedMethodsFactory methodsFactory,
            SourceElement sourceElement,
            SourceOptionValidator optionValidator,
            SourceParameterValidator parameterValidator,
            SourceParametersValidator parametersValidator) {
        this.methodsFactory = methodsFactory;
        this.sourceElement = sourceElement;
        this.optionValidator = optionValidator;
        this.parameterValidator = parameterValidator;
        this.parametersValidator = parametersValidator;
    }

    public Either<List<ValidationFailure>, ContextBuilder> generate() {
        return methodsFactory.createAnnotatedMethods()
                .filter(this::checkDuplicateDescriptionKeys)
                .map(ContextBuilder::builder)
                .flatMap(parameterValidator::wrapPositionalParams)
                .flatMap(parametersValidator::wrapRepeatablePositionalParams)
                .flatMap(optionValidator::wrapOptions);
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(
            AnnotatedMethods methods) {
        List<ValidationFailure> failures = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        Stream.of(methods.namedOptions(),
                        methods.positionalParameters(),
                        methods.repeatablePositionalParameters())
                .flatMap(List::stream)
                .forEach(m -> m.descriptionKey().ifPresent(key -> {
                    if (!keys.add(key)) {
                        String message = "duplicate description key: " + key;
                        failures.add(m.fail(message));
                    }
                }));
        return optionalList(failures);
    }
}
