package net.jbock.annotated;

public final class AnnotatedParameter extends AnnotatedMethod<ExecutableParameter> {

    private AnnotatedParameter(ExecutableParameter parameter) {
        super(parameter);
    }

    static AnnotatedParameter createParameter(
            ExecutableParameter parameter) {
        return new AnnotatedParameter(parameter);
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public boolean isVarargsParameter() {
        return false;
    }

    @Override
    public String paramLabel() {
        return executable().paramLabel();
    }

    public int index() {
        return executable().index();
    }
}
