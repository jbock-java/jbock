package net.jbock.annotated;

import java.util.List;

public final class AnnotatedOption extends AnnotatedMethod<ExecutableOption> {

    private AnnotatedOption(ExecutableOption option) {
        super(option);
    }

    static AnnotatedOption createOption(ExecutableOption option) {
        return new AnnotatedOption(option);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isVarargsParameter() {
        return false;
    }

    @Override
    public String paramLabel() {
        return executable().paramLabel();
    }

    public List<String> names() {
        return executable().names();
    }
}
