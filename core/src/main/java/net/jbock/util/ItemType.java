package net.jbock.util;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;

/**
 * The item type is determined by the annotation on the item method.
 */
public enum ItemType {

    /**
     * Item type for a named option, or a mode flag.
     * The item's method is annotated with {@link Option}.
     */
    OPTION,

    /**
     * Item type for a positional parameter, possibly a
     * repeatable positional parameter.
     * The item's method is annotated with {@link Parameter}
     * or {@link Parameters}.
     *
     * @see net.jbock.model.Multiplicity#REPEATABLE
     */
    PARAMETER,
}
