package net.jbock.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class RepeatableParser<T> extends SubParser<T> {

    private final List<String> rest = new ArrayList<>();

    public RepeatableParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        super(optionNames, optionStates, numParams);
    }

    @Override
    int handleParam(int position, String token) {
        if (position < numParams()) {
            setParam(position, token);
            return 1;
        } else {
            rest.add(token);
            return 0;
        }
    }

    @Override
    public Stream<String> rest() {
        return rest.stream();
    }
}
