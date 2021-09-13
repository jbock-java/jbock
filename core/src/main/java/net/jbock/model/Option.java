package net.jbock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is part of the command model.
 * It represents a named option or a mode flag.
 *
 * @see net.jbock.Option
 * @see CommandModel
 */
public final class Option extends Item {

    private final List<String> names;
    private final Arity arity;

    Option(
            String paramLabel,
            String descriptionKey,
            List<String> description,
            List<String> names,
            Multiplicity multiplicity,
            Arity arity) {
        super(paramLabel, descriptionKey, description, multiplicity);
        if (names.isEmpty()) {
            throw new IllegalArgumentException("paramLabel may not be empty");
        }
        this.names = names;
        this.arity = arity;
    }

    /**
     * Creates a builder instance.
     *
     * @return empty builder
     */
    public static Builder nullary() {
        return new Builder(Multiplicity.OPTIONAL, Arity.NULLARY);
    }

    /**
     * Creates a builder instance.
     *
     * @return empty builder
     */
    public static Builder unary(Multiplicity multiplicity) {
        return new Builder(multiplicity, Arity.UNARY);
    }

    /**
     * Builder for an {@code Option}.
     */
    public static final class Builder {

        private String paramLabel;
        private String descriptionKey = "";
        private final List<String> description = new ArrayList<>();
        private List<String> names;
        private final Multiplicity multiplicity;
        private final Arity arity;

        private Builder(Multiplicity multiplicity, Arity arity) {
            this.multiplicity = multiplicity;
            this.arity = arity;
        }

        /**
         * Sets the param label.
         *
         * @see net.jbock.Option#paramLabel()
         * @param paramLabel a non-empty string
         * @return the builder instance
         */
        public Builder withParamLabel(String paramLabel) {
            this.paramLabel = paramLabel;
            return this;
        }

        /**
         * Sets the description key.
         *
         * @see net.jbock.Option#descriptionKey()
         * @param descriptionKey a string, possibly empty
         * @return the builder instance
         */
        public Builder withDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        /**
         * Adds a line of description text.
         *
         * @see net.jbock.Option#description()
         * @param descriptionLine a string, possibly empty
         * @return the builder instance
         */
        public Builder addDescriptionLine(String descriptionLine) {
            this.description.add(descriptionLine);
            return this;
        }

        /**
         * Sets the list of option names.
         *
         * @see net.jbock.Option#names()
         * @param names the option names
         * @return the builder instance
         */
        public Builder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        /**
         * Creates the option model.
         *
         * @return option model
         */
        public Option build() {
            return new Option(
                    paramLabel,
                    descriptionKey,
                    description,
                    names,
                    multiplicity,
                    arity);
        }
    }

    @Override
    public String namesOverview() {
        String sample = String.join(", ", names);
        switch (arity) {
            case NULLARY:
                return sample;
            case UNARY:
                return sample + ' ' + paramLabel();
            default:
                throw new AssertionError("all cases exhausted");
        }
    }

    @Override
    public String namesOverviewError() {
        return itemType().name().toLowerCase(Locale.US) + " " +
                paramLabel() + " (" + String.join(", ", names) + ")";
    }

    @Override
    public ItemType itemType() {
        return ItemType.OPTION;
    }

    /**
     * Returns a list of the option names, sorted by length and then alphabetically.
     *
     * @see net.jbock.Option#names()
     * @return a nonempty list of option names
     */
    public List<String> names() {
        return names;
    }

    /**
     * Returns the option arity.
     *
     * @return the option arity
     */
    public Arity arity() {
        return arity;
    }
}
