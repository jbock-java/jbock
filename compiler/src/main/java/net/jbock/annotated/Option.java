package net.jbock.annotated;

import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class Option implements Item {

    private static final Comparator<String> LENGTH_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final Supplier<List<String>> names = memoize(() -> optionNames().stream()
            .sorted(LENGTH_FIRST_COMPARATOR)
            .toList());

    private final Supplier<String> paramLabel = memoize(() -> optionParamLabel()
            .or(() -> optionNames().stream()
                    .filter(name -> name.startsWith("--"))
                    .map(name -> name.substring(2))
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .findFirst())
            .orElseGet(() -> SnakeName.create(methodName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final ExecutableElement method;
    private final net.jbock.Option option;
    private final String safeName;
    private final int sourceIndex;

    private Option(
            ExecutableElement method,
            net.jbock.Option option,
            String safeName,
            int sourceIndex) {
        this.method = method;
        this.option = option;
        this.safeName = safeName;
        this.sourceIndex = sourceIndex;
    }

    static Option createOption(
            ExecutableElement method,
            net.jbock.Option option,
            String safeName,
            int sourceIndex) {
        return new Option(method, option, safeName, sourceIndex);
    }

    @Override
    public Optional<String> descriptionKey() {
        return optionalString(option.descriptionKey());
    }

    @Override
    public List<String> description() {
        return List.of(option.description());
    }

    @Override
    public ExecutableElement method() {
        return method;
    }

    @Override
    public String safeName() {
        return safeName;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    public int sourceIndex() {
        return sourceIndex;
    }

    public List<String> names() {
        return names.get();
    }

    private List<String> optionNames() {
        return List.of(option.names());
    }

    private Optional<String> optionParamLabel() {
        return optionalString(option.paramLabel());
    }
}
