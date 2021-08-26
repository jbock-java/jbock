package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Mutable command line parser that does not know about
 * {@link #rest()} tokens.
 * This parser rejects suspicious tokens, and allows
 * double-dash escape.
 *
 * @param <T> type of keys that identify named options
 */
public final class RegularParser<T> extends SubParser<T> {

    private RegularParser(
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
    public static <T> RegularParser<T> create(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        return new RegularParser<>(optionNames, optionStates, numParams);
    }

    @Override
    int handleParam(int position, String token) throws ExToken {
        if (position == numParams()) {
            throw new ExToken(ErrTokenType.EXCESS_PARAM, token);
        }
        setParam(position, token);
        return 1;
    }

    @Override
    public Stream<String> rest() {
        return Stream.of();
    }
}
