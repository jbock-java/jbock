package net.jbock.validate;

import net.jbock.common.ValidationFailure;
import net.jbock.context.ContextModule;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.util.Eithers.optionalList;

/**
 * List of all items, after validation.
 * Each item is wrapped in a {@link Mapped} instance.
 */
public class Items {

    private final List<Mapped<PositionalParameter>> positionalParams;
    private final List<Mapped<NamedOption>> namedOptions;

    Items(List<Mapped<PositionalParameter>> positionalParams,
          List<Mapped<NamedOption>> namedOptions) {
        this.positionalParams = positionalParams;
        this.namedOptions = namedOptions;
    }

    /* Left-Optional
     */
    Optional<List<ValidationFailure>> validatePositions() {
        List<Mapped<PositionalParameter>> sorted = positionalParams.stream()
                .sorted(Comparator.comparing(c -> c.item().position()))
                .collect(Collectors.toUnmodifiableList());
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Mapped<PositionalParameter> c = sorted.get(i);
            PositionalParameter p = c.item();
            if (p.position() != i) {
                String message = "Position " + p.position() + " is not available. Suggested position: " + i;
                failures.add(p.fail(message));
            }
        }
        return optionalList(failures);
    }

    /**
     * Creates the context module.
     *
     * @param sourceElement the command class
     * @return the context module
     */
    public ContextModule contextModule(SourceElement sourceElement) {
        return new ContextModule(sourceElement,
                positionalParams,
                namedOptions);
    }
}
