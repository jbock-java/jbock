package net.jbock.usage;

import net.jbock.model.CommandModel;
import net.jbock.util.HasMessage;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for standard error handling,
 * like printing messages and shutting down the JVM.
 */
public final class StandardErrorHandler {

  private final PrintStream out;
  private final int terminalWidth;
  private final Map<String, String> messages;

  private StandardErrorHandler(
      PrintStream out,
      int terminalWidth,
      Map<String, String> messages) {
    this.out = out;
    this.terminalWidth = terminalWidth;
    this.messages = messages;
  }

  public static class Builder {

    private PrintStream out = System.err;
    private int terminalWidth = 80;
    private Map<String, String> messages = Collections.emptyMap();

    private Builder() {
    }

    /**
     * Sets the output stream for printing help messages
     * or usage documentation.
     * The default value is {@code System.err}.
     *
     * @return the builder instance
     */
    public Builder withOutputStream(PrintStream err) {
      this.out = err;
      return this;
    }

    /**
     * Sets the terminal width. The default value is
     * {@code 80} characters.
     * Use this method to pass the actual terminal width,
     * for improved readability of the usage documentation,
     * if a library like JLine is available.
     *
     * @return the builder instance
     */
    public Builder withTerminalWidth(int width) {
      this.terminalWidth = width == 0 ? this.terminalWidth : width;
      return this;
    }

    /**
     * Set the message map that contains description keys.
     * The default value is an empty map.
     * This map is used to pass the internationalization
     * value of a {@code descriptionKey}.
     *
     * @return the builder instance
     */
    public Builder withMessages(Map<String, String> map) {
      this.messages = map;
      return this;
    }

    public StandardErrorHandler build() {
      return new StandardErrorHandler(out, terminalWidth, messages);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * <p>Public method that may be invoked from the generated code.</p>
   *
   * <p></p>
   * <h2><tt>CAUTION:</tt></h2>
   * <p><b><tt>Invoking this method will shut down the JVM.</tt></b></p>
   * <p></p>
   *
   * <p>This method also does standard error handling like printing of
   *    error messages, or printing standard usage documentation for
   *    the provided {@link CommandModel}.</p>
   *
   * @param notSuccess failure object
   * @return a runtime exception
   */
  public RuntimeException handle(NotSuccess notSuccess) {
    CommandModel model = notSuccess.commandModel();
    AnsiStyle ansi = AnsiStyle.create(model);
    if (notSuccess instanceof HelpRequested) {
      UsageDocumentation.builder(model)
          .withOutputStream(out)
          .withMessages(messages)
          .withTerminalWidth(terminalWidth)
          .build().printUsageDocumentation();
      System.exit(0);
      return new RuntimeException();
    }
    out.println(ansi.red("ERROR:") + ' ' + ((HasMessage) notSuccess).message());
    if (model.helpEnabled()) {
      List<String> synopsis = Synopsis.create(model)
          .createSynopsis("Usage:");
      out.println(String.join(" ", synopsis));
      String helpCommand = model.programName() + " --help";
      out.println("Type " +
          ansi.bold(helpCommand).orElseGet(() -> "'" + helpCommand + "'") +
          " for more information.");
    } else {
      UsageDocumentation.builder(model)
          .build().printUsageDocumentation();
    }
    out.flush();
    System.exit(1);
    return new RuntimeException();
  }
}
