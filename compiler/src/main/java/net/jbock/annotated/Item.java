package net.jbock.annotated;

import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public sealed abstract class Item permits Parameter, Option {

    private static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final ExecutableElement method;
    private final String enumName;

    Item(ExecutableElement method,
         String enumName) {
        this.method = method;
        this.enumName = enumName;
    }

    static Item create(
            ExecutableElement method,
            Annotation annotation,
            String enumName) {
        return switch (annotation) {
            case net.jbock.Option option -> new Option(method, option, enumName);
            case net.jbock.Parameter parameter -> new Parameter(method, parameter, enumName);
            default -> throw new AssertionError("unknown annotation " + annotation);
        };
    }

    public abstract boolean isParameter();

    public abstract String paramLabel();

    public abstract Optional<String> descriptionKey();

    public abstract List<String> description();

    public final ExecutableElement method() {
        return method;
    }

    final Name simpleName() {
        return method.getSimpleName();
    }

    public final List<Modifier> accessModifiers() {
        return method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }

    public final String enumName() {
        return enumName;
    }

    public final String methodName() {
        return method.getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method.getReturnType();
    }
}
