package net.jbock.util;

import net.jbock.model.CommandModel;

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

    private final String file;

    AtFileError(String file) {
        this.file = file;
    }

    final String file() {
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
