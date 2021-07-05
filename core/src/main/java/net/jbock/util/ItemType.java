package net.jbock.util;

import net.jbock.Option;
import net.jbock.Parameter;

/**
 * Item type as determined by its method's annotation.
 * An Item is either a <em>named option</em>
 * or a <em>positional parameter</em>.
 *
 * <table>
 *   <caption></caption>
 *   <thead><tr><td><b>Annotation</b></td><td><b>Item type</b></td></tr></thead>
 *   <tr><td>{@code @Parameter}</td><td>{@link ItemType#PARAMETER PARAMETER}</td></tr>
 *   <tr><td>{@code @Parameters}</td><td>{@link ItemType#PARAMETER PARAMETER}</td></tr>
 *   <tr><td>{@code @Option}</td><td>{@link ItemType#OPTION OPTION}</td></tr>
 * </table>
 */
public enum ItemType {

    /**
     * Named option, or mode flag.
     * The runtime model for this item is {@link Option}.
     */
    OPTION,

    /**
     * Positional parameter.
     * The runtime model for this item is {@link Parameter}.
     */
    PARAMETER,
}
