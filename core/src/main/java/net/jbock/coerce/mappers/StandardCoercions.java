package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StandardCoercions {

  private final Map<MapMirror, CoercionFactory> coercions;

  private static StandardCoercions instance;

  private StandardCoercions(TypeTool tool) {
    Map<MapMirror, CoercionFactory> m = new HashMap<>();
    CoercionFactory[] allCoercions = new CoercionFactory[]{
        new StringCoercion(),
        SimpleCoercion.create(Integer.class, "valueOf"),
        SimpleCoercion.create(Long.class, "valueOf"),
        SimpleCoercion.create(File.class, "new"),
        SimpleCoercion.create(Character.class, CodeBlock.of("Helper::parseCharacter")),
        SimpleCoercion.create(Path.class, CodeBlock.of("$T::get", Paths.class)),
        SimpleCoercion.create(URI.class, "create"),
        SimpleCoercion.create(BigDecimal.class, "new"),
        SimpleCoercion.create(BigInteger.class, "new"),
        SimpleCoercion.create(Charset.class, "forName"),
        SimpleCoercion.create(Pattern.class, "compile"),
        SimpleCoercion.create(LocalDate.class, "parse"),
        SimpleCoercion.create(Short.class, "valueOf"),
        SimpleCoercion.create(Byte.class, "valueOf"),
        SimpleCoercion.create(Double.class, "valueOf"),
        SimpleCoercion.create(Float.class, "valueOf"),
        SimpleCoercion.create(OffsetDateTime.class, "parse"),
        SimpleCoercion.create(LocalDateTime.class, "parse"),
        SimpleCoercion.create(ZonedDateTime.class, "parse"),
        SimpleCoercion.create(Instant.class, "parse")};
    for (CoercionFactory coercion : allCoercions) {
      CoercionFactory previous = m.put(new MapMirror(coercion.mapperReturnType(tool)), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.mapperReturnType(tool),
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
