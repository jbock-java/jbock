package net.jbock.annotated;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static net.jbock.annotated.AnnotatedOption.createOption;
import static net.jbock.annotated.AnnotatedParameter.createParameter;
import static net.jbock.annotated.AnnotatedParameters.createParameters;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public abstract class AnnotatedMethod {

    private static final AnnotationUtil ANNOTATION_UTIL = new AnnotationUtil();

    private final ExecutableElement method;
    private final List<Modifier> accessModifiers;
    private final Optional<TypeElement> converter;
    private final EnumName enumName;
    private final String paramLabel;

    AnnotatedMethod(
            ExecutableElement method,
            List<Modifier> accessModifiers,
            Optional<TypeElement> converter,
            EnumName enumName,
            String paramLabel) {
        this.method = method;
        this.accessModifiers = accessModifiers;
        this.converter = converter;
        this.enumName = enumName;
        this.paramLabel = paramLabel;
    }

    static AnnotatedMethod create(
            ExecutableElement sourceMethod,
            Annotation annotation,
            EnumName enumName) {
        checkNotNull(enumName);
        Optional<TypeElement> converter = ANNOTATION_UTIL.getConverterAttribute(sourceMethod);
        List<Modifier> accessModifiers = sourceMethod.getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
        if (annotation instanceof Option) {
            return createOption(sourceMethod, enumName,
                    converter, (Option) annotation, accessModifiers);
        }
        if (annotation instanceof Parameter) {
            return createParameter(sourceMethod, enumName,
                    converter, (Parameter) annotation, accessModifiers);
        }
        if (annotation instanceof Parameters) {
            return createParameters(sourceMethod, enumName,
                    converter, (Parameters) annotation, accessModifiers);
        }
        throw new AssertionError("expecting one of " + methodLevelAnnotations()
                + " but found: " + annotation.getClass());
    }

    public final ExecutableElement method() {
        return method;
    }

    public final List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public final Optional<TypeElement> converter() {
        return converter;
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }

    public abstract Optional<String> descriptionKey();

    public abstract boolean isParameter();

    public abstract boolean isParameters();

    public abstract List<String> description();

    abstract Optional<AnnotatedOption> asAnnotatedOption();

    abstract Optional<AnnotatedParameter> asAnnotatedParameter();

    abstract Optional<AnnotatedParameters> asAnnotatedParameters();

    public final EnumName enumName() {
        return enumName;
    }

    public final String methodName() {
        return method.getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method.getReturnType();
    }

    public final String paramLabel() {
        return paramLabel;
    }
}
