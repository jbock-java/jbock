package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Mutable command line parser that does not allow additional
 * non-option tokens after the last positional parameter has
 * been read.
 *
 * <p>The parser rejects suspicious tokens, and allows
 * double-dash escape.
 *
 * @param <T> type of keys that identify named options
 */
public final class StandardParser<T> extends SubParser<T> {

    private StandardParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a RegularParser.
     *
     * @param optionNames maps option names to option keys
     * @param optionStates maps option keys to option states
     * @param numParams number of positional parameters
     * @param <T> type of keys that identify named options
     *
     * @return a parser instance
     */
    public static <T> StandardParser<T> create(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        return new StandardParser<>(optionNames, optionStates, numParams);
    }

    @Override
    void handleExcessParam(String token) throws ExToken {
        throw new ExToken(ErrTokenType.EXCESS_PARAM, token);
    }

    @Override
    public Stream<String> rest() {
        return Stream.empty();
    }
}
