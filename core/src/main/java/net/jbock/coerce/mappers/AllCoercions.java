package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllCoercions {

  private static final List<CoercionFactory> ALL_COERCIONS = Arrays.asList(
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

  public final Map<TypeName, CoercionFactory> coercions;

  private static AllCoercions instance;

  private AllCoercions() {
    Map<TypeName, CoercionFactory> _coercions = new HashMap<>();
    for (CoercionFactory coercion : ALL_COERCIONS) {
      CoercionFactory previous = _coercions.put(coercion.trigger(), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.trigger(),
            coercion.getClass().getSimpleName(), previous.getClass().getSimpleName()));
      }
    }
    coercions = Collections.unmodifiableMap(_coercions);
  }

  private static AllCoercions instance() {
    if (instance == null) {
      instance = new AllCoercions();
    }
    return instance;
  }

  public static boolean containsKey(TypeName typeName) {
    return instance().coercions.containsKey(typeName);
  }

  public static CoercionFactory get(TypeName typeName) {
    return instance().coercions.get(typeName);
  }
}
