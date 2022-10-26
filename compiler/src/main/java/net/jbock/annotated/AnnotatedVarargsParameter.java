package net.jbock.annotated;

public final class AnnotatedVarargsParameter extends AnnotatedMethod<ExecutableVarargsParameter> {

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
        return executable().paramLabel();
    }
}
