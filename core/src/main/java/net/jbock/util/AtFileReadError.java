package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * An instance of this class signals an IO error.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 */
final class AtFileReadError extends AtFileError {

    private final Exception exception;

    AtFileReadError(Exception exception, Path file) {
        super(file);
        this.exception = exception;
    }

    @Override
    public NotSuccess addModel(CommandModel model) {
        return new ErrAtFile(model, file(), exception.getClass().getSimpleName()
                + ": " + exception.getMessage());
    }
}
