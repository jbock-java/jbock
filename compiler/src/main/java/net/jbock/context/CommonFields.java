package net.jbock.context;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.convert.ConvertedParameter;
import net.jbock.parameter.NamedOption;
import net.jbock.processor.SourceElement;
import net.jbock.util.ConverterError;
import net.jbock.util.NotSuccess;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_TO_STRING_MAP;
import static net.jbock.common.Constants.mapOf;

public class CommonFields {

  private static final int DEFAULT_WRAP_AFTER = 80;

  private final FieldSpec exitHook;
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

  private final FieldSpec convExError = FieldSpec.builder(ConverterError.class, "error")
      .addModifiers(FINAL)
      .build();

  private CommonFields(
      FieldSpec exitHook,
      FieldSpec optionNames,
      FieldSpec params,
      FieldSpec optionParsers) {
    this.exitHook = exitHook;
    this.optionNames = optionNames;
    this.params = params;
    this.optionParsers = optionParsers;
  }

  public static CommonFields create(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions) {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(
        ClassName.get(Consumer.class),
        ClassName.get(NotSuccess.class));
    ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add(generatedTypes.helpRequestedType()
        .map(helpRequestedType -> CodeBlock.builder()
            .add("$N ->\n", result).indent()
            .add("$T.exit($N instanceof $T ? 0 : 1)", System.class, result, helpRequestedType)
            .unindent().build())
        .orElseGet(() -> CodeBlock.of("$N -> $T.exit(1)", result, System.class)));
    FieldSpec exitHook = FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer(code.build())
        .build();
    long mapSize = namedOptions.stream()
        .map(ConvertedParameter::parameter)
        .map(NamedOption::names)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, sourceElement.itemType()), "optionNames")
        .initializer("new $T<>($L)", HashMap.class, mapSize)
        .build();
    FieldSpec paramParsers = FieldSpec.builder(ArrayTypeName.of(STRING), "params")
        .initializer("new $T[$L]", STRING, positionalParameters.regular().size())
        .build();
    FieldSpec optionParsers = FieldSpec.builder(mapOf(sourceElement.itemType(), generatedTypes.optionParserType()), "optionParsers")
        .initializer("new $T<>($T.class)", EnumMap.class, sourceElement.itemType())
        .build();
    return new CommonFields(exitHook, optionsByName, paramParsers, optionParsers);
  }

  public FieldSpec exitHook() {
    return exitHook;
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

  public FieldSpec convExError() {
    return convExError;
  }

  public FieldSpec optionParsers() {
    return optionParsers;
  }
}
