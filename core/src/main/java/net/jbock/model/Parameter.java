package net.jbock.model;

import net.jbock.Parameters;
import net.jbock.util.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is part of the command model.
 * It represents a positional parameter.
 *
 * @see net.jbock.Parameter
 * @see net.jbock.Parameters
 * @see CommandModel
 */
public final class Parameter extends Item {

    Parameter(
            String paramLabel,
            String descriptionKey,
            List<String> description,
            Multiplicity multiplicity) {
        super(paramLabel, descriptionKey, description, multiplicity);
    }

    /**
     * Creates a builder instance.
     *
     * @return an empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for a {@code Parameter}.
     */
    public static final class Builder {

        private String paramLabel;
        private String descriptionKey = "";
        private final List<String> description = new ArrayList<>();
        private Multiplicity multiplicity = Multiplicity.REQUIRED;

        private Builder() {
        }

        /**
         * Sets the param label.
         *
         * @see Parameter#paramLabel()
         * @see Parameters#paramLabel()
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
         * @see Parameter#descriptionKey()
         * @see Parameters#descriptionKey()
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
         * @see Parameter#description()
         * @see Parameters#description()
         * @param descriptionLine a string, possibly empty
         * @return the builder instance
         */
        public Builder addDescriptionLine(String descriptionLine) {
            this.description.add(descriptionLine);
            return this;
        }

        /**
         * Sets the multiplicity of this parameter.
         *
         * @param multiplicity the multiplicity
         * @return the builder instance
         */
        public Builder withMultiplicity(Multiplicity multiplicity) {
            this.multiplicity = multiplicity;
            return this;
        }

        /**
         * Creates the model for this parameter.
         *
         * @return parameter model
         */
        public Parameter build() {
            return new Parameter(paramLabel, descriptionKey, description, multiplicity);
        }
    }

    @Override
    public String namesOverview() {
        return paramLabel();
    }

    @Override
    public String namesOverviewError() {
        return itemType().name().toLowerCase(Locale.US) + " " + paramLabel();
    }

    @Override
    public ItemType itemType() {
        return ItemType.PARAMETER;
    }
}
