package net.jbock.model;

import net.jbock.Command;
import net.jbock.util.ItemType;

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
  private final boolean ansi;
  private final boolean helpEnabled;
  private final boolean superCommand;
  private final boolean atFileExpansion;
  private final List<Option> options;
  private final List<Parameter> parameters;

  private CommandModel(
      String descriptionKey,
      List<String> descriptionLines,
      String programName,
      boolean ansi,
      boolean helpEnabled,
      boolean superCommand,
      boolean atFileExpansion,
      List<Option> options,
      List<Parameter> parameters) {
    this.descriptionKey = descriptionKey;
    this.descriptionLines = descriptionLines;
    this.programName = programName;
    this.ansi = ansi;
    this.helpEnabled = helpEnabled;
    this.superCommand = superCommand;
    this.atFileExpansion = atFileExpansion;
    this.options = options;
    this.parameters = parameters;
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
   * Builder for {@link CommandModel}.
   */
  public static final class Builder {

    private String descriptionKey = "";
    private final List<String> descriptionLines = new ArrayList<>();
    private String programName;
    private boolean ansi = true;
    private boolean helpEnabled = true;
    private boolean superCommand;
    private boolean atFileExpansion = true;
    private final List<Option> options = new ArrayList<>();
    private final List<Parameter> parameters = new ArrayList<>();

    /**
     * Set the description key.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionKey a key, possibly blank
     * @return the builder instance
     */
    public Builder withDescriptionKey(String descriptionKey) {
      this.descriptionKey = descriptionKey;
      return this;
    }

    /**
     * Add a description line.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionLine a line
     * @return the builder instance
     */
    public Builder addDescriptionLine(String descriptionLine) {
      this.descriptionLines.add(descriptionLine);
      return this;
    }

    /**
     * Set program name.
     * Public method that may be invoked from the generated code.
     *
     * @param programName program name, not blank
     * @return the builder instance
     */
    public Builder withProgramName(String programName) {
      this.programName = programName;
      return this;
    }

    /**
     * Set ansi flag.
     * Public method that may be invoked from the generated code.
     *
     * @param ansi whether to use ansi codes
     * @return the builder instance
     */
    public Builder withAnsi(boolean ansi) {
      this.ansi = ansi;
      return this;
    }

    /**
     * Set help enabled property.
     * Public method that may be invoked from the generated code.
     *
     * @param helpEnabled whether the {@code --help} option is understood
     * @return the builder instance
     */
    public Builder withHelpEnabled(boolean helpEnabled) {
      this.helpEnabled = helpEnabled;
      return this;
    }

    /**
     * Set the supercommand property.
     * Public method that may be invoked from the generated code.
     *
     * @param superCommand whether this is a supercommand
     * @return the builder instance
     */
    public Builder withSuperCommand(boolean superCommand) {
      this.superCommand = superCommand;
      return this;
    }

    /**
     * Set at file expansion property.
     * Public method that may be invoked from the generated code.
     *
     * @param atFileExpansion whether {@code @file}-expansion is enabled
     * @return the builder instance
     */
    public Builder withAtFileExpansion(boolean atFileExpansion) {
      this.atFileExpansion = atFileExpansion;
      return this;
    }

    /**
     * Add an option.
     * Public method that may be invoked from the generated code.
     *
     * @param option a named option
     * @return the builder instance
     */
    public Builder addOption(Option option) {
      this.options.add(option);
      return this;
    }

    /**
     * Add a parameter.
     * Public method that may be invoked from the generated code.
     *
     * @param parameter a positional parameter
     * @return the builder instance
     */
    public Builder addParameter(Parameter parameter) {
      this.parameters.add(parameter);
      return this;
    }

    /**
     * Create the command model.
     * Public method that may be invoked from the generated code.
     *
     * @return command model
     */
    public CommandModel build() {
      return new CommandModel(descriptionKey, descriptionLines, programName, ansi, helpEnabled, superCommand, atFileExpansion,
          options, parameters);
    }
  }

  /**
   * Get the description key, possibly an empty string.
   *
   * @return non-null string
   */
  public String descriptionKey() {
    return descriptionKey;
  }

  /**
   * Get the description that is present directly on the annotated class,
   * either as a description attribute, or in the form of javadoc.
   *
   * @return a list of lines, possibly empty
   */
  public List<String> descriptionLines() {
    return descriptionLines;
  }

  /**
   * Get the program name from the {@link Command#name()} attribute,
   * or, if none is set, a default program name that is derived
   * from the class name of the annotated command class.
   *
   * @return the program name, a nonempty string
   */
  public String programName() {
    return programName;
  }

  /**
   * Get the value of the {@link Command#ansi()} attribute.
   *
   * @return {@code true} if the parser can use ansi colors
   *         when printing the usage documentation
   */
  public boolean ansi() {
    return ansi;
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
   * Get the list of all positional parameters.
   *
   * @return positional parameters
   */
  public List<Parameter> parameters() {
    return parameters;
  }

  /**
   * Get the value of the {@link Command#helpEnabled()} attribute.
   *
   * @return {@code true} if the generated parser supports
   *         the {@code --help} option
   */
  public boolean helpEnabled() {
    return helpEnabled;
  }

  /**
   * Check if the {@link Command} annotation
   * is present on the command class.
   *
   * @return {@code true} if the command is a SuperCommand,
   *         {@code false} if it is a regular Command
   *
   */
  public boolean superCommand() {
    return superCommand;
  }

  /**
   * Get the value of the {@link Command#atFileExpansion()}
   * attribute.
   *
   * @return {@code true} if {@code @file} expansion is enabled
   */
  public boolean atFileExpansion() {
    return atFileExpansion;
  }

  /**
   * Get item by name and index.
   *
   * @param itemType item type
   * @param index the index
   * @return the item
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public final Item getItem(ItemType itemType, int index) {
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
