package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.List;

/**
 * Detects and stores the presence of a mode flag.
 * The {@code read} method cannot be invoked more than once, otherwise
 * an exception is thrown.
 */
public final class OptionStateModeFlag implements OptionState {

    private boolean seen;

    @Override
    public String read(String token, Iterator<String> it) throws ExToken {
        if (seen) {
            throw new ExToken(ErrTokenType.OPTION_REPETITION, token);
        }
        seen = true;
        if (token.startsWith("--") || token.length() == 2) {
            return null;
        }
        return '-' + token.substring(2);
    }

    @Override
    public List<String> stream() {
        return seen ? List.of("") : List.of();
    }
}
