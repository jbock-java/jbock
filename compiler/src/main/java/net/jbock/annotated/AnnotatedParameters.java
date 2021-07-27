package net.jbock.annotated;

import net.jbock.Parameters;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.method.ParametersAnnotation;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceParameters;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;

public final class AnnotatedParameters extends AnnotatedMethod {

    private final Parameters parameters;

    AnnotatedParameters(ExecutableElement method, Parameters parameters) {
        super(method);
        this.parameters = parameters;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public Optional<String> descriptionKey() {
        return Descriptions.optionalString(parameters.descriptionKey());
    }

    @Override
    public Optional<String> label() {
        return Descriptions.optionalString(parameters.paramLabel());
    }

    @Override
    public boolean isParameters() {
        return true;
    }

    @Override
    public List<String> names() {
        return List.of();
    }

    @Override
    public List<String> description() {
        return List.of(parameters.description());
    }

    @Override
    public SourceMethod<?> sourceMethod(EnumName enumName, int numberOfParameters) {
        ParametersAnnotation annotation = new ParametersAnnotation(this, numberOfParameters);
        return new SourceParameters(annotation, enumName);
    }
}
