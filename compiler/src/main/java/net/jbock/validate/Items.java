package net.jbock.validate;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.context.ContextModule;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;

import java.util.List;

/**
 * List of all items, after validation.
 * Each item is wrapped in a {@link Mapped} instance.
 */
public class Items {

    private final List<Mapped<AnnotatedParameter>> positionalParams;
    private final List<Mapped<AnnotatedParameters>> repeatablePositionalParameters;
    private final List<Mapped<AnnotatedOption>> namedOptions;

    Items(List<Mapped<AnnotatedParameter>> positionalParams,
          List<Mapped<AnnotatedParameters>> repeatablePositionalParameters,
          List<Mapped<AnnotatedOption>> namedOptions) {
        this.positionalParams = positionalParams;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
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
                repeatablePositionalParameters,
                namedOptions);
    }
}
