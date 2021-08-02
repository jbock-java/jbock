package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.common.Descriptions.optionalString;

public final class AnnotatedOption extends AnnotatedMethod {

    // visible for testing
    static final Comparator<String> UNIX_NAMES_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final Option option;
    private final List<String> names;
    private final boolean hasUnixName;

    private AnnotatedOption(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Option option,
            String paramLabel,
            List<Modifier> accessModifiers,
            List<String> names,
            boolean hasUnixName) {
        super(method, accessModifiers, converter, enumName, paramLabel);
        this.option = option;
        this.names = names;
        this.hasUnixName = hasUnixName;
    }

    static AnnotatedOption createOption(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Option option,
            List<Modifier> accessModifiers) {
        List<String> names = Arrays.stream(option.names())
                .sorted(UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList());
        String paramLabel = optionalString(option.paramLabel()).or(() -> names.stream()
                .filter(name -> name.startsWith("--"))
                .map(name -> name.substring(2))
                .map(s -> s.toUpperCase(Locale.US))
                .findFirst())
                .orElseGet(() -> SnakeName.create(method.getSimpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        boolean hasUnixName = names.stream().anyMatch(s -> s.length() == 2);
        return new AnnotatedOption(
                method,
                enumName,
                converter,
                option,
                paramLabel,
                accessModifiers,
                names,
                hasUnixName);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isParameters() {
        return false;
    }

    @Override
    public Optional<String> descriptionKey() {
        return optionalString(option.descriptionKey());
    }

    public List<String> names() {
        return names;
    }

    public boolean hasUnixName() {
        return hasUnixName;
    }

    @Override
    public List<String> description() {
        return List.of(option.description());
    }

    @Override
    public Optional<AnnotatedOption> asAnnotatedOption() {
        return Optional.of(this);
    }

    @Override
    public Optional<AnnotatedParameter> asAnnotatedParameter() {
        return Optional.empty();
    }

    @Override
    public Optional<AnnotatedParameters> asAnnotatedParameters() {
        return Optional.empty();
    }
}
