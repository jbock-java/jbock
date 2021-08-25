package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.stream.Stream;

public abstract class OptionState {

    public abstract String read(String token, Iterator<String> it) throws ExToken;

    public abstract Stream<String> stream();

    public final String readOptionArgument(String token, Iterator<String> it) throws ExToken {
        boolean unix = !token.startsWith("--");
        if (unix && token.length() > 2) {
            return token.substring(2);
        }
        if (!unix && token.contains("=")) {
            return token.substring(token.indexOf('=') + 1);
        }
        if (it.hasNext()) {
            return it.next();
        }
        throw new ExToken(ErrTokenType.MISSING_ARGUMENT, token);
    }
}
