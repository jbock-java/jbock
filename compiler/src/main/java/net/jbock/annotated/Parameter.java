package net.jbock.annotated;

import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class Parameter extends Item {

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(methodName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final net.jbock.Parameter parameter;
    private final int effectiveIndex;

    Parameter(
            ExecutableElement method,
            net.jbock.Parameter parameter,
            String enumName) {
        this(method, parameter, enumName, -1);
    }

    Parameter(
            ExecutableElement method,
            net.jbock.Parameter parameter,
            String enumName,
            int effectiveIndex) {
        super(method, enumName);
        this.parameter = parameter;
        this.effectiveIndex = effectiveIndex;
    }

    Parameter withEffectiveIndex(int effectiveIndex) {
        return new Parameter(method(), parameter, safeName(), effectiveIndex);
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

    public int index() {
        return parameter.index();
    }

    public int effectiveIndex() {
        if (effectiveIndex < 0) {
            throw new AssertionError("non-negative index expected");
        }
        return effectiveIndex;
    }

    private Optional<String> parameterParamLabel() {
        return optionalString(parameter.paramLabel());
    }
}
