package net.jbock.model;

import net.jbock.usage.Option;
import net.jbock.usage.Parameter;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UsageContext {

  private final String programName;
  private final boolean ansi;
  private final List<Option> options;
  private final List<Parameter> parameters;

  /**
   * @param programName program name
   * @param ansi whether to use ansi codes
   * @param options all options
   * @param parameters all parameters
   */
  public UsageContext(
      String programName,
      boolean ansi,
      List<Option> options,
      List<Parameter> parameters) {
    this.programName = programName;
    this.ansi = ansi;
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
}
