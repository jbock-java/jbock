package net.jbock.validate;

import net.jbock.context.ContextModule;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import java.util.List;

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
