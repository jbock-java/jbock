package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.stream.Stream;

import static net.jbock.parse.OptionStateUtil.readOptionArgument;

/**
 * Reads and stores the argument of a non-repeatable option.
 * The {@code read} method cannot be invoked more than once, otherwise
 * an exception is thrown.
 */
public final class OptionStateRegular implements OptionState {

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
        return value == null ? Stream.empty() : Stream.of(value);
    }
}
