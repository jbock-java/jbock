package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A model of the annotated class.
 */
public final class CommandModel {

  private final String programName;
  private final boolean ansi;
  private final boolean helpEnabled;
  private final boolean superCommand;
  private final boolean atFileExpansion;
  private final List<Option> options;
  private final List<Parameter> parameters;

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String programName;
    private boolean ansi;
    private boolean helpEnabled;
    private boolean superCommand;
    private boolean atFileExpansion;
    private final List<Option> options = new ArrayList<>();
    private final List<Parameter> parameters = new ArrayList<>();

    public Builder withProgramName(String programName) {
      this.programName = programName;
      return this;
    }

    public Builder withAnsi(boolean ansi) {
      this.ansi = ansi;
      return this;
    }

    public Builder withHelpEnabled(boolean helpEnabled) {
      this.helpEnabled = helpEnabled;
      return this;
    }

    public Builder withSuperCommand(boolean superCommand) {
      this.superCommand = superCommand;
      return this;
    }

    public Builder withAtFileExpansion(boolean atFileExpansion) {
      this.atFileExpansion = atFileExpansion;
      return this;
    }

    public Builder addOption(Option option) {
      this.options.add(option);
      return this;
    }

    public Builder addParameter(Parameter parameter) {
      this.parameters.add(parameter);
      return this;
    }

    public CommandModel build() {
      return new CommandModel(programName, ansi, helpEnabled, superCommand, atFileExpansion,
          options, parameters);
    }
  }

  /**
   * Creates the model.
   * Public constructor that may be invoked from the generated code.
   *  @param programName program name, not blank
   * @param ansi whether to use ansi codes
   * @param helpEnabled whether the {@code --help} option is understood
   * @param superCommand whether this is a supercommand
   * @param atFileExpansion whether {@code @file}-expansion is enabled
   * @param options all options
   * @param parameters all parameters
   */
  CommandModel(
      String programName,
      boolean ansi,
      boolean helpEnabled,
      boolean superCommand,
      boolean atFileExpansion,
      List<Option> options,
      List<Parameter> parameters) {
    this.programName = programName;
    this.ansi = ansi;
    this.helpEnabled = helpEnabled;
    this.superCommand = superCommand;
    this.atFileExpansion = atFileExpansion;
    this.options = options;
    this.parameters = parameters;
  }

  public String programName() {
    return programName;
  }

  public boolean ansi() {
    return ansi;
  }

  public List<Option> options() {
    return options;
  }

  public List<Parameter> parameters() {
    return parameters;
  }

  public boolean helpEnabled() {
    return helpEnabled;
  }

  public boolean superCommand() {
    return superCommand;
  }

  public boolean atFileExpansion() {
    return atFileExpansion;
  }
}
