package net.jbock.util;

import io.jbock.util.Either;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * Allows reading some command line options from
 * a configuration file, if requested.
 *
 * <p>In the configuration file, the following escape sequences are recognized:
 *
 * <table>
 *   <caption></caption>
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
 * <p>Note: additional tokens in the input array, after the
 * initial {@code @file} token, are allowed and will be appended to
 * the result of reading the {@code @file}.
 */
public final class AtFileReader {

    /**
     * If {@link ParseRequest#path() request.path()} is present, reads the contents
     * of the {@code @file} into a list of Strings, and appends the remaining
     * {@link ParseRequest#args() request.args()}.
     * Otherwise, returns {@code request.args()}.
     *
     * @param request a parse request containing the command line input
     * @return the result of {@code file} expansion, if {@code @file} expansion is requested,
     *         otherwise {@code request.args()}
     */
    public Either<? extends AtFileError, List<String>> read(ParseRequest request) {
        return request.path().<Either<? extends AtFileError, List<String>>>map(path -> {
            try {
                List<String> lines = Files.readAllLines(path);
                return readAtLines(lines)
                        .mapLeft(r -> new AtFileSyntaxError(path, r.number, r.lineResult.message())) // TODO
                        .map(atLines -> {
                            List<String> atLinesWithRest = new ArrayList<>(atLines);
                            atLinesWithRest.addAll(request.args());
                            return atLinesWithRest;
                        });
            } catch (Exception e) {
                return left(new AtFileReadError(e, path));
            }
        }).orElseGet(() -> right(request.args()));
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
