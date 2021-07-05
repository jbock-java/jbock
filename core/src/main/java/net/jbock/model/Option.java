package net.jbock.model;

import net.jbock.util.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runtime model of a named option or a mode flag.
 *
 * @see net.jbock.Option
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
        this.names = names;
        this.arity = arity;
    }

    /**
     * Creates a builder instance.
     * Public method that may be invoked from the generated code.
     *
     * @return empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for an {@link Option}.
     */
    public static final class Builder {

        private String paramLabel;
        private String descriptionKey = "";
        private final List<String> description = new ArrayList<>();
        private List<String> names;
        private Multiplicity multiplicity = Multiplicity.OPTIONAL;
        private Arity arity = Arity.UNARY;

        private Builder() {
        }

        /**
         * Set the param label.
         * Public method that may be invoked from the generated code.
         *
         * @param paramLabel a non-empty string
         * @return the builder instance
         */
        public Builder withParamLabel(String paramLabel) {
            this.paramLabel = paramLabel;
            return this;
        }

        /**
         * Set the description key.
         * Public method that may be invoked from the generated code.
         *
         * @param descriptionKey a string, possibly empty
         * @return the builder instance
         */
        public Builder withDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        /**
         * Add a line of description text.
         * Public method that may be invoked from the generated code.
         *
         * @param descriptionLine a string, possibly empty
         * @return the builder instance
         */
        public Builder addDescriptionLine(String descriptionLine) {
            this.description.add(descriptionLine);
            return this;
        }

        /**
         * Set the list of all option names.
         * Public method that may be invoked from the generated code.
         *
         * @param names the option names
         * @return the builder instance
         */
        public Builder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        /**
         * Set the multiplicity of this option.
         * Public method that may be invoked from the generated code.
         *
         * @param multiplicity the multiplicity
         * @return the builder instance
         */
        public Builder withMultiplicity(Multiplicity multiplicity) {
            this.multiplicity = multiplicity;
            return this;
        }

        /**
         * Marks this option as a nullary mode flag.
         * Public method that may be invoked from the generated code.
         *
         * @return the builder instance
         */
        public Builder withModeFlag() {
            this.multiplicity = Multiplicity.OPTIONAL;
            this.arity = Arity.NULLARY;
            return this;
        }

        /**
         * Create the option model.
         * Public method that may be invoked from the generated code.
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
     * List of option names, sorted by length and then alphabetically.
     *
     * @return option names
     */
    public List<String> names() {
        return names;
    }

    /**
     * The arity of this option.
     *
     * @return arity
     */
    public Arity arity() {
        return arity;
    }
}
