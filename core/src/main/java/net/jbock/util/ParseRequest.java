package net.jbock.util;

import io.jbock.util.Either;

import java.nio.file.Paths;
import java.util.List;

/**
 * A convenience class that performs {@code @-file} expansion,
 * if the first token in the command line input starts with an
 * {@code "@"} character.
 */
public abstract class ParseRequest {

    /**
     * Creates a {@code ParseRequest}.
     * {@code @-file} expansion will be performed if the input array is nonempty,
     * and the first token contains at least 2 characters, and starts with an
     * {@code "@"} character.
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
            return new ParseRequestExpand(Paths.get(fileName), rest);
        }
        return new ParseRequestSimple(List.of(args));
    }

    /**
     * Returns a Right containing the result of {@code @-file} expansion.
     * If an error occurs during {@code @-file} reading, returns a Left containing
     * a description of the error.
     *
     * @return the result of {@code @-file} expansion
     */
    public abstract Either<? extends AtFileError, List<String>> expand();
}