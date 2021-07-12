package net.jbock.context;

import net.jbock.convert.Mapped;
import net.jbock.parameter.PositionalParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class PositionalParameters {

    private final List<Mapped<PositionalParameter>> regular; // (optional|required)
    private final Optional<Mapped<PositionalParameter>> repeatable;

    private PositionalParameters(
            List<Mapped<PositionalParameter>> regular,
            Optional<Mapped<PositionalParameter>> repeatable) {
        this.regular = regular;
        this.repeatable = repeatable;
    }

    static PositionalParameters create(List<Mapped<PositionalParameter>> all) {
        List<Mapped<PositionalParameter>> regular = all.stream()
                .filter(c -> !c.isRepeatable())
                .collect(Collectors.toUnmodifiableList());
        Optional<Mapped<PositionalParameter>> repeatable = all.stream()
                .filter(Mapped::isRepeatable)
                .findFirst();
        return new PositionalParameters(regular, repeatable);
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
        repeatable.ifPresent(result::add);
        return result;
    }

    int size() {
        return regular().size() + (anyRepeatable() ? 1 : 0);
    }

    Optional<Mapped<PositionalParameter>> repeatable() {
        return repeatable;
    }

    boolean anyRepeatable() {
        return repeatable.isPresent();
    }

    boolean isEmpty() {
        return regular.isEmpty() && !anyRepeatable();
    }
}
