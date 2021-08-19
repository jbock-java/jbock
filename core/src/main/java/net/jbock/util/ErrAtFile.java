package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * Indicates that reading options
 * from the {@code @-file} was unsuccessful.
 */
public final class ErrAtFile extends ParsingFailed {

    private final String message;
    private final Path atFile;

    /**
     * Public constructor.
     *
     * @param commandModel the command model
     * @param message error message
     * @param atFile path of the at file
     */
    public ErrAtFile(
            CommandModel commandModel,
            Path atFile,
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
