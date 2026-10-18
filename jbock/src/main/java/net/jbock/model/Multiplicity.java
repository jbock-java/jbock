package net.jbock.model;

/**
 * A restriction on the number of times an option or parameter can
 * appear in the command line input.
 */
public enum Multiplicity {

    /**
     * The option or parameter is required.
     * It must appear exactly once in the input.
     */
    REQUIRED,

    /**
     * The option or parameter is optional.
     */
    OPTIONAL,

    /**
     * No restriction; the option or parameter
     * may appear any number of times in the input.
     */
    REPEATABLE,
}
