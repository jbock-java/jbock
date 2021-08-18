package net.jbock.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Input for the generated parser.
 */
public final class ParseRequest {

    private final Optional<Path> path; // if empty, no @-file expansion will be performed
    private final List<String> args; // command line arguments, excluding path

    private ParseRequest(Optional<Path> path, List<String> args) {
        this.path = path;
        this.args = args;
    }

    /**
     * A builder for {@link ParseRequest}.
     */
    public static final class Builder {

        private final Optional<Path> path;
        private final List<String> rest;

        private Builder(Optional<Path> path, List<String> rest) {
            this.path = path;
            this.rest = rest;
        }

        /**
         * Creates the parse request.
         *
         * @return a parse request
         */
        public ParseRequest build() {
            return new ParseRequest(path, rest);
        }
    }

    /**
     * Creates a builder.
     *
     * <p>{@code @file} expansion will be enabled if the input is not empty,
     * and the first token starts with an {@code "@"} character.
     *
     * @param args command line input
     * @return the options in the file, or an error report
     */
    public static Builder standardBuilder(String[] args) {
        if (args.length >= 1
                && args[0].length() >= 2
                && args[0].startsWith("@")) {
            String fileName = args[0].substring(1);
            List<String> rest = List.of(args).subList(1, args.length);
            return expand(Paths.get(fileName), rest);
        }
        return simple(List.of(args));
    }

    /**
     * Creates a builder with {@code @file} expansion set to {@code false}.
     *
     * @param args command line input
     * @return a builder
     */
    public static Builder simple(List<String> args) {
        return new Builder(Optional.empty(), args);
    }

    /**
     * Creates a builder with {@code @file} expansion set to {@code true}.
     *
     * @param atFile source for {@code @file} expansion
     * @param rest the remaining command line arguments, possibly none
     * @return a builder
     */
    public static Builder expand(Path atFile, List<String> rest) {
        return new Builder(Optional.of(atFile), rest);
    }

    /**
     * Returns the {@code path} to be used as input for {@code @file} expansion.
     * If it is empty, {@code @file} expansion should not be performed.
     *
     * @return input for {@code @file} expansion
     */
    public Optional<Path> path() {
        return path;
    }

    /**
     * If {@link #path} is empty, contains the command line arguments.
     * If {@link #path} is nonempty, contains any remaining argument after
     * the {@code @file} parameter.
     *
     * @return command line arguments
     */
    public List<String> args() {
        return args;
    }
}
