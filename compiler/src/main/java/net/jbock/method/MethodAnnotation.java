package net.jbock.method;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.Annotations;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class MethodAnnotation {

    public abstract Optional<String> descriptionKey();

    public abstract Optional<String> paramLabel();

    public final boolean isPositional() {
        return isParameter() || isParameters();
    }

    public abstract boolean isParameters();

    public abstract boolean isParameter();

    public abstract OptionalInt index();

    public abstract List<String> names();

    public abstract List<String> description();

    public static MethodAnnotation create(Annotation annotation) {
        if (annotation instanceof Option) {
            return new OptionAnnotation((Option) annotation);
        }
        if (annotation instanceof Parameter) {
            return new ParameterAnnotation((Parameter) annotation);
        }
        if (annotation instanceof Parameters) {
            return new ParametersAnnotation((Parameters) annotation);
        }
        throw new AssertionError("expecting one of " +
                Annotations.methodLevelAnnotations() +
                " but found: " + annotation);
    }
}
