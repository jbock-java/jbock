package net.jbock.usage;

import net.jbock.model.CommandModel;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParsingError;

import java.io.PrintStream;
import java.util.List;

public class ErrorHandler {

  private final PrintStream err;

  private ErrorHandler(PrintStream err) {
    this.err = err;
  }

  public static class Builder {

    private PrintStream err = System.err;

    private Builder() {
    }

    /**
     * Set the output stream for printing.
     * The default value is {@code System.err}.
     *
     * @return the builder instance
     */
    public Builder withErrorStream(PrintStream err) {
      this.err = err;
      return this;
    }

    public ErrorHandler build() {
      return new ErrorHandler(err);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public RuntimeException handle(NotSuccess notSuccess) {
    CommandModel model = notSuccess.commandModel();
    AnsiStyle ansi = AnsiStyle.create(model);
    if (notSuccess instanceof HelpRequested) {
      UsageDocumentation.builder(model)
          .build().printUsageDocumentation();
      System.exit(0);
      return new RuntimeException();
    }
    err.println(ansi.red("ERROR:") + ' ' + ((ParsingError) notSuccess).message());
    if (model.helpEnabled()) {
      List<String> synopsis = Synopsis.create(model)
          .createSynopsis("Usage:");
      err.println(String.join(" ", synopsis));
      String helpCommand = model.programName() + " --help";
      err.println("Type " +
          ansi.bold(helpCommand).orElseGet(() -> "'" + helpCommand + "'") +
          " for more information.");
    } else {
      UsageDocumentation.builder(model)
          .build().printUsageDocumentation();
    }
    err.flush();
    System.exit(1);
    return new RuntimeException();
  }
}
