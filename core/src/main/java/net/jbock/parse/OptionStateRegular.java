package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public final class OptionStateRegular extends OptionState {

    private String value;

    @Override
    public String read(String token, Iterator<String> it) throws ExToken {
        if (value != null) {
            throw new ExToken(ErrTokenType.OPTION_REPETITION, token);
        }
        value = readOptionArgument(token, it);
        return null;
    }

    @Override
    public Stream<String> stream() {
        return Optional.ofNullable(value).stream();
    }
}
