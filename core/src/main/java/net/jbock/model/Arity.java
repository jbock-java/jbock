package net.jbock.model;

/**
 * The number of arguments an {@link Option} takes,
 * per occurrence of the option name in the input array.
 *
 * <p>Note: A unary option can have the multiplicity
 * {@link Multiplicity#REPEATABLE REPEATABLE}.
 */
public enum Arity {

    /**
     * This option does not take an argument.
     * It is, in fact, a mode flag.
     */
    NULLARY,

    /**
     * This option takes a single argument.
     */
    UNARY,
}
