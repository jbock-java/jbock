package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.function.Function;

final class LiftedType {

  private final TypeMirror liftedType;

  private final Function<ParameterSpec, CodeBlock> extract;

  private LiftedType(
      Function<ParameterSpec, CodeBlock> extract,
      TypeMirror liftedType) {
    this.extract = extract;
    this.liftedType = liftedType;
  }

  private static LiftedType extractViaNullCheck(TypeMirror type) {
    Function<ParameterSpec, CodeBlock> extract = p ->
        CodeBlock.of("$T.requireNonNull($N)", Objects.class, p);
    return new LiftedType(extract, type);
  }

  private static List<Map.Entry<Class<?>, Class<? extends Number>>> OPT_MAP = createOptMap();

  private static List<Map.Entry<Class<?>, Class<? extends Number>>> createOptMap() {
    return Arrays.asList(
        new SimpleImmutableEntry<>(OptionalInt.class, Integer.class),
        new SimpleImmutableEntry<>(OptionalLong.class, Long.class),
        new SimpleImmutableEntry<>(OptionalDouble.class, Double.class));
  }

  // visible for testing
  static LiftedType lift(TypeMirror type, TypeTool tool) {
    for (Map.Entry<Class<?>, Class<? extends Number>> e : OPT_MAP) {
      Class<?> optionalNum = e.getKey();
      if (tool.isSameType(type, optionalNum)) {
        Class<? extends Number> boxedNumber = e.getValue();
        return new LiftedType(p -> CodeBlock.of(
            "$N.isPresent() ? $T.of($N.get()) : $T.empty()",
            p, optionalNum, p, optionalNum),
            tool.optionalOf(boxedNumber));
      }
    }
    return extractViaNullCheck(tool.box(type));
  }

  TypeMirror liftedType() {
    return liftedType;
  }

  Function<ParameterSpec, CodeBlock> extractExpr() {
    return extract;
  }
}
