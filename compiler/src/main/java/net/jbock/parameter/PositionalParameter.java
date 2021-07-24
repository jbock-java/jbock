package net.jbock.parameter;

import net.jbock.common.SnakeName;

import java.util.Locale;

public final class PositionalParameter extends AbstractItem {

    // for @Parameter this is the index
    // for @Parameters, greatest index plus one
    private final int position;

    public PositionalParameter(
            SourceMethod sourceMethod,
            int position) {
        super(sourceMethod);
        this.position = position;
    }

    @Override
    public final String paramLabel() {
        return sourceMethod().paramLabel()
                .orElseGet(() -> SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
    }

    public int position() {
        return position;
    }
}
