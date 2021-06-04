package net.jbock.usage;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UsageContext {

  private final String programName;
  private final boolean ansi;
  private final List<Option> options;
  private final List<Parameter> parameters;

  private PrintStream err = System.err;
  private int terminalWidth = 80;
  private Map<String, String> messages = Collections.emptyMap();

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

  public UsageContext withTerminalWidth(int width) {
    this.terminalWidth = width == 0 ? this.terminalWidth : width;
    return this;
  }

  public UsageContext withMessages(Map<String, String> map) {
    this.messages = map;
    return this;
  }

  public UsageContext withErrorStream(PrintStream err) {
    this.err = err;
    return this;
  }

  /**
   * Creates {@link UsageDocumentation}.
   *
   * @return usage documentation
   */
  public UsageDocumentation buildDocumentation() {
    Usage usage = buildUsage();
    return new UsageDocumentation(err, terminalWidth, messages, options, parameters,
        usage, new AnsiStyle(ansi), maxWidth(options), maxWidth(parameters));
  }

  /**
   * Creates {@link Usage}.
   *
   * @return usage
   */
  public Usage buildUsage() {
    return new Usage(options, parameters, programName);
  }

  private static int maxWidth(List<? extends Item> items) {
    return items.stream()
        .map(Item::name)
        .mapToInt(String::length)
        .max()
        .orElse(0);
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
