package net.jbock.annotated;

import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotatedOption extends AnnotatedMethod {

    // visible for testing
    static final Comparator<String> UNIX_NAMES_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final ExecutableOption option;
    private final List<String> names;
    private final boolean hasUnixName;

    private AnnotatedOption(
            EnumName enumName,
            ExecutableOption option,
            String paramLabel,
            List<String> names,
            boolean hasUnixName) {
        super(enumName, paramLabel);
        this.option = option;
        this.names = names;
        this.hasUnixName = hasUnixName;
    }

    static AnnotatedOption createOption(
            ExecutableOption option,
            EnumName enumName) {
        List<String> names = option.names().stream()
                .sorted(UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList());
        String paramLabel = option.paramLabel().or(() -> names.stream()
                .filter(name -> name.startsWith("--"))
                .map(name -> name.substring(2))
                .map(s -> s.toUpperCase(Locale.US))
                .findFirst())
                .orElseGet(() -> SnakeName.create(option.simpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        boolean hasUnixName = names.stream().anyMatch(s -> s.length() == 2);
        return new AnnotatedOption(
                enumName,
                option,
                paramLabel,
                names,
                hasUnixName);
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

    public boolean hasUnixName() {
        return hasUnixName;
    }

    @Override
    Stream<AnnotatedOption> asAnnotatedOption() {
        return Stream.of(this);
    }

    @Override
    Stream<AnnotatedParameter> asAnnotatedParameter() {
        return Stream.empty();
    }

    @Override
    Stream<AnnotatedParameters> asAnnotatedParameters() {
        return Stream.empty();
    }
}
