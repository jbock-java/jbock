package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.ItemsFactory;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
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
@Inject
public final class CommandProcessor {

    private final ItemsFactory itemsFactory;
    private final SourceElement sourceElement;
    private final OptionValidator optionValidator;
    private final ParameterValidator parameterValidator;

    CommandProcessor(
            ItemsFactory itemsFactory,
            SourceElement sourceElement,
            OptionValidator optionValidator,
            ParameterValidator parameterValidator) {
        this.itemsFactory = itemsFactory;
        this.sourceElement = sourceElement;
        this.optionValidator = optionValidator;
        this.parameterValidator = parameterValidator;
    }

    public Either<List<ValidationFailure>, CommandRepresentation> generate() {
        return itemsFactory.createItems()
                .filter(this::checkDuplicateDescriptionKeys)
                .flatMap(items -> {
                    Either<List<ValidationFailure>, List<Mapping<Parameter>>> p = parameterValidator.wrapPositionalParams(items);
                    Either<List<ValidationFailure>, List<Mapping<Option>>> o = optionValidator.wrapOptions(items);
                    return p.flatMap(parameters ->
                            o.map(options -> new CommandRepresentation(sourceElement, options, parameters)));
                });
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(
            Items items) {
        List<ValidationFailure> failures = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        Stream.of(items.options(),
                        items.parameters())
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
