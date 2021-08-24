package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class SuperParser<T> extends GenericParser<T> {

    private final List<String> rest = new ArrayList<>();

    public SuperParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionParsers,
            int numParams) {
        super(optionNames, optionParsers, numParams);
    }

    @Override
    public SuperParser<T> parse(Iterator<String> it) throws ExToken {
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
            setParam(position, token);
        }
        return this;
    }

    @Override
    public Stream<String> rest() {
        return rest.stream();
    }
}
