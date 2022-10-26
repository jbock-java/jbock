package net.jbock.annotated;

import net.jbock.VarargsParameter;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.annotated.AnnotatedVarargsParameter.createVarargsParameter;
import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class ExecutableVarargsParameter extends Executable {

    private final VarargsParameter parameter;

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    ExecutableVarargsParameter(
            ExecutableElement method,
            VarargsParameter parameter,
            Optional<TypeElement> converter,
            String enumName) {
        super(method, converter, enumName);
        this.parameter = parameter;
    }

    @Override
    AnnotatedMethod<?> annotatedMethod() {
        return createVarargsParameter(this);
    }

    @Override
    Optional<String> descriptionKey() {
        return optionalString(parameter.descriptionKey());
    }

    @Override
    List<String> description() {
        return List.of(parameter.description());
    }

    public String paramLabel() {
        return paramLabel.get();
    }

    private Optional<String> parameterParamLabel() {
        return optionalString(parameter.paramLabel());
    }
}
