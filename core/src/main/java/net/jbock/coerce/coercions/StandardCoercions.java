package net.jbock.coerce.coercions;

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
import java.util.function.Function;
import java.util.regex.Pattern;

public class StandardCoercions {

  private static final String NEW = "new";
  private static final String CREATE = "create";
  private static final String VALUE_OF = "valueOf";
  private static final String FOR_NAME = "forName";
  private static final String COMPILE = "compile";
  private static final String PARSE = "parse";

  private static final List<Map.Entry<Class<?>, CoercionFactory>> COERCIONS = Arrays.asList(
      new SimpleImmutableEntry<>(String.class, SimpleCoercion.create(CodeBlock.of("$T.identity()", Function.class))),
      new SimpleImmutableEntry<>(Integer.class, SimpleCoercion.create(Integer.class, VALUE_OF)),
      new SimpleImmutableEntry<>(Long.class, SimpleCoercion.create(Long.class, VALUE_OF)),
      new SimpleImmutableEntry<>(File.class, SimpleCoercion.create(File.class, NEW)),
      new SimpleImmutableEntry<>(Character.class, SimpleCoercion.create(CodeBlock.of("Helper::parseCharacter"))),
      new SimpleImmutableEntry<>(Path.class, SimpleCoercion.create(CodeBlock.of("$T::get", Paths.class))),
      new SimpleImmutableEntry<>(URI.class, SimpleCoercion.create(URI.class, CREATE)),
      new SimpleImmutableEntry<>(BigDecimal.class, SimpleCoercion.create(BigDecimal.class, NEW)),
      new SimpleImmutableEntry<>(BigInteger.class, SimpleCoercion.create(BigInteger.class, NEW)),
      new SimpleImmutableEntry<>(Charset.class, SimpleCoercion.create(Charset.class, FOR_NAME)),
      new SimpleImmutableEntry<>(Pattern.class, SimpleCoercion.create(Pattern.class, COMPILE)),
      new SimpleImmutableEntry<>(LocalDate.class, SimpleCoercion.create(LocalDate.class, PARSE)),
      new SimpleImmutableEntry<>(Short.class, SimpleCoercion.create(Short.class, VALUE_OF)),
      new SimpleImmutableEntry<>(Byte.class, SimpleCoercion.create(Byte.class, VALUE_OF)),
      new SimpleImmutableEntry<>(Double.class, SimpleCoercion.create(Double.class, VALUE_OF)),
      new SimpleImmutableEntry<>(Float.class, SimpleCoercion.create(Float.class, VALUE_OF)),
      new SimpleImmutableEntry<>(OffsetDateTime.class, SimpleCoercion.create(OffsetDateTime.class, PARSE)),
      new SimpleImmutableEntry<>(LocalDateTime.class, SimpleCoercion.create(LocalDateTime.class, PARSE)),
      new SimpleImmutableEntry<>(ZonedDateTime.class, SimpleCoercion.create(ZonedDateTime.class, PARSE)),
      new SimpleImmutableEntry<>(Instant.class, SimpleCoercion.create(Instant.class, PARSE)));

  public static CoercionFactory get(TypeTool tool, TypeMirror innerType) {
    for (Map.Entry<Class<?>, CoercionFactory> coercion : COERCIONS) {
      if (tool.isSameType(innerType, coercion.getKey())) {
        return coercion.getValue();
      }
    }
    return null;
  }
}
