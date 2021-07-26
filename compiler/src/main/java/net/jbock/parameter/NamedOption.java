package net.jbock.parameter;

import net.jbock.common.SnakeName;

import java.util.List;
import java.util.Locale;

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

    @Override
    public final String paramLabel() {
        return sourceMethod().label().or(() -> names.stream()
                .filter(name -> name.startsWith("--"))
                .map(name -> name.substring(2))
                .map(s -> s.toUpperCase(Locale.US))
                .findFirst())
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }

    public boolean hasUnixName() {
        return names.stream().anyMatch(s -> s.length() == 2);
    }
}
