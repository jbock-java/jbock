package net.jbock.util;

/**
 * Specific types of various possible errors in the command line syntax.
 */
public enum ErrTokenType {

    /**
     * An unescaped token that starts with a dash character was found,
     * which does not match any of the known option names.
     */
    INVALID_OPTION,

    /**
     * Invalid unix flag, or cluster of unix options.
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
     * @see net.jbock.model.Multiplicity
     */
    OPTION_REPETITION,

    /**
     * Missing argument of a unary option.
     *
     * @see net.jbock.model.Arity
     */
    MISSING_ARGUMENT,
}
