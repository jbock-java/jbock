package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Mutable command line parser that allows additional
 * {@link #rest()} tokens after the last positional parameter.
 * This parser rejects suspicious tokens, and allows
 * double-dash escape.
 *
 * @param <T> type of keys that identify named options
 */
public final class RepeatableParser<T> extends SubParser<T> {

    private final List<String> rest = new ArrayList<>();

    private RepeatableParser(
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
    public static <T> RepeatableParser<T> create(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        return new RepeatableParser<>(optionNames, optionStates, numParams);
    }

    @Override
    int handleParam(int position, String token) {
        if (position < numParams()) {
            setParam(position, token);
            return 1;
        } else {
            rest.add(token);
            return 0;
        }
    }

    @Override
    public Stream<String> rest() {
        return rest.stream();
    }
}
