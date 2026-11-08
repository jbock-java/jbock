package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Instances of this interface represent runtime errors that can happen
 * during command line parsing.
 */
public sealed interface ParsingFailed permits ErrConvert, ErrMissingItem, ErrToken {

    /**
     * Returns a static model of the annotated command class.
     *
     * @return command model
     */
    CommandModel commandModel();

    /**
     * Returns the error message that describes the runtime error.
     *
     * @return the error message
     */
    String message();
}
