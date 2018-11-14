package net.jbock.coerce.mappers;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StandardCoercions {

  private final Map<MapMirror, CoercionFactory> coercions;

  private static StandardCoercions instance;

  private StandardCoercions(TypeTool tool) {
    Map<MapMirror, CoercionFactory> m = new HashMap<>();
    CoercionFactory[] allCoercions = new CoercionFactory[]{
        new CharsetCoercion(),
        new PatternCoercion(),
        new IntegerCoercion(),
        new LongCoercion(),
        new DoubleCoercion(),
        new FloatCoercion(),
        new CharacterCoercion(),
        new BooleanCoercion(),
        new ShortCoercion(),
        new ByteCoercion(),
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
      CoercionFactory previous = m.put(new MapMirror(coercion.mapperReturnType()), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.mapperReturnType(),
            coercion.getClass().getSimpleName(), previous.getClass().getSimpleName()));
      }
    }
    for (TypeKind typeKind : Arrays.asList(
        TypeKind.INT, TypeKind.FLOAT,
        TypeKind.LONG, TypeKind.DOUBLE,
        TypeKind.BOOLEAN, TypeKind.BYTE,
        TypeKind.SHORT, TypeKind.CHAR)) {
      TypeMirror primitiveType = tool.getPrimitiveType(typeKind);
      CoercionFactory factory = m.get(new MapMirror(tool.box(primitiveType)));
      m.put(new MapMirror(primitiveType), factory);
    }
    coercions = Collections.unmodifiableMap(m);
  }

  public static void init(TypeTool tool) {
    instance = new StandardCoercions(tool);
  }

  private static StandardCoercions instance() {
    return Objects.requireNonNull(instance, "not initialized?");
  }

  public static void unset() {
    instance = null;
  }

  public static CoercionFactory get(TypeMirror mapperReturnType) {
    return instance().coercions.get(new MapMirror(mapperReturnType));
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
      return TypeTool.get().isSameType(typeMirror, mapMirror.typeMirror);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
