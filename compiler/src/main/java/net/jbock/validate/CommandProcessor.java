package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.ItemsFactory;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
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
public class CommandProcessor {

    private final ItemsFactory itemsFactory;
    private final SourceElement sourceElement;
    private final OptionValidator optionValidator;
    private final ParameterValidator parameterValidator;
    private final VarargsParameterValidator varargsParameterValidator;

    CommandProcessor(
            ItemsFactory itemsFactory,
            SourceElement sourceElement,
            OptionValidator optionValidator,
            ParameterValidator parameterValidator,
            VarargsParameterValidator varargsParameterValidator) {
        this.itemsFactory = itemsFactory;
        this.sourceElement = sourceElement;
        this.optionValidator = optionValidator;
        this.parameterValidator = parameterValidator;
        this.varargsParameterValidator = varargsParameterValidator;
    }

    public Either<List<ValidationFailure>, CommandRepresentation> generate() {
        return itemsFactory.createItems()
                .filter(this::checkDuplicateDescriptionKeys)
                .flatMap(items -> {
                    Either<List<ValidationFailure>, List<Mapping<Parameter>>> a = parameterValidator.wrapPositionalParams(items);
                    Either<List<ValidationFailure>, Optional<Mapping<VarargsParameter>>> b = varargsParameterValidator.wrapVarargsParameters(items);
                    Either<List<ValidationFailure>, List<Mapping<Option>>> c = optionValidator.wrapOptions(items);
                    return a.flatMap(parameters ->
                            b.flatMap(varargsParameter ->
                                    c.map(options -> new CommandRepresentation(sourceElement, options, parameters, varargsParameter))));
                });
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
