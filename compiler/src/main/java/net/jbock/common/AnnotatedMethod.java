package net.jbock.common;

import net.jbock.method.MethodAnnotation;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

public final class AnnotatedMethod {

    private final ExecutableElement sourceMethod;
    private final MethodAnnotation annotation;

    private AnnotatedMethod(ExecutableElement sourceMethod, MethodAnnotation annotation) {
        this.sourceMethod = sourceMethod;
        this.annotation = annotation;
    }

    public static AnnotatedMethod create(ExecutableElement sourceMethod, Annotation annotation) {
        return new AnnotatedMethod(sourceMethod, MethodAnnotation.create(annotation));
    }

    public ExecutableElement sourceMethod() {
        return sourceMethod;
    }

    public MethodAnnotation annotation() {
        return annotation;
    }
}
