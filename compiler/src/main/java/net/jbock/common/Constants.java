package net.jbock.common;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.jbock.util.Either;
import io.jbock.util.Eithers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Constants {

    public static final ClassName STRING = ClassName.get(String.class);

    public static final TypeName LIST_OF_STRING = ParameterizedTypeName.get(ClassName.get(List.class), STRING);

    public static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);

    public static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);

    public static final ClassName OPTIONAL = ClassName.get(Optional.class);

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
}
