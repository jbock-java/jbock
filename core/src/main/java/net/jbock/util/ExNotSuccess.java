package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Superclass of internal exceptions that may be thrown and caught
 * in the generated code.
 */
public abstract class ExNotSuccess extends Exception {

    /**
     * Convert this exception to a failure object by removing
     * the internal stacktrace and adding the command model.
     * Public method that may be invoked from the generated code.
     *
     * @param model the command model
     * @return failure object
     */
    public abstract NotSuccess toError(CommandModel model);
}
