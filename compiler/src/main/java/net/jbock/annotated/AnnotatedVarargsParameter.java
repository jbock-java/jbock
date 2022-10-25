package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class AnnotatedVarargsParameter extends AnnotatedMethod {

    private final ExecutableVarargsParameter parameters;

    private final Supplier<String> paramLabel = memoize(() -> executable().paramLabel()
            .orElseGet(() -> SnakeName.create(executable().simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private AnnotatedVarargsParameter(
            String enumName,
            ExecutableVarargsParameter parameters) {
        super(enumName);
        this.parameters = parameters;
    }

    static AnnotatedVarargsParameter createVarargsParameter(
            ExecutableVarargsParameter parameters,
            String enumName) {
        return new AnnotatedVarargsParameter(enumName, parameters);
    }

    @Override
    ExecutableVarargsParameter executable() {
        return parameters;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isVarargsParameter() {
        return true;
    }

    @Override
    public String paramLabel() {
        return paramLabel.get();
    }
}
