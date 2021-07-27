package net.jbock.parameter;

import net.jbock.source.SourceMethod;

import java.util.List;

public final class NamedOption extends AbstractItem {

    private final List<String> names;

    public NamedOption(
            List<String> names,
            SourceMethod<?> sourceMethod) {
        super(sourceMethod);
        this.names = names;
    }

    public List<String> names() {
        return names;
    }

    public boolean hasUnixName() {
        return names.stream().anyMatch(s -> s.length() == 2);
    }
}
