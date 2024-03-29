package net.jbock.parse;

import net.jbock.util.ExToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static net.jbock.parse.OptionStateUtil.readOptionArgument;

/**
 * Reads and stores the arguments of a repeatable option.
 */
public final class OptionStateRepeatable implements OptionState {

    private List<String> values;

    @Override
    public String read(String token, Iterator<String> it) throws ExToken {
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(readOptionArgument(token, it));
        return null;
    }

    @Override
    public Stream<String> stream() {
        return values == null ? Stream.empty() : values.stream();
    }
}
