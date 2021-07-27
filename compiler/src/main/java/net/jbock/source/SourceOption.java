package net.jbock.source;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class SourceOption extends SourceMethod<AnnotatedOption> {

    private final AnnotatedOption option;

    public SourceOption(
            AnnotatedOption option,
            EnumName enumName) {
        super(enumName);
        this.option = option;
    }

    public List<String> names() {
        return annotatedMethod().names();
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.empty();
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
}
