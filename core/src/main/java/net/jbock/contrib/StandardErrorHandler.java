package net.jbock.contrib;

import net.jbock.model.CommandModel;
import net.jbock.util.HasMessage;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is responsible for standard error handling,
 * like printing messages and shutting down the JVM.
 */
public final class StandardErrorHandler {

  private final NotSuccess notSuccess;

  private final PrintStream out;
  private final int terminalWidth;
  private final Map<String, String> messages;
  private final Supplier<RuntimeException> exitHook;

  private StandardErrorHandler(
      NotSuccess notSuccess,
      PrintStream out,
      int terminalWidth,
      Map<String, String> messages,
      Supplier<RuntimeException> exitHook) {
    this.notSuccess = notSuccess;
    this.out = out;
    this.terminalWidth = terminalWidth;
    this.messages = messages;
    this.exitHook = exitHook;
  }

  /**
   * Builder for {@link StandardErrorHandler}.
   */
  public static final class Builder {

    private final NotSuccess notSuccess;

    private PrintStream out = System.err;
    private int terminalWidth = 80;
    private Map<String, String> messages = Collections.emptyMap();
    private Supplier<RuntimeException> exitHook;

    private Builder(
        NotSuccess notSuccess,
        Supplier<RuntimeException> exitHook) {
      this.notSuccess = notSuccess;
      this.exitHook = exitHook;
    }

    /**
     * Sets the output stream for printing help messages
     * or usage documentation.
     * The default value is {@code System.err}.
     *
     * @param out the output stream
     * @return the builder instance
     */
    public Builder withOutputStream(PrintStream out) {
      this.out = out;
      return this;
    }

    /**
     * Sets the terminal width. The default value is
     * {@code 80} characters.
     * Use this method to pass the actual terminal width,
     * for improved readability of the usage documentation,
     * if a library like JLine is available.
     *
     * @param width terminal width in characters
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
     * @param map a map of strings
     * @return the builder instance
     */
    public Builder withMessages(Map<String, String> map) {
      this.messages = map;
      return this;
    }

    /**
     * Change or elide the JVM shutdown command.
     * The default behaviour is to invoke {@link System#exit(int)}
     * with an exit code of {@code 1} if an error occurred,
     * or {@code 0} if the {@code --help} option was passed.
     *
     * @param exitHook the exit logic
     * @return the builder instance
     */
    public Builder withExitHook(Supplier<RuntimeException> exitHook) {
      this.exitHook = exitHook;
      return this;
    }

    /**
     * Create the error handler.
     *
     * @return an error handler
     */
    public StandardErrorHandler build() {
      return new StandardErrorHandler(notSuccess, out, terminalWidth, messages, exitHook);
    }
  }

  /**
   * Create an empty builder instance.
   * Public method that may be invoked from the generated code.
   *
   * @param notSuccess failure object
   * @return empty builder
   */
  public static Builder builder(NotSuccess notSuccess) {
    Supplier<RuntimeException> exitHook = () -> {
      System.exit(notSuccess instanceof HelpRequested ? 0 : 1);
      return new RuntimeException();
    };
    return new Builder(notSuccess, exitHook);
  }

  /**
   * <p>Public method that may be invoked from the generated code.</p>
   *
   * <h2><b>CAUTION: Invoking this method may shut down the JVM.</b></h2>
   *
   * <p>This method also does standard error handling like printing of
   *    error messages, or printing standard usage documentation for
   *    the provided {@link CommandModel}.</p>
   *
   * @return a runtime exception
   */
  public RuntimeException handle() {
    CommandModel model = notSuccess.commandModel();
    AnsiStyle ansi = AnsiStyle.create(model);
    if (notSuccess instanceof HelpRequested) {
      UsageDocumentation.builder(model)
          .withOutputStream(out)
          .withMessages(messages)
          .withTerminalWidth(terminalWidth)
          .build().printUsageDocumentation();
      out.flush();
      return exitHook.get();
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
    return exitHook.get();
  }
}
