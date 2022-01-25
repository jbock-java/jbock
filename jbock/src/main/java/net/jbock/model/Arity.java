package net.jbock.model;

/**
 * Arity is the expected number of arguments of a unary named option,
 * per occurrence of the option name in the command line input.
 *
 * <p>Note: A unary option can still take multiple arguments:
 *          Return {@code List<?>} from the item method
 *          to declare a repeatable option.
 *
 * @see Multiplicity
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
