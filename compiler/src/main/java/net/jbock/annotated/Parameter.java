package net.jbock.annotated;

import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class Parameter implements Item {

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(methodName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final SimpleParameter simpleParameter; // todo inline
    private final int effectiveIndex; // rename to sourceIndex

    Parameter(
            SimpleParameter simpleParameter,
            int effectiveIndex) {
        this.simpleParameter = simpleParameter;
        this.effectiveIndex = effectiveIndex;
    }

    static Parameter createParameter(SimpleParameter parameter, int effectiveIndex) {
        return new Parameter(parameter, effectiveIndex);
    }

    @Override
    public Optional<String> descriptionKey() {
        return optionalString(simpleParameter.parameter().descriptionKey());
    }

    @Override
    public List<String> description() {
        return List.of(simpleParameter.parameter().description());
    }

    @Override
    public ExecutableElement method() {
        return simpleParameter.method();
    }

    @Override
    public String safeName() {
        return simpleParameter.safeName();
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    public int index() {
        return simpleParameter.parameter().index();
    }

    public int effectiveIndex() {
        return effectiveIndex;
    }

    private Optional<String> parameterParamLabel() {
        return optionalString(simpleParameter.parameter().paramLabel());
    }
}
