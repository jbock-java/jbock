package net.jbock.util;

import io.jbock.util.Either;
import net.jbock.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * <p>Allow reading some or all command line options from
 * a configuration file, if at file reading is enabled via {@link Command#atFileExpansion()}
 * and the first command line token starts with a {@code "@"} character.
 *
 * <p>The following escape sequences are recognized:
 *
 * <table>
 *   <caption>Escape sequences</caption>
 *   <thead><tr><td><b>Code</b></td><td><b>Meaning</b></td></tr></thead>
 *   <tr><td>{@code \\}</td><td>backslash</td></tr>
 *   <tr><td>{@code \n}</td><td>newline</td></tr>
 *   <tr><td>{@code \r}</td><td>carriage return</td></tr>
 *   <tr><td>{@code \t}</td><td>horizontal tab</td></tr>
 * </table>
 *
 * <p>Escape sequences are not interpreted inside single quotes.
 * <p>Single quotes are not interpreted inside double quotes.
 * <p>An unpaired backslash at the end of a line instructs the reader
 * to continue reading the current token on the next line.
 * <p>Note: Even if set to {@code true},
 * and the user wants to pass exactly one positional parameter
 * that starts with an {@code @} character,
 * they can still prevent the {@code @file} expansion,
 * by passing {@code --} as the first token.
 * <p>Note: additional tokens in the input array, after the
 * initial {@code @file} token, are allowed and will be appended to
 * the result of reading the {@code @file}.
 */
public final class AtFileReader {

    /**
     * Read the contents of the {@code @file} into a list of Strings.
     * This method may be invoked from the generated code,
     * unless {@link Command#atFileExpansion()} is {@code false}.
     *
     * @param args command line input, must contain at least one token,
     *             and the first token must be two characters at least
     * @return the options in the file, or an error report
     */
    public Either<? extends AtFileError, List<String>> read(String[] args) {
        if (args.length == 0
                || args[0].length() < 2
                || !args[0].startsWith("@")) {
            return right(List.of(args));
        }
        String fileName = args[0].substring(1);
        try {
            Path path = Paths.get(fileName);
            List<String> lines = Files.readAllLines(path);
            return readAtLines(lines)
                    .mapLeft(r -> new AtFileSyntaxError(fileName, r.number, r.lineResult.message())) // TODO
                    .map(atLines -> {
                        List<String> atLinesWithRest = new ArrayList<>(atLines);
                        atLinesWithRest.addAll(List.of(args).subList(1, args.length));
                        return atLinesWithRest;
                    });
        } catch (Exception e) {
            return left(new AtFileReadError(e, fileName));
        }
    }

    Either<NumberedLineResult, List<String>> readAtLines(List<String> lines) {
        int[] counter = {1};
        Iterator<NumberedLine> it = lines.stream()
                .filter(line -> !line.isEmpty())
                .map(line -> new NumberedLine(counter[0]++, line))
                .iterator();
        List<Either<NumberedLineResult, String>> tokens = new ArrayList<>(lines.size());
        while (it.hasNext()) {
            tokens.add(readTokenFromAtFile(it));
        }
        return tokens.stream().collect(Either.toValidList());
    }

    private Either<NumberedLineResult, String> readTokenFromAtFile(Iterator<NumberedLine> it) {
        StringBuilder sb = new StringBuilder();
        NumberedLine current;
        LineResult esc;
        do {
            current = it.next();
            esc = readLine(current.line, sb);
        } while (esc == LineResult.CONTINUE && it.hasNext());
        if (esc.isError()) {
            return left(new NumberedLineResult(current.number, esc));
        }
        if (esc == LineResult.CONTINUE && !it.hasNext()) {
            return left(new NumberedLineResult(current.number, LineResult.NO_NEXT_LINE));
        }
        return right(sb.toString());
    }

    private LineResult readLine(String line, StringBuilder sb) {
        boolean esc = false;
        Mode mode = Mode.PLAIN;
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            if (c == '\'' && mode != Mode.DOUBLE_QUOTE) {
                if (esc) {
                    sb.append('\'');
                } else {
                    mode = mode.toggle(Mode.SINGLE_QUOTE);
                }
            } else if (mode != Mode.SINGLE_QUOTE && c == '\\') {
                if (esc) {
                    sb.append('\\');
                    esc = false;
                } else {
                    esc = true;
                }
            } else if (mode != Mode.SINGLE_QUOTE && !esc && c == '\"') {
                mode = mode.toggle(Mode.DOUBLE_QUOTE);
            } else if (esc) {
                sb.append(escapeValue(c));
                esc = false;
            } else { // either quoted or not escaped
                sb.append(c);
            }
        }
        if (mode != Mode.PLAIN) {
            return LineResult.UNMATCHED_QUOTE;
        }
        return esc ? LineResult.CONTINUE : LineResult.END;
    }

    enum Mode {
        SINGLE_QUOTE, DOUBLE_QUOTE, PLAIN;

        Mode toggle(Mode other) {
            return this == other ? PLAIN : other;
        }
    }

    private static class NumberedLine {
        final int number;
        final String line;

        NumberedLine(int number, String line) {
            this.number = number;
            this.line = line;
        }
    }

    // visible for testing
    static final class NumberedLineResult {
        private final int number;
        private final LineResult lineResult;

        private NumberedLineResult(int number, LineResult lineResult) {
            this.number = number;
            this.lineResult = lineResult;
        }

        // visible for testing
        int number() {
            return number;
        }

        // visible for testing
        LineResult lineResult() {
            return lineResult;
        }
    }

    enum LineResult {
        CONTINUE, END, UNMATCHED_QUOTE() {
            @Override
            boolean isError() {
                return true;
            }

            @Override
            String message() {
                return "unmatched quote";
            }
        }, NO_NEXT_LINE {
            @Override
            boolean isError() {
                return true;
            }

            @Override
            String message() {
                return "backslash at end of file";
            }
        };

        boolean isError() {
            return false;
        }

        String message() {
            return "";
        }
    }

    private char escapeValue(char c) {
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            default:
                return c;
        }
    }
}
