package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.either.Either;
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
  private final SafeElements elements;
  private final List<Entry<String, MapExpr>> converters;

  @Inject
  AutoConverters(TypeTool tool, SafeElements elements) {
    this.tool = tool;
    this.elements = elements;
    this.converters = autoConverters();
  }

  Either<TypeMirror, MapExpr> findAutoConverter(TypeMirror baseType) {
    for (Entry<String, MapExpr> converter : converters) {
      if (tool.isSameType(baseType, converter.getKey())) {
        return right(converter.getValue());
      }
    }
    return left(baseType);
  }

  private Entry<String, MapExpr> create(Class<?> autoType, String methodName) {
    return create(autoType, CodeBlock.of("$T::" + methodName, autoType));
  }

  private Entry<String, MapExpr> create(Class<?> autoType, CodeBlock mapExpr) {
    return create(autoType, CodeBlock.of("$T.create($L)", StringConverter.class, mapExpr), false);
  }

  private Entry<String, MapExpr> create(Class<?> autoType, CodeBlock code, boolean multiline) {
    String canonicalName = autoType.getCanonicalName();
    TypeMirror type = elements.getTypeElement(canonicalName)
        .orElseThrow(() -> new RuntimeException("no typeElement: " + canonicalName))
        .asType();
    return new SimpleImmutableEntry<>(canonicalName, new MapExpr(code, type, multiline));
  }

  private List<Entry<String, MapExpr>> autoConverters() {
    return List.of(
        create(String.class, CodeBlock.of("$T.identity()", Function.class)),
        create(Integer.class, VALUE_OF),
        create(Path.class, CodeBlock.of("$T::get", Paths.class)),
        create(File.class, autoConverterFile(), true),
        create(URI.class, CREATE),
        create(Pattern.class, COMPILE),
        create(LocalDate.class, PARSE),
        create(Long.class, VALUE_OF),
        create(Short.class, VALUE_OF),
        create(Byte.class, VALUE_OF),
        create(Float.class, VALUE_OF),
        create(Double.class, VALUE_OF),
        create(Character.class, autoConverterCharBlock(), true),
        create(BigInteger.class, NEW),
        create(BigDecimal.class, NEW));
  }

  private CodeBlock autoConverterCharBlock() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    return CodeBlock.builder()
        .add("if ($N.length() != 1)\n", token).indent()
        .addStatement("throw new $T($S + $N + $S)", RuntimeException.class,
            "Not a single character: <", token, ">").unindent()
        .addStatement("return $N.charAt(0)", token)
        .build();
  }

  private CodeBlock autoConverterFile() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec file = ParameterSpec.builder(File.class, "file").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T($N)", File.class, file, File.class, token);
    code.add("if (!$N.exists())\n", file).indent()
        .addStatement("throw new $T($S + $N)", IllegalStateException.class,
            "File does not exist: ", token)
        .unindent();
    code.add("if (!$N.isFile())\n", file).indent()
        .addStatement("throw new $T($S + $N)", IllegalStateException.class,
            "Not a file: ", token)
        .unindent();
    code.addStatement("return $N", file);
    return code.build();
  }
}
