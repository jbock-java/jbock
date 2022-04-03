package net.jbock.common;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import io.jbock.util.Either;
import io.jbock.util.Eithers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Constants {

    public static final ClassName STRING = ClassName.get(String.class);

    public static final TypeName LIST_OF_STRING = ParameterizedTypeName.get(ClassName.get(List.class), STRING);

    public static final ClassName EITHER = ClassName.get(Either.class);

    public static final ClassName EITHERS = ClassName.get(Eithers.class);

    public static TypeName mapOf(TypeName keyType, TypeName valueType) {
        return ParameterizedTypeName.get(ClassName.get(Map.class), keyType, valueType);
    }

    public static Optional<String> optionalString(String s) {
        if (s.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(s);
    }

    public static <T> Function<Object, Stream<T>> instancesOf(Class<T> to) {
        return f -> to.isInstance(f) ? Stream.of(to.cast(f)) : Stream.empty();
    }
}
