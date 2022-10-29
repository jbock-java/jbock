package net.jbock.annotated;

import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class Option extends Item {

    private static final Comparator<String> LENGTH_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final Supplier<List<String>> names = memoize(() -> optionNames().stream()
            .sorted(LENGTH_FIRST_COMPARATOR)
            .collect(toList()));

    private final Supplier<String> paramLabel = memoize(() -> optionParamLabel()
            .or(() -> optionNames().stream()
                    .filter(name -> name.startsWith("--"))
                    .map(name -> name.substring(2))
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .findFirst())
            .orElseGet(() -> SnakeName.create(simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final net.jbock.Option option;

    Option(
            ExecutableElement method,
            net.jbock.Option option,
            String enumName) {
        super(method, enumName);
        this.option = option;
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
    Annotation annotation() {
        return option;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isVarargsParameter() {
        return false;
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
