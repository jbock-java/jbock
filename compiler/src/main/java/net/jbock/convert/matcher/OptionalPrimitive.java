package net.jbock.convert.matcher;

import com.squareup.javapoet.CodeBlock;

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

  CodeBlock extractExpr() {
    return CodeBlock.of(".map($1T::of).orElse($1T.empty())", type);
  }

  public String type() {
    return type.getCanonicalName();
  }

  public String wrappedObjectType() {
    return wrappedObjectType;
  }
}
