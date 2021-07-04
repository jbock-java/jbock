package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * An instance of this class signals failure of a file reading operation.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 *
 * <p>There are a fixed number of subclasses:</p>
 *
 * <ul>
 *   <li>{@link AtFileSyntaxError}</li>
 *   <li>{@link AtFileReadError}</li>
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
     * Add the command model.
     * Public method that may be invoked from the generated code.
     *
     * @param model command model
     * @return a failure object
     */
    public abstract NotSuccess addModel(CommandModel model);
}
