package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A model of the annotated class.
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

    private String descriptionKey;
    private final List<String> descriptionLines = new ArrayList<>();
    private String programName;
    private boolean ansi;
    private boolean helpEnabled;
    private boolean superCommand;
    private boolean atFileExpansion;
    private final List<Option> options = new ArrayList<>();
    private final List<Parameter> parameters = new ArrayList<>();

    /**
     * Set description key.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionKey a key, possibly blank
     */
    public Builder withDescriptionKey(String descriptionKey) {
      this.descriptionKey = descriptionKey;
      return this;
    }

    /**
     * Add description line.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionLine a line
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

  public String descriptionKey() {
    return descriptionKey;
  }

  public List<String> descriptionLines() {
    return descriptionLines;
  }

  /**
   * Get the program name.
   *
   * @return the program name
   */
  public String programName() {
    return programName;
  }

  /**
   * Check if ansi codes are enabled.
   *
   * @return {@code true} if the parser can use ansi colors
   *         when printing the usage documentation
   */
  public boolean ansi() {
    return ansi;
  }

  /**
   * Get the list of all named options, including modal flags.
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
   * Check if the command supports the help option.
   *
   * @return {@code true} if the generated parser supports
   *         the {@code --help} option
   */
  public boolean helpEnabled() {
    return helpEnabled;
  }

  /**
   * Check for the supercommand annotation.
   *
   * @return {@code true} if the command is a {@link net.jbock.SuperCommand},
   *         {@code false} if it is a {@link net.jbock.Command}
   *
   */
  public boolean superCommand() {
    return superCommand;
  }

  /**
   * Check for {@code @file} expansion.
   *
   * @return {@code true} if {@code @file} expansion is enabled
   */
  public boolean atFileExpansion() {
    return atFileExpansion;
  }
}
