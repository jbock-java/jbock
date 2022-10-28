package net.jbock.validate;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.Items;
import net.jbock.annotated.ItemsFactory;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.jbock.util.Eithers.optionalList;

/**
 * This class is responsible for item validation.
 * If validation succeeds, a {@link CommandRepresentation} is created.
 */
@ValidateScope
public class CommandProcessor {

    private final ItemsFactory itemsFactory;
    private final SourceElement sourceElement;
    private final OptionValidator optionValidator;
    private final ParameterValidator parameterValidator;
    private final VarargsParameterValidator parametersValidator;

    @Inject
    CommandProcessor(
            ItemsFactory itemsFactory,
            SourceElement sourceElement,
            OptionValidator optionValidator,
            ParameterValidator parameterValidator,
            VarargsParameterValidator parametersValidator) {
        this.itemsFactory = itemsFactory;
        this.sourceElement = sourceElement;
        this.optionValidator = optionValidator;
        this.parameterValidator = parameterValidator;
        this.parametersValidator = parametersValidator;
    }

    public Either<List<ValidationFailure>, CommandRepresentation> generate() {
        return itemsFactory.createItems()
                .filter(this::checkDuplicateDescriptionKeys)
                .map(ContextBuilder::builder)
                .map(builder -> builder.accept(sourceElement))
                .flatMap(parameterValidator::wrapPositionalParams)
                .flatMap(parametersValidator::wrapRepeatablePositionalParams)
                .flatMap(optionValidator::wrapOptions)
                .map(ContextBuilder::build);
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(
            Items items) {
        List<ValidationFailure> failures = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        Stream.of(items.namedOptions(),
                        items.positionalParameters(),
                        items.varargsParameters())
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
