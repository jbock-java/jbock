package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

public final class LiftedType {

  private final TypeMirror originalType;

  private final TypeMirror liftedType;

  // how to go back from lifted to original type
  private final Function<ParameterSpec, CodeBlock> extract;

  private boolean isLifted() {
    TypeTool tool = TypeTool.get();
    return tool.isSameType(tool.box(originalType), originalType) &&
        !tool.isSameType(originalType, liftedType);
  }

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

  static LiftedType lift(TypeMirror originalType) {
    TypeTool tool = TypeTool.get();
    if (tool.isSameType(originalType, OptionalInt.class)) {
      return new LiftedType(p -> convertToPrimitiveOptional(OptionalInt.class, p),
          originalType, tool.optionalOf(Integer.class));
    }
    if (tool.isSameType(originalType, OptionalLong.class)) {
      return new LiftedType(p -> convertToPrimitiveOptional(OptionalLong.class, p),
          originalType, tool.optionalOf(Long.class));
    }
    if (tool.isSameType(originalType, OptionalDouble.class)) {
      return new LiftedType(p -> convertToPrimitiveOptional(OptionalDouble.class, p),
          originalType, tool.optionalOf(Double.class));
    }
    TypeMirror type = tool.box(originalType);
    return keep(originalType, type);
  }

  public static boolean isOptionalType(TypeMirror originalType) {
    LiftedType mirror = lift(originalType);
    if (mirror.isLifted()) {
      return true;
    }
    TypeTool tool = TypeTool.get();
    return tool.isSameErasure(mirror.liftedType, Optional.class)
        && tool.typeargs(mirror.liftedType).size() == 1;
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
