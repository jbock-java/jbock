package net.jbock.model;

import net.jbock.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * The runtime model of a class that is annotated with
 * {@link Command}.
 */
public final class CommandModel {

    private final String descriptionKey;
    private final List<String> descriptionLines;
    private final String programName;
    private final boolean superCommand;
    private final boolean unixClustering;
    private final List<Option> options;
    private final List<Parameter> parameters;

    private CommandModel(
            String descriptionKey,
            List<String> descriptionLines,
            String programName,
            boolean superCommand,
            boolean unixClustering,
            List<Option> options,
            List<Parameter> parameters) {
        this.descriptionKey = descriptionKey;
        this.descriptionLines = descriptionLines;
        this.programName = programName;
        this.superCommand = superCommand;
        this.unixClustering = unixClustering;
        this.options = options;
        this.parameters = parameters;
    }

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
        private final List<String> descriptionLines = new ArrayList<>();
        private String programName;
        private boolean superCommand;
        private boolean unixClustering;
        private final List<Option> options = new ArrayList<>();
        private final List<Parameter> parameters = new ArrayList<>();

        private Builder() {
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
         * @see Command#superCommand()
         * @param superCommand whether this is a supercommand
         * @return the builder instance
         */
        public Builder withSuperCommand(boolean superCommand) {
            this.superCommand = superCommand;
            return this;
        }

        /**
         * Sets the unix clustering property.
         *
         * @see Command#unixClustering()
         * @param unixClustering whether unix clustering is enabled
         * @return the builder instance
         */
        public Builder withUnixClustering(boolean unixClustering) {
            this.unixClustering = unixClustering;
            return this;
        }

        /**
         * Adds an option.
         *
         * @param option a named option
         * @return the builder instance
         */
        public Builder addOption(Option option) {
            this.options.add(option);
            return this;
        }

        /**
         * Adds a parameter.
         *
         * @param parameter a positional parameter
         * @return the builder instance
         */
        public Builder addParameter(Parameter parameter) {
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Creates the command model.
         *
         * @return command model
         */
        public CommandModel build() {
            return new CommandModel(descriptionKey, descriptionLines,
                    programName, superCommand,
                    unixClustering, options, parameters);
        }
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
     * empty, from the class javadoc of the command class.
     *
     * @return a list of lines, possibly empty
     */
    public List<String> descriptionLines() {
        return descriptionLines;
    }

    /**
     * Get the program name from the {@link Command#name()} attribute,
     * or, if that is empty, a default program name that is derived
     * from the class name of the annotated command class.
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
     * Returns the value of the {@link Command#superCommand()} attribute.
     *
     * @return {@code true} if the command is a "super command",
     *         {@code false} if it is a regular command
     */
    public boolean isSuperCommand() {
        return superCommand;
    }

    /**
     * Returns the value of the {@link Command#unixClustering()}}
     * attribute. Note, this may also return {@code false}
     * if unix clustering is impossible because
     * there are no unix-style mode flags.
     *
     * @return {@code true} if unix clustering is enabled
     */
    public boolean isUnixClustering() {
        return unixClustering;
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
        switch (itemType) {
            case PARAMETER:
                return parameters.get(index);
            case OPTION:
                return options.get(index);
            default:
                throw new AssertionError("all cases exhausted");
        }
    }
}
