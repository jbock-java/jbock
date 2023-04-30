package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters and any
 * number of <em>non-option</em> excess tokens.
 *
 * <p>The parser recognizes the standard escape sequence.
 *
 * @param <T> type of keys that identify named options
 */
public final class VarargsParameterParser<T> extends AbstractParser<T> {

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
        return rest.stream();
    }
}
