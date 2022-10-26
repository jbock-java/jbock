package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Suppliers.memoize;

public final class AnnotatedOption extends AnnotatedMethod<ExecutableOption> {

    // visible for testing
    static final Comparator<String> LENGTH_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final Supplier<List<String>> names = memoize(() -> executable().names().stream()
            .sorted(LENGTH_FIRST_COMPARATOR)
            .collect(toList()));

    private final Supplier<String> paramLabel = memoize(() -> executable().paramLabel()
            .or(() -> names().stream()
                    .filter(name -> name.startsWith("--"))
                    .map(name -> name.substring(2))
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .findFirst())
            .orElseGet(() -> SnakeName.create(executable().simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private AnnotatedOption(ExecutableOption option) {
        super(option);
    }

    static AnnotatedOption createOption(ExecutableOption option) {
        return new AnnotatedOption(option);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isVarargsParameter() {
        return false;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    public List<String> names() {
        return names.get();
    }
}
