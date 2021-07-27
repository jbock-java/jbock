package net.jbock.source;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class SourceOption extends SourceMethod<AnnotatedOption> {

    private final AnnotatedOption option;

    public SourceOption(
            AnnotatedOption option,
            EnumName enumName) {
        super(enumName);
        this.option = option;
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
    public final String paramLabel() {
        return option.label().or(() -> names().stream()
                .filter(name -> name.startsWith("--"))
                .map(name -> name.substring(2))
                .map(s -> s.toUpperCase(Locale.US))
                .findFirst())
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }
}
