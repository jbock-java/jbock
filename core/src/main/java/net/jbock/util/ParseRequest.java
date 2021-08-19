package net.jbock.util;

import io.jbock.util.Either;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Input for the generated parser.
 */
public abstract class ParseRequest {

    /**
     * Creates a {@code ParseRequest}.
     * {@code @-file} expansion will be enabled if the input array is nonempty,
     * and the first token starts with an {@code "@"} character.
     *
     * @param args command line input
     * @return a parse request
     */
    public static ParseRequest from(String[] args) {
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
     * Creates a {@code ParseRequest} that indicates {@code @-file} expansion should
     * not be performed.
     *
     * @param args command line input
     * @return a parse request
     */
    public static ParseRequest simple(List<String> args) {
        return new ParseRequestSimple(args);
    }

    /**
     * Creates a {@code ParseRequest} that indicates {@code @-file} expansion should
     * be performed.
     *
     * @param atFile the {@code @-file}
     * @param rest additional command line arguments
     * @return a parse request
     */
    public static ParseRequest expand(Path atFile, List<String> rest) {
        return new ParseRequestExpand(atFile, rest);
    }

    /**
     * Returns the path of the {@code @-file}, or an empty
     * {@code Optional} if {@code @-file} expansion is
     * not requested.
     *
     * @return path of the {@code @-file}, or {@code empty}
     */
    public abstract Optional<Path> path();

    /**
     * Returns the command line arguments, excluding the {@code @-file}.
     *
     * @return command line arguments
     */
    public abstract List<String> args();

    public abstract Either<? extends AtFileError, List<String>> expand();
}