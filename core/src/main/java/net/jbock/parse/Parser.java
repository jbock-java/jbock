package net.jbock.parse;

import net.jbock.util.ExFailure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Common interface of several types of mutable command line parsers.
 * Mutable parsers are not reusable.
 * The parser does not do string conversion, so all parsing results
 * are in the form of strings.
 *
 * @param <T> type of keys that identify named options
 */
interface Parser<T> {

    /**
     * Parse the given input and store the result internally.
     * This method should only be invoked once.
     *
     * @param tokens command line input
     * @throws ExFailure if the input is not valid command line syntax
     */
    void parse(List<String> tokens) throws ExFailure;

    /**
     * Returns the arguments of the given option.
     * If the option was not present on the command line,
     * an empty stream is returned. If the option is not
     * repeatable, the stream will contain exactly one element.
     * In the case of a nullary option, an empty stream
     * represents absence, and any nonempty stream represents presence
     * of the option.
     *
     * <p>This method should be not be invoked before {@link #parse(List)}
     * was invoked.
     *
     * @param option the option key
     * @return a stream of strings
     */
    Stream<String> option(T option);

    /**
     * Returns the value of the positional parameter at the given position,
     * if any.
     *
     * <p>This method should be not be invoked before {@link #parse(List)}
     * was invoked.
     *
     * @param index the parameter position
     * @return an optional string
     */
    Optional<String> param(int index);
}
