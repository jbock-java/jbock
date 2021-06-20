package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.either.Either;
import net.jbock.processor.SourceElement;
import net.jbock.util.StringConverter;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static net.jbock.common.Constants.STRING;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ParameterScope
public class AutoConverters {

  private static final String NEW = "new";
  private static final String CREATE = "create";
  private static final String VALUE_OF = "valueOf";
  private static final String COMPILE = "compile";
  private static final String PARSE = "parse";

  private final TypeTool tool;
  private final SourceElement sourceElement;

  @Inject
  AutoConverters(
      TypeTool tool,
      SourceElement sourceElement) {
    this.tool = tool;
    this.sourceElement = sourceElement;
  }

  private static Entry<String, Supplier<CodeBlock>> create(Class<?> autoType, String createFromString) {
    return create(autoType, CodeBlock.of("$T::" + createFromString, autoType));
  }

  private static Entry<String, Supplier<CodeBlock>> create(Class<?> autoType, CodeBlock mapExpr) {
    return create(autoType, () -> CodeBlock.of("$T.create($L)", StringConverter.class, mapExpr));
  }

  private static Entry<String, Supplier<CodeBlock>> create(Class<?> autoType, Supplier<CodeBlock> mapExpr) {
    return new SimpleImmutableEntry<>(autoType.getCanonicalName(), mapExpr);
  }

  private final List<Entry<String, Supplier<CodeBlock>>> converters = autoConverters();

  private List<Entry<String, Supplier<CodeBlock>>> autoConverters() {
    return List.of(
        create(String.class, CodeBlock.of("$T.identity()", Function.class)),
        create(Integer.class, VALUE_OF),
        create(Path.class, CodeBlock.of("$T::get", Paths.class)),
        create(File.class, autoConverterFile()),
        create(URI.class, CREATE),
        create(Pattern.class, COMPILE),
        create(LocalDate.class, PARSE),
        create(Long.class, VALUE_OF),
        create(Short.class, VALUE_OF),
        create(Byte.class, VALUE_OF),
        create(Float.class, VALUE_OF),
        create(Double.class, VALUE_OF),
        create(Character.class, autoConverterChar()),
        create(BigInteger.class, NEW),
        create(BigDecimal.class, NEW));
  }

  Either<TypeMirror, CodeBlock> findAutoConverter(TypeMirror baseType) {
    for (Entry<String, Supplier<CodeBlock>> converter : converters) {
      if (tool.isSameType(baseType, converter.getKey())) {
        return right(converter.getValue().get());
      }
    }
    return left(baseType);
  }

  private Supplier<CodeBlock> autoConverterFile() {
    return () -> CodeBlock.of("new $T()", sourceElement.converterFileExistsType());
  }

  private CodeBlock autoConverterChar() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    return CodeBlock.builder()
        .add("$N -> {\n", s)
        .indent().add(autoConverterCharBlock()).unindent()
        .add("}").build();
  }

  private CodeBlock autoConverterCharBlock() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    return CodeBlock.builder()
        .beginControlFlow("if ($N.length() != 1)", s)
        .add("throw new $T($S + $N + $S);\n", RuntimeException.class,
            "Not a single character: <", s, ">")
        .endControlFlow()
        .add("return $N.charAt(0);\n", s)
        .build();
  }
}
