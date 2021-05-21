package net.jbock.qualifier;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;
import static net.jbock.compiler.Constants.mapOf;

public class CommonFields {

  private static final int DEFAULT_WRAP_AFTER = 80;

  private final FieldSpec exitHookField;
  private final FieldSpec optionNames;
  private final FieldSpec params;
  private final FieldSpec optionParsers;

  private final FieldSpec rest = FieldSpec.builder(LIST_OF_STRING, "rest")
      .initializer("new $T<>()", ArrayList.class)
      .build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec terminalWidth = FieldSpec.builder(INT, "terminalWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec suspiciousPattern = FieldSpec.builder(Pattern.class, "suspicious")
      .initializer("$T.compile($S)", Pattern.class, "-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+")
      .build();

  private CommonFields(
      FieldSpec exitHookField,
      FieldSpec optionNames,
      FieldSpec params,
      FieldSpec optionParsers) {
    this.exitHookField = exitHookField;
    this.optionNames = optionNames;
    this.params = params;
    this.optionParsers = optionParsers;
  }

  public static CommonFields create(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions) {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(Consumer.class),
        generatedTypes.parseResultType());
    ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add(generatedTypes.helpRequestedType()
        .map(helpRequestedType -> CodeBlock.builder()
            .add("$N ->\n", result).indent()
            .add("$T.exit($N instanceof $T ? 0 : 1)", System.class, result, helpRequestedType)
            .unindent().build())
        .orElseGet(() -> CodeBlock.of("$N -> $T.exit(1)", result, System.class)));
    FieldSpec exitHookField = FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer(code.build())
        .build();
    long mapSize = namedOptions.stream()
        .map(ConvertedParameter::parameter)
        .map(NamedOption::names)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, sourceElement.optionType()), "optionNames")
        .initializer("new $T<>($L)", HashMap.class, mapSize)
        .build();
    FieldSpec paramParsers = FieldSpec.builder(ArrayTypeName.of(STRING), "params")
        .initializer("new $T[$L]", STRING, positionalParameters.regular().size())
        .build();
    FieldSpec optionParsers = FieldSpec.builder(mapOf(sourceElement.optionType(), generatedTypes.optionParserType()), "optionParsers")
        .initializer("new $T<>($T.class)", EnumMap.class, sourceElement.optionType())
        .build();
    return new CommonFields(exitHookField, optionsByName, paramParsers, optionParsers);
  }

  public FieldSpec exitHook() {
    return exitHookField;
  }

  public FieldSpec err() {
    return err;
  }

  public FieldSpec terminalWidth() {
    return terminalWidth;
  }

  public FieldSpec messages() {
    return messages;
  }

  public FieldSpec suspiciousPattern() {
    return suspiciousPattern;
  }

  public FieldSpec optionNames() {
    return optionNames;
  }

  public FieldSpec rest() {
    return rest;
  }

  public FieldSpec params() {
    return params;
  }

  public FieldSpec optionParsers() {
    return optionParsers;
  }
}
