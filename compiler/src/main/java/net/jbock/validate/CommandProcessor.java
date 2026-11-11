package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.ItemsFactory;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.jbock.util.Either.right;
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
                .filterMap(items -> {
                    Either<List<ValidationFailure>, List<Parameter>> p = parameterValidator.wrapPositionalParams(items);
                    if (p.isLeft()) {
                        return p.pinLeft();
                    }
                    Either<List<ValidationFailure>, List<Option>> o = optionValidator.wrapOptions(items);
                    if (o.isLeft()) {
                        return o.pinLeft();
                    }
                    List<Option> options = o.getRightOrElseThrow();
                    List<Parameter> parameters = p.getRightOrElseThrow();
                    return right(new CommandRepresentation(sourceElement, options, parameters));
                });
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(
            Items items) {
        Set<String> keys = HashSet.newHashSet(items.size() + 1);
        sourceElement.descriptionKey().ifPresent(keys::add);
        List<ValidationFailure> failures = items.allItems().stream()
                .flatMap(m -> m.descriptionKey().map(key -> {
                    if (keys.add(key)) {
                        return Stream.<ValidationFailure>empty();
                    } else {
                        return Stream.of(m.fail("duplicate description key"));
                    }
                }).orElseGet(Stream::empty)).toList();
        return optionalList(failures);
    }
}
