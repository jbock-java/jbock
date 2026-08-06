package net.jbock.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser accepts a fixed number of positional parameters, and any
 * number of excess positional parameters after that.
 *
 * <p>The parser recognizes the end-of-option-parsing token &quot;--&quot;.
 */
public final class VarargsParameterParser extends SimpleParser {

    private final List<String> rest = new ArrayList<>();

    private VarargsParameterParser(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    /**
     * Creates a VarargsParameterParser.
     *
     * @param optionNames maps option names to indexes in the array of option states
     * @param optionStates array of option states
     * @param numParams number of non-repeatable positional parameters
     *
     * @return new parser instance
     */
    public static VarargsParameterParser create(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        return new VarargsParameterParser(optionNames, optionStates, numParams);
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
