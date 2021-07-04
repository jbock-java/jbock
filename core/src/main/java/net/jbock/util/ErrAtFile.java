package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates that reading options from the {@code @file} was not successful.
 */
public final class ErrAtFile extends NotSuccess implements HasMessage {

    private final String message;
    private final String atFile;

    /**
     * Public constructor that may be invoked from the generated code.
     *
     * @param commandModel the command model
     * @param message error message
     * @param atFile path of the at file
     */
    public ErrAtFile(
            CommandModel commandModel,
            String atFile,
            String message) {
        super(commandModel);
        this.message = message;
        this.atFile = atFile;
    }

    @Override
    public String message() {
        return "while reading " + atFile + ": " + message;
    }
}
