package net.jbock.contrib;

import net.jbock.ColorMode;
import net.jbock.model.CommandModel;
import net.jbock.util.ParsingFailed;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * A convenience class that performs standard error handling,
 * like printing error messages and printing the usage documentation.
 */
public record StandardErrorHandler(
        PrintStream out,
        int terminalWidth,
        ColorMode colorMode,
        Map<String, String> messages) {

    /**
     * Builder for {@link StandardErrorHandler}.
     */
    public static final class Builder {

        private PrintStream out = System.err;
        private int terminalWidth = 80;
        private ColorMode colorMode = ColorMode.AUTO;
        private Map<String, String> messages = Map.of();

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
         * Sets the value of the noColor attribute to {@code true}.
         */
        public Builder withColorMode(ColorMode colorMode) {
            this.colorMode = colorMode;
            return this;
        }

        /**
         * Creates the error handler.
         *
         * @return an error handler
         */
        public StandardErrorHandler build() {
            return new StandardErrorHandler(out, terminalWidth, colorMode, messages);
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

    /**
     * Prints the usage documentation.
     *
     * @param model command model
     */
    public void printUsageDocumentation(CommandModel model) {
        UsageDocumentation.builder(model)
                .withOutputStream(out)
                .withColorMode(colorMode)
                .withMessages(messages)
                .withTerminalWidth(terminalWidth)
                .build()
                .printUsageDocumentation();
        out.flush();
    }

    /**
     * Prints an error message.
     *
     * @param failure an object describing the error condition
     */
    public void printErrorMessage(ParsingFailed failure) {
        CommandModel model = failure.commandModel();
        AnsiStyle ansiStyle = AnsiStyle.create(colorMode);
        out.println(ansiStyle.red("ERROR:") + ' ' + failure.message());
        List<String> synopsis = Synopsis.create(model)
                .createSynopsis("Usage:");
        out.println(String.join(" ", synopsis));
        out.println("Type " +
                boldHelpCommand(model, ansiStyle) +
                " for more information.");
        out.flush();
    }

    private String boldHelpCommand(CommandModel model, AnsiStyle ansiStyle) {
        String helpCommand = model.programName() + " --help";
        if (ansiStyle.noColor()) {
            return "'" + helpCommand + "'";
        } else {
            return ansiStyle.bold(helpCommand);
        }
    }
}
