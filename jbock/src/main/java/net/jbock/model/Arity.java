package net.jbock.model;

/**
 * Arity is the expected number of arguments of a named option.
 */
public enum Arity {

    /**
     * The option does not take an argument.
     * It is indeed a mode flag.
     */
    NULLARY,

    /**
     * The option takes a single argument.
     */
    UNARY,
}
