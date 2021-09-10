package net.jbock.parse;

import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.Map;

import static net.jbock.util.ErrTokenType.INVALID_OPTION;

abstract class SubParser<T> extends AbstractParser<T> {

    SubParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    @Override
    final void parse(Iterator<String> it) throws ExToken {
        boolean endOfOptionParsing = false;
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
                throw new ExToken(INVALID_OPTION, token);
            }
            if (isExcessPosition()) {
                handleExcessParam(token);
            } else {
                handleParam(token);
            }
        }
    }

    abstract void handleExcessParam(String token) throws ExToken;
}
