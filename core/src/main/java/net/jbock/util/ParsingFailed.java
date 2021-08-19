package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Superclass of failure objects that may be
 * returned from the generated {@code parse} method.
 *
 * <p>There are a fixed number of subclasses:
 *
 * <ul>
 *   <li>{@link ErrAtFile}
 *   <li>{@link ErrConvert}
 *   <li>{@link ErrMissingItem}
 *   <li>{@link ErrToken}
 * </ul>
 */
public abstract class ParsingFailed {

    private final CommandModel commandModel;

    ParsingFailed(CommandModel commandModel) {
        this.commandModel = commandModel;
    }

    /**
     * Returns a complete model of the annotated class, to generate
     * usage documentation or failure messages.
     *
     * @return command model
     */
    public CommandModel commandModel() {
        return commandModel;
    }

    /**
     * Returns an error message that describes the error.
     *
     * @return the error message
     */
    public abstract String message();
}
