package net.jbock.contrib;

import net.jbock.model.CommandModel;
import net.jbock.util.HasMessage;
import net.jbock.util.ParsingFailed;

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
    private final boolean ansi;
    private final Map<String, String> messages;

    private StandardErrorHandler(
            PrintStream out,
            int terminalWidth,
            boolean ansi,
            Map<String, String> messages) {
        this.out = out;
        this.terminalWidth = terminalWidth;
        this.ansi = ansi;
        this.messages = messages;
    }

    /**
     * Builder for {@link StandardErrorHandler}.
     */
    public static final class Builder {

        private PrintStream out = System.err;
        private int terminalWidth = 80;
        private boolean ansi = true;
        private Map<String, String> messages = Collections.emptyMap();

        private Builder() {
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
         * Set the value of the ansi attribute.
         *
         * @param ansi if ansi codes should be used
         *         when printing the usage documentation
         * @return the builder instance
         */
        public Builder withAnsi(boolean ansi) {
            this.ansi = ansi;
            return this;
        }

        /**
         * Create the error handler.
         *
         * @return an error handler
         */
        public StandardErrorHandler build() {
            return new StandardErrorHandler(out, terminalWidth, ansi, messages);
        }
    }

    /**
     * Creates an empty builder instance.
     *
     * @return empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public void printHelp(CommandModel model) {
        UsageDocumentation.builder(model)
                .withOutputStream(out)
                .withAnsi(ansi)
                .withMessages(messages)
                .withTerminalWidth(terminalWidth)
                .build().printUsageDocumentation();
        out.flush();
    }

    /**
     * This method does standard error handling like printing
     * error messages, or printing usage documentation.
     *
     * @param parsingFailed an object describing the error condition
     */
    public void handle(ParsingFailed parsingFailed) {
        CommandModel model = parsingFailed.commandModel();
        AnsiStyle ansiStyle = AnsiStyle.create(ansi);
        out.println(ansiStyle.red("ERROR:") + ' ' + ((HasMessage) parsingFailed).message());
        List<String> synopsis = Synopsis.create(model)
                .createSynopsis("Usage:");
        out.println(String.join(" ", synopsis));
        String helpCommand = model.programName() + " --help";
        out.println("Type " +
                ansiStyle.bold(helpCommand).orElseGet(() -> "'" + helpCommand + "'") +
                " for more information.");
        out.flush();
    }
}
