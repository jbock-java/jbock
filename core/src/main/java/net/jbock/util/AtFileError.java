package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * An instance of this class represents failure of {@code @file} expansion.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 *
 * <p>There are a fixed number of subclasses:
 *
 * <ul>
 *   <li>{@link AtFileSyntaxError}
 *   <li>{@link AtFileReadError}
 * </ul>
 */
public abstract class AtFileError {

    private final Path file;

    AtFileError(Path file) {
        this.file = file;
    }

    final Path file() {
        return file;
    }

    /**
     * Adds the command model to create a {@code NotSuccess} instance.
     *
     * @param model command model
     * @return a non-exceptional failure object
     */
    public abstract ParsingFailed addModel(CommandModel model);
}
