package net.jbock.util;

import io.jbock.util.Optional;
import net.jbock.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Input for the generated parser.
 */
public final class ParseRequest {

    private final Optional<Path> path; // if empty, no @-file expansion will be performed
    private final List<String> rest;
    private final boolean helpRequested;

    private ParseRequest(Optional<Path> path, List<String> rest, boolean helpRequested) {
        this.path = path;
        this.rest = rest;
        this.helpRequested = helpRequested;
    }

    public static final class Builder {
        private final Optional<Path> path;
        private final List<String> rest;
        private boolean helpRequested;

        private Builder(Optional<Path> path, List<String> rest) {
            this.path = path;
            this.rest = rest;
        }

        public Builder withHelpRequested(boolean helpRequested) {
            this.helpRequested = helpRequested;
            return this;
        }

        public ParseRequest build() {
            return new ParseRequest(path, rest, helpRequested);
        }
    }

    /**
     * Creates a builder.
     *
     * <p>{@code @file} expansion will be enabled if the input is not empty,
     * and the first token starts with an {@code "@"} character.
     *
     * <p>This method may be invoked from the generated code,
     * unless {@link Command#atFileExpansion()} is {@code false}.
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
     * @param path a path to be used as input for {@code @file} expansion
     * @param rest the remaining command line arguments, if any
     * @return a builder
     */
    public static Builder expand(Path path, List<String> rest) {
        return new Builder(Optional.of(path), rest);
    }

    Optional<Path> path() {
        return path;
    }

    List<String> rest() {
        return rest;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }
}
