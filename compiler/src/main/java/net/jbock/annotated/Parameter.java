package net.jbock.annotated;

import net.jbock.common.SnakeName;
import net.jbock.processor.SourceElement;

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

    private final ExecutableElement method;
    private final net.jbock.Parameter parameter;
    private final String safeName;
    private final int sourceIndex; // rename to sourceIndex

    Parameter(
            SourceElement sourceElement,
            ExecutableElement method,
            net.jbock.Parameter parameter,
            String safeName,
            int sourceIndex) {
        super(sourceElement);
        this.method = method;
        this.parameter = parameter;
        this.safeName = safeName;
        this.sourceIndex = sourceIndex;
    }

    static Parameter createParameter(
            SourceElement sourceElement,
            ExecutableElement method,
            net.jbock.Parameter parameter,
            String safeName,
            int sourceIndex) {
        return new Parameter(sourceElement, method, parameter, safeName, sourceIndex);
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
    public ExecutableElement method() {
        return method;
    }

    @Override
    public String safeName() {
        return safeName;
    }

    boolean isBool() {
        return false;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }

    @Override
    public String converterAttribute() {
        return ""; // todo
    }

    public int index() {
        return parameter.index();
    }

    public int sourceIndex() {
        return sourceIndex;
    }

    private Optional<String> parameterParamLabel() {
        return optionalString(parameter.paramLabel());
    }
}
