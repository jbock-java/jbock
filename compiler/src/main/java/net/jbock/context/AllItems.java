package net.jbock.context;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapped;

import java.util.List;

import static net.jbock.common.Constants.concat;

final class AllItems {

    private final List<Mapped<?>> items;
    private final boolean anyRequired;

    private AllItems(
            List<Mapped<?>> items,
            boolean anyRequired) {
        this.items = items;
        this.anyRequired = anyRequired;
    }

    static AllItems create(
            List<Mapped<AnnotatedParameter>> positionalParams,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParams,
            List<Mapped<AnnotatedOption>> namedOptions) {
        List<Mapped<?>> everything = concat(concat(namedOptions, positionalParams), repeatablePositionalParams);
        boolean anyRequired = everything.stream().anyMatch(Mapped::isRequired);
        return new AllItems(everything, anyRequired);
    }

    List<Mapped<?>> items() {
        return items;
    }

    boolean anyRequired() {
        return anyRequired;
    }
}
