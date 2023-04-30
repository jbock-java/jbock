package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters and
 * any number of <em>arbitrary</em> excess tokens.
 *
 * <p>The parser does not recognize the standard escape sequence.
 *
 * @param <T> type of keys that identify named options
 */
public final class SuperParser<T> extends AbstractParser<T> {

    private final List<String> rest = new ArrayList<>();

    private SuperParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a SuperParser.
     *
     * @param optionNames maps option names to option keys
     * @param optionStates maps option keys to option states
     * @param numParams number of positional parameters
     * @param <T> type of keys that identify named options
     *
     * @return a parser instance
     */
    public static <T> SuperParser<T> create(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        return new SuperParser<>(optionNames, optionStates, numParams);
    }

    @Override
    boolean hasOptionParsingEnded(int position) {
        return position >= numParams();
    }

    @Override
    boolean isEscapeSequence(String token) {
        return false;
    }

    @Override
    void handleExcessParam(String token) {
        rest.add(token);
    }

    @Override
    public Stream<String> rest() {
        return rest.stream();
    }
}
