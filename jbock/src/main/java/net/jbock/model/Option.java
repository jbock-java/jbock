package net.jbock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is part of the command model.
 * It represents a named option.
 */
public record Option(
        String paramLabel,
        String descriptionKey,
        List<String> description,
        List<String> names,
        Multiplicity multiplicity,
        boolean nullary) implements Item {

    /**
     * Creates a builder for a mode flag.
     *
     * @return builder instance
     */
    public static Builder nullaryBuilder() {
        return new Builder(Multiplicity.OPTIONAL, true);
    }

    /**
     * Creates a builder for a unary option.
     *
     * @param multiplicity multiplicity
     * @return builder instance
     */
    public static Builder unaryBuilder(Multiplicity multiplicity) {
        return new Builder(multiplicity, false);
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
        private final boolean nullary;

        private Builder(Multiplicity multiplicity, boolean nullary) {
            this.multiplicity = multiplicity;
            this.nullary = nullary;
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
        public Builder withNames(String... names) {
            if (names.length == 0) {
                throw new AssertionError("a named option needs at least one name");
            }
            this.names = List.of(names);
            return this;
        }

        /**
         * Creates the option model.
         *
         * @return option model
         */
        public Option build() {
            if (names == null || names.isEmpty()) {
                throw new AssertionError("a named option needs at least one name");
            }
            return new Option(
                    paramLabel,
                    descriptionKey,
                    description,
                    names,
                    multiplicity,
                    nullary);
        }
    }

    @Override
    public String namesOverview() {
        String sample = String.join(", ", names);
        if (nullary) {
            return sample;
        } else {
            return sample + ' ' + paramLabel();
        }
    }

    @Override
    public String namesOverviewError() {
        return itemType().name().toLowerCase(Locale.ROOT) + " " +
                paramLabel() + " (" + String.join(", ", names) + ")";
    }

    @Override
    public ItemType itemType() {
        return ItemType.OPTION;
    }

    /**
     * Returns a non-empty list of the option names, sorted by length and then alphabetically.
     *
     * @return a non-empty list
     */
    @Override
    public List<String> names() {
        return names;
    }
}
