package net.jbock.state;

import net.jbock.util.ExToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class OptionStateRepeatable extends OptionState {

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
