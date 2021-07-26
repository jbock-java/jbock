package net.jbock.validate;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.AnnotatedMethod;
import net.jbock.common.Descriptions;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public enum ParameterStyle {

    OPTION(Option.class) {
        @Override
        public Optional<String> descriptionKey(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).descriptionKey());
        }

        @Override
        public Optional<String> paramLabel(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).paramLabel());
        }

        @Override
        public boolean isPositional() {
            return false;
        }

        @Override
        public OptionalInt index(AnnotatedMethod method) {
            return OptionalInt.empty();
        }

        @Override
        public List<String> names(AnnotatedMethod method) {
            return List.of(get(method.sourceMethod()).names());
        }

        @Override
        public List<String> description(AnnotatedMethod method) {
            return List.of(get(method.sourceMethod()).description());
        }

        private Option get(ExecutableElement method) {
            return method.getAnnotation(Option.class);
        }
    },

    PARAMETER(Parameter.class) {
        @Override
        public Optional<String> descriptionKey(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).descriptionKey());
        }

        @Override
        public Optional<String> paramLabel(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).paramLabel());
        }

        @Override
        public boolean isPositional() {
            return true;
        }

        @Override
        public OptionalInt index(AnnotatedMethod method) {
            return OptionalInt.of(get(method.sourceMethod()).index());
        }

        @Override
        public List<String> names(AnnotatedMethod method) {
            return List.of();
        }

        @Override
        public List<String> description(AnnotatedMethod method) {
            return List.of(get(method.sourceMethod()).description());
        }

        private Parameter get(ExecutableElement method) {
            return method.getAnnotation(Parameter.class);
        }
    },

    PARAMETERS(Parameters.class) {
        @Override
        public Optional<String> descriptionKey(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).descriptionKey());
        }

        @Override
        public Optional<String> paramLabel(AnnotatedMethod method) {
            return Descriptions.optionalString(get(method.sourceMethod()).paramLabel());
        }

        @Override
        public boolean isPositional() {
            return true;
        }

        @Override
        public OptionalInt index(AnnotatedMethod method) {
            return OptionalInt.empty();
        }

        @Override
        public List<String> names(AnnotatedMethod method) {
            return List.of();
        }

        @Override
        public List<String> description(AnnotatedMethod method) {
            return List.of(get(method.sourceMethod()).description());
        }

        private Parameters get(ExecutableElement method) {
            return method.getAnnotation(Parameters.class);
        }
    };

    private final Class<? extends Annotation> annotationClass;

    ParameterStyle(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public static ParameterStyle getStyle(ExecutableElement sourceMethod) {
        for (ParameterStyle style : values()) {
            if (sourceMethod.getAnnotation(style.annotationClass) != null) {
                return style;
            }
        }
        throw new IllegalArgumentException("no style: " + sourceMethod.getSimpleName());
    }

    public abstract Optional<String> descriptionKey(AnnotatedMethod method);

    public abstract Optional<String> paramLabel(AnnotatedMethod method);

    public abstract boolean isPositional();

    public abstract OptionalInt index(AnnotatedMethod method);

    public abstract List<String> names(AnnotatedMethod method);

    public abstract List<String> description(AnnotatedMethod method);
}
