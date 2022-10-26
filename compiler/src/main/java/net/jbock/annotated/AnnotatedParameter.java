package net.jbock.annotated;

public final class AnnotatedParameter extends AnnotatedMethod<ExecutableParameter> {

    private AnnotatedParameter(ExecutableParameter parameter) {
        super(parameter);
    }

    static AnnotatedParameter createParameter(
            ExecutableParameter parameter) {
        return new AnnotatedParameter(parameter);
    }

    public int index() {
        return executable().index();
    }
}
