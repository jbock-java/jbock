package net.jbock.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.jbock.util.Either.firstFailure;
import static net.jbock.util.Either.left;
import static net.jbock.util.Either.right;

final class ParseRequestExpand extends ParseRequest {

    private final Path path;
    private final List<String> args;

    ParseRequestExpand(Path path, List<String> args) {
        this.path = path;
        this.args = args;
    }

    @Override
    public Either<AtFileError, List<String>> expand() {
        try {
            List<String> lines = Files.readAllLines(path);
            return readAtLines(lines)
                    .fold(
                            numberedLineResult -> Either.left(
                                    new AtFileSyntaxError(path, numberedLineResult.number,
                                            numberedLineResult.lineResult.message())),
                            atLines -> {
                                List<String> atLinesWithRest = new ArrayList<>(atLines.size() + args.size());
                                atLinesWithRest.addAll(atLines);
                                atLinesWithRest.addAll(args);
                                return Either.right(atLinesWithRest);
                            });
        } catch (Exception e) {
            return left(new AtFileReadError(e, path));
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
        return tokens.stream().collect(firstFailure());
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
            return left(new NumberedLineResult(current.number, LineResult.BACKSLASH_BEFORE_EOF));
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

    private enum Mode {
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

    // visible for testing
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
        }, BACKSLASH_BEFORE_EOF {
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
        return switch (c) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> c;
        };
    }
}
