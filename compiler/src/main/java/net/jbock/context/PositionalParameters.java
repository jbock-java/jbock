package net.jbock.context;

import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapped;

import java.util.ArrayList;
import java.util.List;

class PositionalParameters {

    private final List<Mapped<AnnotatedParameter>> regular; // (optional|required)
    private final List<Mapped<AnnotatedParameters>> repeatable;

    private PositionalParameters(
            List<Mapped<AnnotatedParameter>> regular,
            List<Mapped<AnnotatedParameters>> repeatable) {
        this.regular = regular;
        this.repeatable = repeatable;
    }

    static PositionalParameters create(
            List<Mapped<AnnotatedParameter>> regular,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParameter) {
        return new PositionalParameters(regular, repeatablePositionalParameter);
    }

    List<Mapped<AnnotatedParameter>> regular() {
        return regular;
    }

    List<Mapped<?>> parameters() {
        List<Mapped<?>> result = new ArrayList<>(regular.size() + 1);
        result.addAll(regular);
        result.addAll(repeatable);
        return result;
    }

    int size() {
        return regular().size() + (anyRepeatable() ? 1 : 0);
    }

    List<Mapped<AnnotatedParameters>> repeatable() {
        return repeatable;
    }

    boolean anyRepeatable() {
        return !repeatable.isEmpty();
    }

    boolean isEmpty() {
        return regular.isEmpty() && !anyRepeatable();
    }
}
