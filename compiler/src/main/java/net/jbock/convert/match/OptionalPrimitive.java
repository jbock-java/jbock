package net.jbock.convert.match;

import com.palantir.javapoet.CodeBlock;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public enum OptionalPrimitive {

    INT(OptionalInt.class),
    LONG(OptionalLong.class),
    DOUBLE(OptionalDouble.class),
    ;

    private final Class<?> type;

    OptionalPrimitive(Class<?> type) {
        this.type = type;
    }

    public CodeBlock extractExpr() {
        return CodeBlock.of(".map($1T::of).orElse($1T.empty())", type);
    }

    public Class<?> type() {
        return type;
    }
}
