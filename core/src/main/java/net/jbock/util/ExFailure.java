package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Superclass of exceptions that may be used internally
 * in the generated code. These are checked exceptions, to
 * ensure none of them are thrown from the generated
 * parse method.
 *
 * <p>This class is internal API and should not be used
 * in client code. It may be removed without warning in future
 * releases.
 */
public abstract class ExFailure extends Exception {

    /**
     * Converts this exception to a non-exceptional failure object.
     *
     * @param model the command model
     * @return a failure object that is not an {@code Exception}
     */
    public abstract ParsingFailed toError(CommandModel model);
}
