package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Represents a syntax error in the at-file.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 */
final class AtFileSyntaxError extends AtFileError {

    private final int line;
    private final String message;

    AtFileSyntaxError(String file, int line, String message) {
        super(file);
        this.line = line;
        this.message = message;
    }

    /**
     * Add the command model.
     * Public method that may be invoked from the generated code.
     *
     * @param model command model
     * @return a failure object
     */
    @Override
    public NotSuccess addModel(CommandModel model) {
        return new ErrAtFile(model, file(), "at line " + line + ": " + message);
    }
}
