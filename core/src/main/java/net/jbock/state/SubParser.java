package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.Map;

abstract class SubParser<T> extends GenericParser<T> {

    SubParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionParsers,
            int numParams) {
        super(optionNames, optionParsers, numParams);
    }

    @Override
    public final SubParser<T> parse(Iterator<String> it) throws ExToken {
        boolean endOfOptionParsing = false;
        int position = 0;
        while (it.hasNext()) {
            String token = it.next();
            if (!endOfOptionParsing && "--".equals(token)) {
                endOfOptionParsing = true;
                continue;
            }
            if (!endOfOptionParsing && tryReadOption(token, it)) {
                continue;
            }
            if (!endOfOptionParsing && suspicious(token)) {
                throw new ExToken(ErrTokenType.INVALID_OPTION, token);
            }
            position += handleParam(position, token);
        }
        return this;
    }

    abstract int handleParam(int position, String token) throws ExToken;
}
