package net.jbock.annotated;

import net.jbock.common.Constants;
import net.jbock.common.SnakeName;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class VarargsParameter extends Item {

    private final Supplier<String> paramLabel = memoize(() -> parameterParamLabel()
            .orElseGet(() -> SnakeName.create(simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private final Optional<net.jbock.VarargsParameter> parameter;

    VarargsParameter(
            ExecutableElement method,
            Optional<net.jbock.VarargsParameter> parameter,
            String enumName) {
        super(method, enumName);
        this.parameter = parameter;
    }

    @Override
    public Optional<String> descriptionKey() {
        return parameter.map(net.jbock.VarargsParameter::descriptionKey).flatMap(Constants::optionalString);
    }

    @Override
    public List<String> description() {
        return parameter.map(net.jbock.VarargsParameter::description).map(List::of).orElse(List.of());
    }

    @Override
    Optional<? extends Annotation> annotation() {
        return parameter;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isVarargsParameter() {
        return true;
    }

    private Optional<String> parameterParamLabel() {
        return parameter.map(net.jbock.VarargsParameter::paramLabel).flatMap(Constants::optionalString);
    }
}
