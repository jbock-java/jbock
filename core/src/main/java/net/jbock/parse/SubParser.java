package net.jbock.parse;

import net.jbock.util.ExToken;

import java.util.Map;

abstract class SubParser<T> extends AbstractParser<T> {

    SubParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    @Override
    final boolean isEscapeSequence(String token) {
        return "--".equals(token);
    }

    @Override
    final boolean hasOptionParsingEnded(int position) {
        return false;
    }

    abstract void handleExcessParam(String token) throws ExToken;
}
