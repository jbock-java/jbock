package net.jbock.source;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class SourceOption extends SourceMethod<AnnotatedOption> {

    private final AnnotatedOption option;
    private final boolean hasUnixName;

    public SourceOption(
            AnnotatedOption option,
            EnumName enumName,
            boolean hasUnixName) {
        super(enumName);
        this.option = option;
        this.hasUnixName = hasUnixName;
    }

    public static SourceOption create(
            AnnotatedOption option,
            EnumName enumName) {
        boolean hasUnixName = option.names().stream().anyMatch(s -> s.length() == 2);
        return new SourceOption(option, enumName, hasUnixName);
    }

    public List<String> names() {
        return option.names();
    }

    @Override
    public AnnotatedOption annotatedMethod() {
        return option;
    }

    @Override
    public Optional<SourceOption> asAnnotatedOption() {
        return Optional.of(this);
    }

    @Override
    public Optional<SourceParameter> asAnnotatedParameter() {
        return Optional.empty();
    }

    @Override
    public Optional<SourceParameters> asAnnotatedParameters() {
        return Optional.empty();
    }

    @Override
    public String paramLabel() {
        return option.label().or(() -> names().stream()
                .filter(name -> name.startsWith("--"))
                .map(name -> name.substring(2))
                .map(s -> s.toUpperCase(Locale.US))
                .findFirst())
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }

    @Override
    public boolean hasUnixName() {
        return hasUnixName;
    }
}
