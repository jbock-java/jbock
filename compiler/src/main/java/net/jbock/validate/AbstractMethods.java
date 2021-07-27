package net.jbock.validate;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceOption;
import net.jbock.source.SourceParameter;
import net.jbock.source.SourceParameters;

import java.util.List;

class AbstractMethods {

    private final List<SourceParameter> positionalParameters;
    private final List<SourceParameters> repeatablePositionalParameters;
    private final List<SourceOption> namedOptions;

    AbstractMethods(
            List<SourceParameter> positionalParameters,
            List<SourceParameters> repeatablePositionalParameters,
            List<SourceOption> namedOptions) {
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
    }

    List<SourceParameter> positionalParameters() {
        return positionalParameters;
    }

    List<SourceParameters> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }

    List<SourceOption> namedOptions() {
        return namedOptions;
    }
}
