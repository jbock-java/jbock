package net.jbock.model;

import net.jbock.ColorMode;
import net.jbock.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * The runtime model of a class that is annotated with
 * {@link Command}.
 */
public record CommandModel(
        String descriptionKey,
        List<String> descriptionLines,
        String programName,
        boolean superCommand,
        ColorMode colorMode,
        List<Option> options,
        List<Parameter> parameters) {

    /**
     * Creates a builder instance.
     *
     * @return empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link CommandModel}.
     */
    public static final class Builder {

        private String descriptionKey = "";
        private List<String> descriptionLines;
        private String programName;
        private boolean superCommand;
        private ColorMode colorMode;
        private List<Option> options;
        private List<Parameter> parameters;

        private Builder() {
        }

        public Builder withOptions(List<Option> options) {
            this.options = options;
            return this;
        }

        public Builder withParameters(List<Parameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withDescriptionLines(List<String> descriptionLines) {
            this.descriptionLines = descriptionLines;
            return this;
        }

        /**
         * Sets the description key.
         *
         * @see Command#descriptionKey()
         * @param descriptionKey a key, possibly blank
         * @return the builder instance
         */
        public Builder withDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        /**
         * Adds a description line.
         *
         * @see Command#description()
         * @param descriptionLine a line
         * @return the builder instance
         */
        public Builder addDescriptionLine(String descriptionLine) {
            if (descriptionLines == null) {
                descriptionLines = new ArrayList<>();
            }
            this.descriptionLines.add(descriptionLine);
            return this;
        }

        /**
         * Sets program name.
         *
         * @see Command#name()
         * @param programName program name, not blank
         * @return the builder instance
         */
        public Builder withProgramName(String programName) {
            this.programName = programName;
            return this;
        }

        /**
         * Sets the supercommand property.
         *
         * @param superCommand whether this is a supercommand
         * @return the builder instance
         */
        public Builder withSuperCommand(boolean superCommand) {
            this.superCommand = superCommand;
            return this;
        }

        /**
         * Sets the noColor property to {@code true}.
         */
        public Builder withColorMode(ColorMode colorMode) {
            this.colorMode = colorMode;
            return this;
        }

        /**
         * Adds an option.
         *
         * @param option a named option
         * @return the builder instance
         */
        public Builder addOption(Option option) {
            if (options == null) {
                options = new ArrayList<>();
            }
            this.options.add(option);
            return this;
        }

        public Builder addUnary(Multiplicity multiplicity, List<String> names, String label) {
            return addOption(Option.unary(multiplicity).withParamLabel(label).withNames(names).build());
        }

        public Builder addNullary(List<String> names) {
            return addOption(Option.nullary().withNames(names).build());
        }

        /**
         * Adds a parameter.
         *
         * @param parameter a positional parameter
         * @return the builder instance
         */
        public Builder addParameter(Parameter parameter) {
            if (parameters == null) {
                parameters = new ArrayList<>();
            }
            this.parameters.add(parameter);
            return this;
        }

        public Builder addParameter(Multiplicity multiplicity, String label) {
            return addParameter(Parameter.builder(multiplicity).withParamLabel(label).build());
        }

        /**
         * Creates the command model.
         *
         * @return command model
         */
        public CommandModel build() {
            return new CommandModel(
                    descriptionKey,
                    descriptionLines == null ? List.of() : descriptionLines,
                    programName,
                    superCommand,
                    colorMode,
                    options == null ? List.of() : options,
                    parameters == null ? List.of() : parameters);
        }
    }

    public Builder toBuilder() {
        return builder()
                .withDescriptionKey(descriptionKey)
                .withDescriptionLines(descriptionLines)
                .withProgramName(programName)
                .withSuperCommand(superCommand)
                .withColorMode(colorMode)
                .withOptions(options)
                .withParameters(parameters);
    }

    /**
     * Returns the description key from the {@link Command#descriptionKey()} attribute,
     * possibly an empty string.
     *
     * @see Command#descriptionKey()
     * @return description key
     */
    public String descriptionKey() {
        return descriptionKey;
    }

    /**
     * Returns the command description,
     * either from the {@link Command#description()} attribute, or, if that is
     * empty, from the class javadoc of the command interface.
     *
     * @return a list of lines, possibly empty
     */
    public List<String> descriptionLines() {
        return descriptionLines;
    }

    /**
     * Get the program name from the {@link Command#name()} attribute,
     * or, if that is empty, a default program name that is derived
     * from the class name of the command interface.
     *
     * @see Command#name()
     * @return the program name, a nonempty string
     */
    public String programName() {
        return programName;
    }

    /**
     * Get the list of all named options, including mode flags.
     *
     * @return named options
     */
    public List<Option> options() {
        return options;
    }

    /**
     * Get the list of all positional parameters, in the correct order.
     * If a repeatable positional parameter exists, it will be the last
     * entry in this list.
     *
     * @return list of all positional parameters in the correct order
     */
    public List<Parameter> parameters() {
        return parameters;
    }

    /**
     * Returns {@code true} if {@code superCommand} is {@code true}.
     *
     * @return {@code true} if the command is a "super command",
     *         {@code false} if it is a regular command
     */
    public boolean isSuperCommand() {
        return superCommand;
    }

    /**
     * Get item by name and index.
     *
     * @param itemType item type
     * @param index the index
     * @return the item
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Item getItem(ItemType itemType, int index) {
        return switch (itemType) {
            case PARAMETER -> parameters.get(index);
            case OPTION -> options.get(index);
        };
    }
}
