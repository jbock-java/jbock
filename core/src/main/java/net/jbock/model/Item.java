package net.jbock.model;

import net.jbock.util.ItemType;

import java.util.List;

/**
 * This class is part of the command model.
 * It represents an abstract method in the command class.
 *
 * <p>There are a fixed number of subclasses:
 * <ul>
 *     <li>{@link Option}
 *     <li>{@link Parameter}
 * </ul>
 *
 * @see net.jbock.Command
 */
public abstract class Item {

    private final String paramLabel;
    private final String descriptionKey;
    private final List<String> description;
    private final Multiplicity multiplicity;

    Item(
            String paramLabel,
            String descriptionKey,
            List<String> description,
            Multiplicity multiplicity) {
        if (paramLabel.isEmpty()) {
            throw new IllegalArgumentException("paramLabel may not be empty");
        }
        this.paramLabel = paramLabel;
        this.descriptionKey = descriptionKey;
        this.description = description;
        this.multiplicity = multiplicity;
    }

    /**
     * An overview of all names of this item, including a
     * sample argument if this is a unary option.
     * Used in the LHS of the usage documentation.
     *
     * @see net.jbock.Option#names()
     * @return a non-empty string
     */
    public abstract String namesOverview();

    /**
     * An alternative form of {@link #namesOverview()} that is
     * commonly used to identify the item in error messages.
     *
     * @see net.jbock.Option#names()
     * @return a non-empty string
     */
    public abstract String namesOverviewError();

    /**
     * The item as shown in the synopsis, if this is a
     * positional parameter or a required option.
     * For unary options, this is also the name of the
     * sample argument that's shown in the full usage
     * documentation.
     *
     * @see net.jbock.Option#paramLabel()
     * @see net.jbock.Parameter#paramLabel()
     * @see net.jbock.Parameters#paramLabel()
     * @return param label, a non-empty string
     */
    public final String paramLabel() {
        return paramLabel;
    }

    /**
     * Get the description that is present directly on the annotated method,
     * either as a description attribute, or in the form of javadoc.
     *
     * @see net.jbock.Option#description()
     * @see net.jbock.Parameter#description()
     * @see net.jbock.Parameters#description()
     * @return a list of lines, possibly empty
     */
    public final List<String> description() {
        return description;
    }

    /**
     * A string, possibly empty.
     *
     * @see net.jbock.Option#descriptionKey()
     * @see net.jbock.Parameter#descriptionKey()
     * @see net.jbock.Parameters#descriptionKey()
     * @return description key
     */
    public final String descriptionKey() {
        return descriptionKey;
    }

    /**
     * The multiplicity of this item.
     *
     * @return item multiplicity
     */
    public final Multiplicity multiplicity() {
        return multiplicity;
    }

    /**
     * Returns the item type.
     *
     * @return this item's {@code ItemType}
     */
    public abstract ItemType itemType();
}
