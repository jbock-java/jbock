package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static net.jbock.annotated.AnnotatedOption.createOption;
import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class ExecutableOption extends Executable {

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

    private final Option option;

    ExecutableOption(
            ExecutableElement method,
            Option option,
            Optional<TypeElement> converter,
            String enumName) {
        super(method, converter, enumName);
        this.option = option;
    }

    @Override
    AnnotatedMethod<?> annotatedMethod() {
        return createOption(this);
    }

    @Override
    Optional<String> descriptionKey() {
        return optionalString(option.descriptionKey());
    }

    @Override
    List<String> description() {
        return List.of(option.description());
    }

    public List<String> names() {
        return names.get();
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

    private List<String> optionNames() {
        return List.of(option.names());
    }

    Optional<String> optionParamLabel() {
        return optionalString(option.paramLabel());
    }
}
