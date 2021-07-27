package net.jbock.parameter;

import net.jbock.source.SourceMethod;

public final class PositionalParameter extends AbstractItem {

    // for @Parameter this is the index
    // for @Parameters, greatest index plus one
    private final int position;

    public PositionalParameter(
            SourceMethod<?> sourceMethod,
            int position) {
        super(sourceMethod);
        this.position = position;
    }

    public int position() {
        return position;
    }
}
