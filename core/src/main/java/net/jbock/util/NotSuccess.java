package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * An instance of this class represents a parsing
 * result that's different from &quot;success&quot;.
 * This will be returned from the generated {@code parse(String[])}
 * method, if the parsing is not successful, or if the user has
 * passed the {@code --help} option on the command line.
 *
 * <p>There are a fixed number of subclasses:
 *
 * <ul>
 *   <li>{@link ErrAtFile}</li>
 *   <li>{@link ErrConvert}</li>
 *   <li>{@link ErrMissingItem}</li>
 *   <li>{@link ErrToken}</li>
 *   <li>{@link HelpRequested}</li>
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
