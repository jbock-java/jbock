package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Mutable command line parser that allows an arbitrary number of additional
 * tokens after the last positional parameter.
 * The parser rejects unknown option-like tokens, and recognizes
 * double-dash escape.
 *
 * @param <T> type of keys that identify named options
 */
public final class VarargsParameterParser<T> extends SubParser<T> {

    private final List<String> rest = new ArrayList<>();

    private VarargsParameterParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a RepeatableParser.
     *
     * @param optionNames maps option names to option keys
     * @param optionStates maps option keys to option states
     * @param numParams number of non-repeatable positional parameters
     * @param <T> type of keys that identify named options
     *
     * @return a parser instance
     */
    public static <T> VarargsParameterParser<T> create(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        return new VarargsParameterParser<>(optionNames, optionStates, numParams);
    }

    @Override
    void handleExcessParam(String token) {
        rest.add(token);
    }

    /**
     * Returns the additional positional parameters, after the last
     * regular positional parameter was read.
     *
     * <p>This method should be not be invoked before {@link #parse(List)}
     * was invoked.
     *
     * @return a stream of strings
     */
    public Stream<String> rest() {
        return rest.stream();
    }
}
