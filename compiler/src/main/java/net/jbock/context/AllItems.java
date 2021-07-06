package net.jbock.context;

import net.jbock.common.Constants;
import net.jbock.convert.Mapped;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import java.util.List;

final class AllItems {

    private final List<Mapped<? extends AbstractItem>> items;
    private final boolean anyRequired;

    private AllItems(
            List<Mapped<? extends AbstractItem>> items,
            boolean anyRequired) {
        this.items = items;
        this.anyRequired = anyRequired;
    }

    static AllItems create(
            List<Mapped<PositionalParameter>> positionalParams,
            List<Mapped<NamedOption>> namedOptions) {
        boolean anyRequired = Constants.concat(namedOptions, positionalParams).stream()
                .anyMatch(Mapped::isRequired);
        return new AllItems(Constants.concat(namedOptions, positionalParams), anyRequired);
    }

    public List<Mapped<? extends AbstractItem>> items() {
        return items;
    }

    boolean anyRequired() {
        return anyRequired;
    }
}
