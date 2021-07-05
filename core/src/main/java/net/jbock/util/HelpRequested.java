package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * This parsing result indicates that the user has passed the
 * {@code --help} option on the command line.
 */
public final class HelpRequested extends NotSuccess {

    /**
     * Public constructor.
     *
     * @param commandModel command model
     */
    public HelpRequested(CommandModel commandModel) {
        super(commandModel);
    }
}
