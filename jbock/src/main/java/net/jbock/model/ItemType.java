package net.jbock.model;

import net.jbock.Option;
import net.jbock.Parameter;

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
     * Item type for a positional parameter.
     * The item's method is annotated with {@link Parameter}.
     */
    PARAMETER,
}
