package net.jbock.source;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;

import java.util.OptionalInt;

public final class SourceOption extends SourceMethod<AnnotatedOption> {

    private final AnnotatedOption option;

    public SourceOption(
            AnnotatedOption option,
            EnumName enumName) {
        super(enumName);
        this.option = option;
    }

    @Override
    public OptionalInt index() {
        return OptionalInt.empty();
    }

    @Override
    public AnnotatedOption annotatedMethod() {
        return option;
    }
}
