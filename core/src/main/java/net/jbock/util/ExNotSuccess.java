package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Superclass of internal exceptions that may be thrown and caught
 * in the generated code.
 */
public abstract class ExNotSuccess extends Exception {

    /**
     * Converts this exception to a non-exceptional failure object.
     *
     * @param model the command model
     * @return a failure object that is not an {@code Exception}
     */
    public abstract NotSuccess toError(CommandModel model);
}
