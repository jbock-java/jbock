package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

final class LiftedType {

  private final TypeMirror originalType;

  private final TypeMirror liftedType;

  // how to go back from lifted to original type
  private final Function<ParameterSpec, CodeBlock> extract;

  private LiftedType(
      Function<ParameterSpec, CodeBlock> extract,
      TypeMirror originalType,
      TypeMirror liftedType) {
    this.extract = extract;
    this.originalType = originalType;
    this.liftedType = liftedType;
  }

  private static LiftedType keep(TypeMirror originalType, TypeMirror type) {
    return new LiftedType(p -> CodeBlock.of("$T.requireNonNull($N)", Objects.class, p),
        originalType,
        type);
  }

  private static Map<Class<?>, Class<?>> OPT_MAP = createOptMap();

  private static Map<Class<?>, Class<?>> createOptMap() {
    Map<Class<?>, Class<?>> map = new LinkedHashMap<>(3);
    map.put(Integer.class, OptionalInt.class);
    map.put(Long.class, OptionalLong.class);
    map.put(Double.class, OptionalDouble.class);
    return map;
  }

  static LiftedType lift(TypeMirror originalType) {
    return lift(originalType, TypeTool.get());
  }

  // visible for testing
  static LiftedType lift(TypeMirror originalType, TypeTool tool) {
    for (Map.Entry<Class<?>, Class<?>> e : OPT_MAP.entrySet()) {
      if (tool.isSameType(originalType, e.getValue())) {
        return new LiftedType(p -> convertToPrimitiveOptional(e.getValue(), p),
            originalType, tool.optionalOf(e.getKey()));
      }
    }
    TypeMirror type = tool.box(originalType);
    return keep(originalType, type);
  }

  private static CodeBlock convertToPrimitiveOptional(Class<?> primitiveOptional, ParameterSpec p) {
    return CodeBlock.of("$N.isPresent() ? $T.of($N.get()) : $T.empty()",
        p, primitiveOptional, p, primitiveOptional);
  }

  TypeMirror liftedType() {
    return liftedType;
  }

  TypeMirror originalType() {
    return originalType;
  }

  Function<ParameterSpec, CodeBlock> extractExpr() {
    return extract;
  }
}
