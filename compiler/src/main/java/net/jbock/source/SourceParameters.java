package net.jbock.source;

import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.EnumName;
import net.jbock.method.ParametersAnnotation;

public final class SourceParameters extends SourceMethod<AnnotatedParameters> {

    public SourceParameters(
            ParametersAnnotation methodAnnotation,
            EnumName enumName) {
        super(methodAnnotation, enumName);
    }
}
