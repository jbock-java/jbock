package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

final class LiftedType {

  // the parameter type, but primitives are boxed, also OptionalInt becomes Optional<Integer> etc
  private final TypeMirror liftedType;

  // going back i.e. int -> Integer or Optional<Integer> -> OptionalInt
  private final Function<ParameterSpec, CodeBlock> extract;

  private LiftedType(
      Function<ParameterSpec, CodeBlock> extract,
      TypeMirror liftedType) {
    this.extract = extract;
    if (liftedType.getKind().isPrimitive()) {
      throw new AssertionError("just checking");
    }
    this.liftedType = liftedType;
  }

  private static LiftedType noLifting(TypeMirror type) {
    Function<ParameterSpec, CodeBlock> extract = p -> CodeBlock.of("$N", p);
    return new LiftedType(extract, type);
  }

  private static class OptionalMapping {
    final Class<?> optionalPrimitiveClass;
    final Class<? extends Number> boxedNumberClass;

    OptionalMapping(Class<?> optionalPrimitiveClass, Class<? extends Number> boxedNumberClass) {
      this.optionalPrimitiveClass = optionalPrimitiveClass;
      this.boxedNumberClass = boxedNumberClass;
    }

    Function<ParameterSpec, CodeBlock> extractOptionalPrimitive() {
      return optional -> CodeBlock.of(
          "$N.isPresent() ? $T.of($N.get()) : $T.empty()",
          optional, optionalPrimitiveClass, optional, optionalPrimitiveClass);
    }
  }

  private static final List<OptionalMapping> OPT_MAP = Arrays.asList(
      new OptionalMapping(OptionalInt.class, Integer.class),
      new OptionalMapping(OptionalLong.class, Long.class),
      new OptionalMapping(OptionalDouble.class, Double.class));

  // visible for testing
  static Optional<LiftedType> unwrapOptional(TypeMirror type, TypeTool tool) {
    for (OptionalMapping e : OPT_MAP) {
      if (tool.isSameType(type, e.optionalPrimitiveClass)) {
        return Optional.of(new LiftedType(
            e.extractOptionalPrimitive(),
            tool.optionalOf(e.boxedNumberClass)));
      }
    }
    Optional<TypeMirror> unwrap = tool.unwrap(Optional.class, type);
    if (!unwrap.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(noLifting(type));
  }

  TypeMirror liftedType() {
    return liftedType;
  }

  Function<ParameterSpec, CodeBlock> extractExpr() {
    return extract;
  }
}
