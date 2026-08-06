package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters and
 * any number of <em>arbitrary</em> excess tokens after that.
 *
 * <p>The parser does not recognize the end-of-option-parsing token &quot;--&quot;.
 */
public final class SuperParser extends SimpleParser {

    private final List<String> rest = new ArrayList<>();

    private SuperParser(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a SuperParser.
     *
     * @param optionNames maps option names to indexes in the array of option states
     * @param optionStates array of option states
     * @param numParams number of positional parameters
     *
     * @return new parser instance
     */
    public static SuperParser create(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        return new SuperParser(optionNames, optionStates, numParams);
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
