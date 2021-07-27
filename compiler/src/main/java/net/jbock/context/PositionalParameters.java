package net.jbock.context;

import net.jbock.convert.Mapped;
import net.jbock.parameter.PositionalParameter;

import java.util.ArrayList;
import java.util.List;

class PositionalParameters {

    private final List<Mapped<PositionalParameter>> regular; // (optional|required)
    private final List<Mapped<PositionalParameter>> repeatable;

    private PositionalParameters(
            List<Mapped<PositionalParameter>> regular,
            List<Mapped<PositionalParameter>> repeatable) {
        this.regular = regular;
        this.repeatable = repeatable;
    }

    static PositionalParameters create(
            List<Mapped<PositionalParameter>> regular,
            List<Mapped<PositionalParameter>> repeatablePositionalParameter) {
        return new PositionalParameters(regular, repeatablePositionalParameter);
    }

    List<Mapped<PositionalParameter>> regular() {
        return regular;
    }

    List<Mapped<PositionalParameter>> parameters() {
        if (repeatable.isEmpty()) {
            return regular;
        }
        List<Mapped<PositionalParameter>> result = new ArrayList<>(regular.size() + 1);
        result.addAll(regular);
        result.addAll(repeatable);
        return result;
    }

    int size() {
        return regular().size() + (anyRepeatable() ? 1 : 0);
    }

    List<Mapped<PositionalParameter>> repeatable() {
        return repeatable;
    }

    boolean anyRepeatable() {
        return !repeatable.isEmpty();
    }

    boolean isEmpty() {
        return regular.isEmpty() && !anyRepeatable();
    }
}
