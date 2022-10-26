package net.jbock.annotated;

import net.jbock.Parameter;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.annotated.AnnotatedParameter.createParameter;
import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class ExecutableParameter extends Executable {

    private final Parameter parameter;

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));


    ExecutableParameter(
            ExecutableElement method,
            Parameter parameter,
            Optional<TypeElement> converter,
            String enumName) {
        super(method, converter, enumName);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod<?> annotatedMethod() {
        return createParameter(this);
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
    public String paramLabel() {
        return paramLabel.get();
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public boolean isVarargsParameter() {
        return false;
    }


    private Optional<String> parameterParamLabel() {
        return optionalString(parameter.paramLabel());
    }

    int index() {
        return parameter.index();
    }
}
