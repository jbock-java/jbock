package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * An instance of this class signals failure of a file reading operation.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 */
final class AtFileReadError extends AtFileError {

    private final Exception exception;

    AtFileReadError(Exception exception, Path file) {
        super(file);
        this.exception = exception;
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
        return new ErrAtFile(model, file(), exception.getClass().getSimpleName()
                + ": " + exception.getMessage());
    }
}
