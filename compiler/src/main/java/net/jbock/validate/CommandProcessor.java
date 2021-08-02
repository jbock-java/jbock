package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Eithers.optionalList;
import static net.jbock.common.Constants.concat;

/**
 * This class is responsible for item validation.
 * If validation succeeds, a {@link ContextBuilder} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final MethodsFactory methodsFactory;
    private final SourceElement sourceElement;
    private final SourceOptionValidator optionValidator;
    private final SourceParameterValidator parameterValidator;
    private final SourceParametersValidator parametersValidator;

    @Inject
    CommandProcessor(
            MethodsFactory methodsFactory,
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
        return methodsFactory.findAbstractMethods()
                .filter(this::checkDuplicateDescriptionKeys)
                .map(ContextBuilder::builder)
                .flatMap(parameterValidator::wrapPositionalParams)
                .flatMap(parametersValidator::wrapRepeatablePositionalParams)
                .flatMap(optionValidator::wrapOptions);
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(AbstractMethods methods) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<? extends AnnotatedMethod> items =
                concat(concat(methods.namedOptions(), methods.positionalParameters()), methods.repeatablePositionalParameters());
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        for (AnnotatedMethod m : items) {
            m.descriptionKey().ifPresent(key -> {
                if (!keys.add(key)) {
                    String message = "duplicate description key: " + key;
                    failures.add(m.fail(message));
                }
            });
        }
        return optionalList(failures);
    }
}
