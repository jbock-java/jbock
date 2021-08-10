package net.jbock.convert.match;

import com.squareup.javapoet.CodeBlock;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

enum OptionalPrimitive {

    INT(OptionalInt.class, Integer.class),
    LONG(OptionalLong.class, Long.class),
    DOUBLE(OptionalDouble.class, Double.class);

    private final Class<?> type;
    private final String numberType;

    OptionalPrimitive(Class<?> type, Class<? extends Number> numberType) {
        this.type = type;
        this.numberType = numberType.getCanonicalName();
    }

    CodeBlock extractExpr() {
        return CodeBlock.of(".map($1T::of).orElse($1T.empty())", type);
    }

    String type() {
        return type.getCanonicalName();
    }

    String numberType() {
        return numberType;
    }
}
