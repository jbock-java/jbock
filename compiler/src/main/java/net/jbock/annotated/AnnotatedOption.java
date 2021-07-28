package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceOption;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            Optional<TypeElement> converter,
            Option option,
            List<Modifier> accessModifiers,
            List<String> names,
            boolean hasUnixName) {
        super(method, accessModifiers, converter);
        this.option = option;
        this.names = names;
        this.hasUnixName = hasUnixName;
    }

    static AnnotatedOption create(
            ExecutableElement method,
            Optional<TypeElement> converter,
            Option option,
            List<Modifier> accessModifiers) {
        List<String> names = Arrays.stream(option.names())
                .sorted(UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList());
        boolean hasUnixName = names.stream().anyMatch(s -> s.length() == 2);
        return new AnnotatedOption(method, converter, option, accessModifiers, names, hasUnixName);
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
        return Descriptions.optionalString(option.descriptionKey());
    }

    @Override
    public Optional<String> label() {
        return Descriptions.optionalString(option.paramLabel());
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
    public SourceMethod<?> sourceMethod(EnumName enumName) {
        return SourceOption.create(this, enumName);
    }
}
