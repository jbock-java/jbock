package net.jbock.coerce.mappers;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllCoercions {

  private final List<CoercionFactory> allCoercions = Arrays.asList(
      new CharsetCoercion(),
      new PatternCoercion(),
      new ObjectIntegerCoercion(),
      new PrimitiveIntCoercion(),
      new OptionalIntCoercion(),
      new ObjectLongCoercion(),
      new PrimitiveLongCoercion(),
      new OptionalDoubleCoercion(),
      new ObjectDoubleCoercion(),
      new PrimitiveDoubleCoercion(),
      new ObjectFloatCoercion(),
      new PrimitiveFloatCoercion(),
      new OptionalLongCoercion(),
      new ObjectCharacterCoercion(),
      new PrimitiveCharacterCoercion(),
      new ObjectBooleanCoercion(),
      new PrimitiveBooleanCoercion(),
      new ObjectShortCoercion(),
      new PrimitiveShortCoercion(),
      new ObjectByteCoercion(),
      new PrimitiveByteCoercion(),
      new PathCoercion(),
      new FileCoercion(),
      new URICoercion(),
      new BigDecimalCoercion(),
      new BigIntegerCoercion(),
      new LocalDateCoercion(),
      new LocalDateTimeCoercion(),
      new OffsetDateTimeCoercion(),
      new ZonedDateTimeCoercion(),
      new InstantCoercion(),
      new StringCoercion());

  private static AllCoercions instance;

  private AllCoercions() {
    Map<TypeMirror, CoercionFactory> _coercions = new HashMap<>();
    for (CoercionFactory coercion : allCoercions) {
      CoercionFactory previous = _coercions.put(coercion.trigger(), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.trigger(),
            coercion.getClass().getSimpleName(), previous.getClass().getSimpleName()));
      }
    }
  }

  public static AllCoercions instance() {
    if (instance == null) {
      instance = new AllCoercions();
    }
    return instance;
  }

  public static void unset() {
    instance = null;
  }

  public boolean containsKey(TypeMirror typeName) {
    CoercionFactory factory = get(typeName);
    return factory != null;
  }

  public CoercionFactory get(TypeMirror typeName) {
    for (CoercionFactory coercion : instance().allCoercions) {
      if (TypeTool.get().equals(typeName, coercion.trigger)) {
        return coercion;
      }
    }
    return null;
  }
}
