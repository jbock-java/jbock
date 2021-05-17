package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.TypeTool;
import net.jbock.either.Either;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static net.jbock.compiler.Constants.STRING;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@Reusable
public class AutoConverter {

  private static final String NEW = "new";
  private static final String CREATE = "create";
  private static final String VALUE_OF = "valueOf";
  private static final String COMPILE = "compile";
  private static final String PARSE = "parse";

  private final TypeTool tool;

  @Inject
  AutoConverter(TypeTool tool) {
    this.tool = tool;
  }

  private static Entry<String, CodeBlock> create(Class<?> autoType, String createFromString) {
    return create(autoType, CodeBlock.of("$T::" + createFromString, autoType));
  }

  private static Entry<String, CodeBlock> create(Class<?> autoType, CodeBlock mapExpr) {
    return new SimpleImmutableEntry<>(autoType.getCanonicalName(), mapExpr);
  }

  private static final List<Entry<String, CodeBlock>> CONVERTERS = Arrays.asList(
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

  public Either<String, CodeBlock> findAutoConverter(TypeMirror unwrappedReturnType) {
    if (tool.isSameType(unwrappedReturnType, String.class.getCanonicalName())) {
      return right(CodeBlock.builder().build());
    }
    for (Entry<String, CodeBlock> converter : CONVERTERS) {
      if (tool.isSameType(unwrappedReturnType, converter.getKey())) {
        return right(CodeBlock.builder()
            .add(".map(")
            .add(converter.getValue())
            .add(")").build());
      }
    }
    return left("");
  }

  private static CodeBlock autoConverterFile() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec f = ParameterSpec.builder(File.class, "f").build();
    return CodeBlock.builder()
        .add("$N -> {\n", s).indent()
        .add("$T $N = new $T($N);\n", File.class, f, File.class, s)
        .beginControlFlow("if (!$N.exists())", f)
        .add("throw new $T($S + $N);\n", IllegalStateException.class,
            "File does not exist: ", s)
        .endControlFlow()
        .beginControlFlow("if (!$N.isFile())", f)
        .add("throw new $T($S + $N);\n", IllegalStateException.class,
            "Not a file: ", s)
        .endControlFlow()
        .add("return $N;\n", f)
        .unindent().add("}").build();
  }

  private static CodeBlock autoConverterChar() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    return CodeBlock.builder()
        .add("$N -> {\n", s).indent()
        .beginControlFlow("if ($N.length() != 1)", s)
        .add("throw new $T($S + $N + $S);\n", RuntimeException.class,
            "Not a single character: <", s, ">")
        .endControlFlow()
        .add("return $N.charAt(0);\n", s)
        .unindent().add("}").build();
  }
}
