package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters and
 * rejects any excess non-option parameters after that.
 *
 * <p>The parser recognizes the standard escape sequence.
 *
 * @param <T> type of keys that identify named options
 */
public final class StandardParser<T> extends AbstractParser<T> {

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
    boolean isEscapeSequence(String token) {
        return "--".equals(token);
    }

    @Override
    boolean hasOptionParsingEnded(int position) {
        return false;
    }

    @Override
    public Stream<String> rest() {
        return Stream.empty();
    }
}
