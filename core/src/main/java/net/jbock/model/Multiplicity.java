package net.jbock.model;

/**
 * Restricts the number of times an {@link Item} may
 * appear in the command line input.
 */
public enum Multiplicity {

    /**
     * The item is required.
     * It must appear exactly once in the input array.
     * Return any type that is <em>not</em> an {@link java.util.Optional Optional&lt;?&gt;}
     * or a {@link java.util.List List&lt;?&gt;} to declare a required item.
     *
     * <pre>{@code
     * multiplicity = 1
     * }</pre>
     */
    REQUIRED,

    /**
     * The item is optional.
     * It must either be absent, or appear exactly once
     * in the input array.
     * Return {@link java.util.Optional Optional&lt;?&gt;} from the
     * item method to declare an optional item.
     *
     * <pre>{@code
     * multiplicity = 0..1
     * }</pre>
     */
    OPTIONAL,

    /**
     * The item is repeatable.
     * It may appear any number of times in the input array.
     * Return {@link java.util.List List&lt;?&gt;} from the
     * item method to declare a repeatable item.
     *
     * <pre>{@code
     * multiplicity = 0..*
     * }</pre>
     */
    REPEATABLE,
}
