package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

enum OptionalPrimitive {

  INT(OptionalInt.class, Integer.class),
  LONG(OptionalLong.class, Long.class),
  DOUBLE(OptionalDouble.class, Double.class);

  private final Class<?> type;
  private final String wrappedObjectType;

  OptionalPrimitive(Class<?> type, Class<? extends Number> wrappedObjectType) {
    this.type = type;
    this.wrappedObjectType = wrappedObjectType.getCanonicalName();
  }

  CodeBlock extractExpr(ParameterSpec constructorParam) {
    return CodeBlock.of("$N.isPresent() ? $T.of($N.get()) : $T.empty()",
        constructorParam, type, constructorParam, type);
  }

  public String type() {
    return type.getCanonicalName();
  }

  public String wrappedObjectType() {
    return wrappedObjectType;
  }
}
