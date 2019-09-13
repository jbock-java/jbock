package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

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
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StandardCoercions {

  private static final SimpleCoercion NEW = SimpleCoercion.create("new");
  private static final SimpleCoercion CREATE = SimpleCoercion.create("create");
  private static final SimpleCoercion VALUE_OF = SimpleCoercion.create("valueOf");
  private static final SimpleCoercion FOR_NAME = SimpleCoercion.create("forName");
  private static final SimpleCoercion COMPILE = SimpleCoercion.create("compile");
  private static final SimpleCoercion PARSE = SimpleCoercion.create("parse");

  private static final List<Map.Entry<Class<?>, CoercionFactory>> COERCIONS = Arrays.asList(
      new SimpleImmutableEntry<>(String.class, new StringCoercion()),
      new SimpleImmutableEntry<>(Integer.class, VALUE_OF),
      new SimpleImmutableEntry<>(Long.class, VALUE_OF),
      new SimpleImmutableEntry<>(File.class, NEW),
      new SimpleImmutableEntry<>(Character.class, SimpleCoercion.create(type -> CodeBlock.of("Helper::parseCharacter"))),
      new SimpleImmutableEntry<>(Path.class, SimpleCoercion.create(type -> CodeBlock.of("$T::get", Paths.class))),
      new SimpleImmutableEntry<>(URI.class, CREATE),
      new SimpleImmutableEntry<>(BigDecimal.class, NEW),
      new SimpleImmutableEntry<>(BigInteger.class, NEW),
      new SimpleImmutableEntry<>(Charset.class, FOR_NAME),
      new SimpleImmutableEntry<>(Pattern.class, COMPILE),
      new SimpleImmutableEntry<>(LocalDate.class, PARSE),
      new SimpleImmutableEntry<>(Short.class, VALUE_OF),
      new SimpleImmutableEntry<>(Byte.class, VALUE_OF),
      new SimpleImmutableEntry<>(Double.class, VALUE_OF),
      new SimpleImmutableEntry<>(Float.class, VALUE_OF),
      new SimpleImmutableEntry<>(OffsetDateTime.class, PARSE),
      new SimpleImmutableEntry<>(LocalDateTime.class, PARSE),
      new SimpleImmutableEntry<>(ZonedDateTime.class, PARSE),
      new SimpleImmutableEntry<>(Instant.class, PARSE));

  public static CoercionFactory get(TypeTool tool, TypeMirror mapperReturnType) {
    for (Map.Entry<Class<?>, CoercionFactory> coercion : COERCIONS) {
      if (tool.isSameType(mapperReturnType, coercion.getKey())) {
        return coercion.getValue();
      }
    }
    return null;
  }
}
