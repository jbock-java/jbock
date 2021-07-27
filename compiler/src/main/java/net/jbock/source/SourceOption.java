package net.jbock.source;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;
import net.jbock.method.OptionAnnotation;

public final class SourceOption extends SourceMethod<AnnotatedOption> {

    public SourceOption(
            OptionAnnotation methodAnnotation,
            EnumName enumName) {
        super(methodAnnotation, enumName);
    }
}
