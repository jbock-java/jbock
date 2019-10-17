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

public final class CanonicalOptional {

  // Optional<Integer> instead of OptionalInt etc
  private final TypeMirror liftedType;

  // OptionalInt -> Integer
  private final TypeMirror wrappedType;

  // the function returns an expression of the original type, like OptionalInt
  private final Function<ParameterSpec, CodeBlock> extract;

  private CanonicalOptional(
      Function<ParameterSpec, CodeBlock> extract,
      TypeMirror liftedType, TypeMirror wrappedType) {
    this.extract = extract;
    this.wrappedType = wrappedType;
    if (liftedType.getKind().isPrimitive()) {
      throw new AssertionError("just checking");
    }
    this.liftedType = liftedType;
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
  public static Optional<CanonicalOptional> unwrap(TypeMirror type, TypeTool tool) {
    for (OptionalMapping e : OPT_MAP) {
      if (tool.isSameType(type, e.optionalPrimitiveClass)) {
        return Optional.of(new CanonicalOptional(
            e.extractOptionalPrimitive(),
            tool.optionalOf(e.boxedNumberClass),
            tool.asType(e.boxedNumberClass)));
      }
    }
    return tool.unwrap(Optional.class, type)
        .map(wrapped -> new CanonicalOptional(p -> CodeBlock.of("$N", p), type, wrapped));
  }

  public TypeMirror liftedType() {
    return liftedType;
  }

  public Function<ParameterSpec, CodeBlock> extractExpr() {
    return extract;
  }

  public TypeMirror wrappedType() {
    return wrappedType;
  }
}
