package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.stream.Stream;

public final class OptionStateModeFlag extends OptionState {

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
    public Stream<String> stream() {
        return seen ? Stream.of("") : Stream.empty();
    }
}
