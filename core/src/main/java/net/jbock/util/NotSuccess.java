package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Superclass of non-exceptional failure objects that may be
 * produced as a result of invoking the generated parser.
 * Each of the subclasses represents a parsing
 * result that's different from "success".
 *
 * <p>A Left-Either containing an instance of this class will be returned
 * from the generated {@code parse(ParseRequest)} method, if the parsing is unsuccessful,
 * or if {@link ParseRequest#isHelpRequested()} returns {@code true}.
 *
 * <p>There are a fixed number of subclasses:
 *
 * <ul>
 *   <li>{@link ErrAtFile}
 *   <li>{@link ErrConvert}
 *   <li>{@link ErrMissingItem}
 *   <li>{@link ErrToken}
 *   <li>{@link HelpRequested}
 * </ul>
 */
public abstract class NotSuccess {

    private final CommandModel commandModel;

    NotSuccess(CommandModel commandModel) {
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
}
