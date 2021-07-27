package net.jbock.source;

import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.EnumName;
import net.jbock.method.ParameterAnnotation;

public final class SourceParameter extends SourceMethod<AnnotatedParameter> {

    public SourceParameter(
            ParameterAnnotation methodAnnotation,
            EnumName enumName) {
        super(methodAnnotation, enumName);
    }
}
