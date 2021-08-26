package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Map;
import java.util.stream.Stream;

public final class RegularParser<T> extends SubParser<T> {

    public RegularParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
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
