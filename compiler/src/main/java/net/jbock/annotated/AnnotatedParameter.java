package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.jbock.common.Descriptions.optionalString;

public final class AnnotatedParameter extends AnnotatedMethod {

    private final Parameter parameter;

    private AnnotatedParameter(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Parameter parameter,
            String paramLabel,
            List<Modifier> accessModifiers) {
        super(method, accessModifiers, converter, enumName, paramLabel);
        this.parameter = parameter;
    }

    static AnnotatedParameter createParameter(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Parameter parameter,
            List<Modifier> accessModifiers) {
        String paramLabel = optionalString(parameter.paramLabel())
                .orElseGet(() -> SnakeName.create(method.getSimpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedParameter(method, enumName, converter,
                parameter, paramLabel, accessModifiers);
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public boolean isParameters() {
        return false;
    }

    @Override
    public Optional<String> descriptionKey() {
        return optionalString(parameter.descriptionKey());
    }

    @Override
    public List<String> description() {
        return List.of(parameter.description());
    }

    @Override
    Optional<AnnotatedOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameter> asAnnotatedParameter() {
        return Optional.of(this);
    }

    @Override
    Optional<AnnotatedParameters> asAnnotatedParameters() {
        return Optional.empty();
    }

    public int index() {
        return parameter.index();
    }
}
