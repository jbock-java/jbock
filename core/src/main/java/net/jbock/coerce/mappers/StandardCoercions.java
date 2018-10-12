package net.jbock.coerce.mappers;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StandardCoercions {

  private final Map<MapMirror, CoercionFactory> coercions;

  private static StandardCoercions instance;

  private StandardCoercions() {
    Map<MapMirror, CoercionFactory> m = new HashMap<>();
    CoercionFactory[] allCoercions = new CoercionFactory[]{
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
        new StringCoercion()};
    for (CoercionFactory coercion : allCoercions) {
      CoercionFactory previous = m.put(new MapMirror(coercion.trigger()), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.trigger(),
            coercion.getClass().getSimpleName(), previous.getClass().getSimpleName()));
      }
    }
    coercions = Collections.unmodifiableMap(m);
  }

  private static StandardCoercions instance() {
    if (instance == null) {
      instance = new StandardCoercions();
    }
    return instance;
  }

  public static void unset() {
    instance = null;
  }

  public static boolean containsKey(TypeMirror trigger) {
    return instance().coercions.containsKey(new MapMirror(trigger));
  }

  public static CoercionFactory get(TypeMirror trigger) {
    return instance().coercions.get(new MapMirror(trigger));
  }

  private static final class MapMirror {

    final TypeMirror typeMirror;

    final int hashCode;

    MapMirror(TypeMirror typeMirror) {
      this.typeMirror = typeMirror;
      this.hashCode = typeMirror.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MapMirror mapMirror = (MapMirror) o;
      return TypeTool.get().eql(typeMirror, mapMirror.typeMirror);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
