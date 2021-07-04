package net.jbock.util;

import io.jbock.util.Optional;
import net.jbock.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Input data for the generated parse method.
 * Use either {@link #expansion(Path, List)} to request {@code @file} expansion,
 * or {@link #noExpansion} to disable it.
 * Alternatively, leave this decision to the user by invoking
 * {@link #prepare(String[])}.
 */
public final class ParseRequest {

    private final Optional<Path> path; // if empty, no @-file expansion will be performed
    private final List<String> rest;
    private final boolean helpEnabled;

    private ParseRequest(Optional<Path> path, List<String> rest, boolean helpEnabled) {
        this.path = path;
        this.rest = rest;
        this.helpEnabled = helpEnabled;
    }

    public static final class Builder {
        private final Optional<Path> path;
        private final List<String> rest;
        private boolean helpEnabled = true;

        public Builder(Optional<Path> path, List<String> rest) {
            this.path = path;
            this.rest = rest;
        }

        public Builder withHelpEnabled(boolean helpEnabled) {
            this.helpEnabled = helpEnabled;
            return this;
        }

        public ParseRequest build() {
            return new ParseRequest(path, rest, helpEnabled);
        }
    }

    /**
     * Prepare reading the input, potentially from an {@code @file},
     * by creating an intermediate {@code AtFileInput} object.
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
    public static Builder prepare(String[] args) {
        if (args.length >= 1
                && args[0].length() >= 2
                && args[0].startsWith("@")) {
            String fileName = args[0].substring(1);
            List<String> rest = List.of(args).subList(1, args.length);
            return expansion(Paths.get(fileName), rest);
        }
        return noExpansion(List.of(args));
    }

    /**
     * Creates an {@code AtFileInput} that disables {@code @file} expansion explicitly,
     * regardless of the {@code args}.
     *
     * @param args command line input
     * @return a builder
     */
    public static Builder noExpansion(List<String> args) {
        return new Builder(Optional.empty(), args);
    }

    /**
     * Creates an {@code AtFileInput} that requests {@code @file} expansion explicitly.
     *
     * @param path a path to be used as input for {@code @file} expansion
     * @param rest the remaining command line arguments, if any
     * @return a builder
     */
    public static Builder expansion(Path path, List<String> rest) {
        return new Builder(Optional.of(path), rest);
    }

    Optional<Path> path() {
        return path;
    }

    List<String> rest() {
        return rest;
    }

    public boolean isHelpRequested() {
        if (path.isPresent() || !helpEnabled) {
            return false;
        }
        return !rest.isEmpty() && "--help".equals(rest.get(0));
    }

    public boolean isHelpEnabled() {
        return helpEnabled;
    }

    public boolean isEmpty() {
        return !path.isPresent() && rest.isEmpty();
    }
}
