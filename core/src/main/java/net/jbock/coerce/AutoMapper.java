package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static net.jbock.compiler.Constants.STRING;

class AutoMapper {

  private static final String NEW = "new";
  private static final String CREATE = "create";
  private static final String VALUE_OF = "valueOf";
  private static final String COMPILE = "compile";
  private static final String PARSE = "parse";

  private static Entry<Class<?>, CodeBlock> create(Class<?> clasz, String createFromString) {
    return new AbstractMap.SimpleImmutableEntry<>(clasz, CodeBlock.of("$T::" + createFromString, clasz));
  }

  private static Entry<Class<?>, CodeBlock> create(Class<?> clasz, CodeBlock mapExpr) {
    return new AbstractMap.SimpleImmutableEntry<>(clasz, mapExpr);
  }

  private static final List<Entry<Class<?>, CodeBlock>> MAPPERS = Arrays.asList(
      create(String.class, CodeBlock.of("$T.identity()", Function.class)),
      create(Integer.class, VALUE_OF),
      create(Path.class, CodeBlock.of("$T::get", Paths.class)),
      create(File.class, parseFileLambda()),
      create(URI.class, CREATE),
      create(Pattern.class, COMPILE),
      create(LocalDate.class, PARSE),
      create(Long.class, VALUE_OF),
      create(Short.class, VALUE_OF),
      create(Byte.class, VALUE_OF),
      create(Float.class, VALUE_OF),
      create(Double.class, VALUE_OF),
      create(Character.class, parseCharacterLambda()),
      create(BigInteger.class, NEW),
      create(BigDecimal.class, NEW));

  static Optional<CodeBlock> findAutoMapper(TypeTool tool, TypeMirror type) {
    TypeMirror boxed = tool.box(type);
    for (Entry<Class<?>, CodeBlock> coercion : MAPPERS) {
      if (tool.isSameType(boxed, coercion.getKey())) {
        return Optional.of(coercion.getValue());
      }
    }
    return Optional.empty();
  }

  private static CodeBlock parseFileLambda() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec f = ParameterSpec.builder(File.class, "f").build();
    return CodeBlock.builder()
        .add("$N -> {\n", s).indent()
        .add("$T $N = new $T($N);\n", File.class, f, File.class, s)
        .beginControlFlow("if (!$N.exists())", f)
        .add("throw new $T($S + $N);\n", IllegalStateException.class,
            "File does not exist: ", s)
        .endControlFlow()
        .add("return $N;\n", f)
        .unindent().add("}").build();
  }

  private static CodeBlock parseCharacterLambda() {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    return CodeBlock.builder()
        .add("$N -> {\n", s).indent()
        .beginControlFlow("if ($N.length() != 1)", s)
        .add("throw new $T($S + $N + $S);\n", IllegalArgumentException.class,
            "Not a single character: <", s, ">")
        .endControlFlow()
        .add("return $N.charAt(0);\n", s)
        .unindent().add("}").build();
  }
}
