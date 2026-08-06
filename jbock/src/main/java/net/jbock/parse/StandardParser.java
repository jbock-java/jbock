package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters and
 * rejects any excess positional parameters after that.
 *
 * <p>The parser recognizes the end-of-option-parsing token &quot;--&quot;.
 */
public final class StandardParser extends SimpleParser {

    private StandardParser(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a RegularParser.
     *
     * @param optionNames maps option names to indexes in the array of option states
     * @param optionStates array of option states
     * @param numParams number of positional parameters
     *
     * @return new parser instance
     */
    public static  StandardParser create(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        return new StandardParser(optionNames, optionStates, numParams);
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
