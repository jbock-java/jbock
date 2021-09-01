package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Mutable command line parser that allows additional
 * {@link #rest()} tokens after the last positional parameter.
 * This parser rejects suspicious tokens, but allows them after
 * the last positional parameter has been read. It does not support
 * double-dash escape.
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
    void parse(Iterator<String> it) throws ExToken {
        int position = 0;
        while (it.hasNext()) {
            String token = it.next();
            if (position == numParams()) {
                rest.add(token);
                continue;
            }
            if (tryReadOption(token, it)) {
                continue;
            }
            if (suspicious(token)) {
                throw new ExToken(ErrTokenType.INVALID_OPTION, token);
            }
            setParam(position++, token);
        }
    }

    /**
     * Returns all remaining tokens, after the last
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
