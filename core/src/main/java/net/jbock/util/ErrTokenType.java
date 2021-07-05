package net.jbock.util;

import net.jbock.Command;

/**
 * Specific types of various possible errors in the command line syntax.
 */
public enum ErrTokenType {

    /**
     * An unescaped token starting with a dash was found that
     * did not match any of the known option names.
     */
    INVALID_OPTION,

    /**
     * Invalid unix flag, or cluster of unix options.
     *
     * @see Command#unixClustering()
     */
    INVALID_UNIX_GROUP,

    /**
     * An additional positional parameter was found, after
     * all known parameters have already been read.
     */
    EXCESS_PARAM,

    /**
     * A non-repeatable option was found to be present more
     * than once in the input array.
     *
     * @see net.jbock.model.Multiplicity#REPEATABLE
     */
    OPTION_REPETITION,

    /**
     * Missing argument of a unary option.
     *
     * @see net.jbock.model.Arity#UNARY
     */
    MISSING_ARGUMENT,
}
