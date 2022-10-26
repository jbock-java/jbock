package net.jbock.annotated;

import net.jbock.common.SnakeName;

import java.util.Locale;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class AnnotatedVarargsParameter extends AnnotatedMethod<ExecutableVarargsParameter> {

    private final Supplier<String> paramLabel = memoize(() -> executable().paramLabel()
            .orElseGet(() -> SnakeName.create(executable().simpleName())
                    .snake('_')
                    .toUpperCase(Locale.ROOT)));

    private AnnotatedVarargsParameter(ExecutableVarargsParameter parameters) {
        super(parameters);
    }

    static AnnotatedVarargsParameter createVarargsParameter(ExecutableVarargsParameter parameters) {
        return new AnnotatedVarargsParameter(parameters);
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
