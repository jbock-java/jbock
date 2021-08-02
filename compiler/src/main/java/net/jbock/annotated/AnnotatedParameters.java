package net.jbock.annotated;

import net.jbock.Parameters;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.jbock.common.Descriptions.optionalString;

public final class AnnotatedParameters extends AnnotatedMethod {

    private final Parameters parameters;

    private AnnotatedParameters(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Parameters parameters,
            String paramLabel,
            List<Modifier> accessModifiers) {
        super(method, accessModifiers, converter, enumName, paramLabel);
        this.parameters = parameters;
    }

    static AnnotatedParameters createParameters(
            ExecutableElement method,
            EnumName enumName,
            Optional<TypeElement> converter,
            Parameters parameters,
            List<Modifier> accessModifiers) {
        String paramLabel = optionalString(parameters.paramLabel())
                .orElseGet(() -> SnakeName.create(method.getSimpleName().toString())
                        .snake('_')
                        .toUpperCase(Locale.US));
        return new AnnotatedParameters(method, enumName, converter,
                parameters, paramLabel, accessModifiers);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isParameters() {
        return true;
    }

    @Override
    public Optional<String> descriptionKey() {
        return Descriptions.optionalString(parameters.descriptionKey());
    }

    @Override
    public List<String> description() {
        return List.of(parameters.description());
    }

    @Override
    Optional<AnnotatedOption> asAnnotatedOption() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameter> asAnnotatedParameter() {
        return Optional.empty();
    }

    @Override
    Optional<AnnotatedParameters> asAnnotatedParameters() {
        return Optional.of(this);
    }
}
