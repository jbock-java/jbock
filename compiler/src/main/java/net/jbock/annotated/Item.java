package net.jbock.annotated;

import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public sealed interface Item permits Parameter, Option {

    Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    String paramLabel();

    Optional<String> descriptionKey();

    List<String> description();

    ExecutableElement method();

    String safeName();

    default List<Modifier> accessModifiers() {
        return method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .toList();
    }

    default ValidationFailure fail(String message) {
        return new ValidationFailure(message, method());
    }

    default String methodName() {
        return method().getSimpleName().toString();
    }

    default TypeMirror returnType() {
        return method().getReturnType();
    }
}
