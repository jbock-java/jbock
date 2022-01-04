package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

public final class AnnotatedOption extends AnnotatedMethod {

    // visible for testing
    static final Comparator<String> LENGTH_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final ExecutableOption option;
    private final List<String> names;

    private AnnotatedOption(
            String enumName,
            ExecutableOption option,
            String paramLabel,
            List<String> names) {
        super(enumName, paramLabel);
        this.option = option;
        this.names = names;
    }

    static AnnotatedOption createOption(
            ExecutableOption option,
            String enumName) {
        List<String> names = option.names().stream()
                .sorted(LENGTH_FIRST_COMPARATOR)
                .collect(toList());
        String paramLabel = option.paramLabel().or(() -> names.stream()
                        .filter(name -> name.startsWith("--"))
                        .map(name -> name.substring(2))
                        .map(s -> s.toUpperCase(Locale.US))
                        .findFirst())
                .orElseGet(() -> SnakeName.create(option.simpleName())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedOption(
                enumName,
                option,
                paramLabel,
                names);
    }

    @Override
    Executable executable() {
        return option;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isParameters() {
        return false;
    }

    public List<String> names() {
        return names;
    }
}
