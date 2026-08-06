package net.jbock.parse;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class represents the result of a successful parsing operation.
 */
public interface SimpleParseResult {

    /**
     * Get all option values for the given option key,
     * in the order in which they were passed on the command line.
     *
     * @param optionIndex a number that identifies a named option
     * @return a stream of tokens
     */
    Stream<String> option(int optionIndex);

    /**
     * Get the positional parameter at the given index.
     *
     * <p>If the index is negative, or if there is no positional parameter at this index,
     * an empty {@code Optional} is returned.
     *
     * @param index a number
     * @return the positional parameter at the given index
     */
    Optional<String> param(int index);

    /**
     * Returns the remaining tokens after the last positional parameter.
     *
     * @return remaining tokens
     */
    Stream<String> rest();
}
