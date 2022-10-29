package net.jbock.annotated;

import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class Parameter extends Item {

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final net.jbock.Parameter parameter;

    Parameter(
            ExecutableElement method,
            net.jbock.Parameter parameter,
            String enumName) {
        super(method, enumName);
        this.parameter = parameter;
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
    Annotation annotation() {
        return parameter;
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

    public int index() {
        return parameter.index();
    }

    private Optional<String> parameterParamLabel() {
        return optionalString(parameter.paramLabel());
    }
}
