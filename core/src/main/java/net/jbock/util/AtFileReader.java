package net.jbock.util;

import io.jbock.util.Either;
import net.jbock.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * <p>Allow reading some or all command line options from
 * a configuration file, if the user requests it by prefixing
 * the first command line token with a {@code "@"} character.</p>
 *
 * <p>The following escape sequences are recognized:</p>
 *
 * <br/>
 * <table>
 *   <caption>Escape sequences</caption>
 *   <thead><tr><td><b>Code</b></td><td><b>Meaning</b></td></tr></thead>
 *   <tr><td>{@code \\}</td><td>backslash</td></tr>
 *   <tr><td>{@code \n}</td><td>newline</td></tr>
 *   <tr><td>{@code \r}</td><td>carriage return</td></tr>
 *   <tr><td>{@code \t}</td><td>horizontal tab</td></tr>
 * </table>
 *
 * <p>An unpaired backslash at the end of a line prevents
 * the newline from being read.</p>
 * <p>Note: Even if set to {@code true},
 * and the user wants to pass exactly one positional parameter
 * that starts with an {@code @} character,
 * they can still prevent the {@code @file} expansion,
 * by passing {@code --} as the first token.</p>
 * <p>Note: additional arguments after the {@code @file}
 * are allowed and will be appended to the result.</p>
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
            return right(Arrays.asList(args));
        }
        String fileName = args[0].substring(1);
        try {
            Path path = Paths.get(fileName);
            List<String> lines = Files.readAllLines(path);
            return readAtLines(lines)
                    .mapLeft(r -> new AtFileSyntaxError(fileName, r.line, r.lineResult.message())) // TODO
                    .map(atLines -> {
                        List<String> expanded = new ArrayList<>(atLines);
                        expanded.addAll(Arrays.asList(args).subList(1, args.length));
                        return expanded;
                    });
        } catch (Exception e) {
            return left(new AtFileReadError(e, fileName));
        }
    }

    Either<NumberedLineResult, List<String>> readAtLines(List<String> lines) {
        int[] counter = {1};
        Iterator<Entry<Integer, String>> it = lines.stream()
                .filter(line -> !line.isEmpty())
                .<Entry<Integer, String>>map(line -> new SimpleImmutableEntry<>(counter[0]++, line))
                .iterator();
        List<Either<NumberedLineResult, String>> tokens = new ArrayList<>(lines.size());
        while (it.hasNext()) {
            tokens.add(readTokenFromAtFile(it));
        }
        return tokens.stream().collect(Either.toValidList());
    }

    private Either<NumberedLineResult, String> readTokenFromAtFile(Iterator<Entry<Integer, String>> it) {
        StringBuilder sb = new StringBuilder();
        Entry<Integer, String> current;
        LineResult esc;
        do {
            current = it.next();
            esc = readLine(current.getValue(), sb);
        } while (esc == LineResult.CONTINUE && it.hasNext());
        if (esc.isError()) {
            return left(new NumberedLineResult(current.getKey(), esc));
        }
        return right(sb.toString());
    }

    private LineResult readLine(String line, StringBuilder sb) {
        boolean esc = false;
        Mode mode = Mode.REGULAR;
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            if (c == '\'') {
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
            } else if (esc) {
                sb.append(escapeValue(c));
                esc = false;
            } else { // either quoted or not escaped
                sb.append(c);
            }
        }
        if (mode != Mode.REGULAR) {
            return LineResult.UNMATCHED_QUOTE;
        }
        return esc ? LineResult.CONTINUE : LineResult.END;
    }

    enum Mode {
        SINGLE_QUOTE, QUOTE, REGULAR;

        Mode toggle(Mode other) {
            return this == other ? REGULAR : other;
        }
    }

    static class NumberedLineResult {
        final int line;
        final LineResult lineResult;

        NumberedLineResult(int line, LineResult lineResult) {
            this.line = line;
            this.lineResult = lineResult;
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
