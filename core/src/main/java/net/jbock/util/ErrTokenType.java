package net.jbock.util;

import net.jbock.Command;

/**
 * Specific types of various possible errors in the command line syntax.
 */
public enum ErrTokenType {

    /**
     * Error condition:
     *
     * <p>An unescaped token that starts with a dash character was found,
     * which does not match any of the known option names.
     */
    INVALID_OPTION,

    /**
     * Error condition:
     *
     * <p>Invalid unix flag, or cluster of unix options.
     *
     * @see Command#unixClustering()
     */
    INVALID_UNIX_GROUP,

    /**
     * Error condition:
     *
     * <p>An additional positional parameter was found, after
     * all known parameters have already been read.
     */
    EXCESS_PARAM,

    /**
     * Error condition:
     *
     * <p>A non-repeatable option was found to be present more
     * than once in the input array.
     *
     * @see net.jbock.model.Multiplicity
     */
    OPTION_REPETITION,

    /**
     * Error condition:
     *
     * <p>Missing argument of a unary option.
     *
     * @see net.jbock.model.Arity
     */
    MISSING_ARGUMENT,
}
