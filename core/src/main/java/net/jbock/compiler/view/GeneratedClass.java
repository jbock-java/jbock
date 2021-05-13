package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.ExitHookField;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Generates the *_Parser class.
 */
public final class GeneratedClass {

  private static final int DEFAULT_WRAP_AFTER = 80;

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  static final int CONTINUATION_INDENT_USAGE = 8;
  static final String OPTIONS_BY_NAME = "OPTIONS_BY_NAME";
  static final String SUSPICIOUS_PATTERN = "SUSPICIOUS";

  private final AllParameters allParameters;
  private final ParseMethod parseMethod;
  private final Impl impl;
  private final GeneratedTypes generatedTypes;
  private final GeneratedType generatedType;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser parserState;
  private final ParseResult parseResult;
  private final SourceElement sourceElement;
  private final PositionalParameters positionalParameters;
  private final NamedOptions namedOptions;
  private final AnyDescriptionKeys anyDescriptionKeys;
  private final PrintOnlineHelpMethod printOnlineHelpMethod;
  private final ExitHookField exitHookField;
  private final ParseOrExitMethod parseOrExitMethod;

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec terminalWidth = FieldSpec.builder(INT, "terminalWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec programName;

  @Inject
  GeneratedClass(
      AllParameters allParameters,
      ParseMethod parseMethod,
      GeneratedType generatedType,
      SourceElement sourceElement,
      Impl impl,
      GeneratedTypes generatedTypes,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser parserState,
      ParseResult parseResult,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions,
      AnyDescriptionKeys anyDescriptionKeys,
      PrintOnlineHelpMethod printOnlineHelpMethod,
      ExitHookField exitHookField, ParseOrExitMethod parseOrExitMethod) {
    this.parseMethod = parseMethod;
    this.generatedType = generatedType;
    this.sourceElement = sourceElement;
    this.allParameters = allParameters;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.parserState = parserState;
    this.parseResult = parseResult;
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
    this.anyDescriptionKeys = anyDescriptionKeys;
    this.printOnlineHelpMethod = printOnlineHelpMethod;
    this.programName = FieldSpec.builder(STRING, "programName", PRIVATE, FINAL)
        .initializer("$S", sourceElement.programName()).build();
    this.exitHookField = exitHookField;
    this.parseOrExitMethod = parseOrExitMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedType.type())
        .addMethod(parseMethod.define())
        .addMethod(parseOrExitMethod.define())
        .addMethod(withTerminalWidthMethod())
        .addMethod(withMessagesMethod())
        .addMethod(withExitHookMethod())
        .addMethod(withErrorStreamMethod())
        .addMethod(printOnlineHelpMethod.define())
        .addMethod(printOptionMethod())
        .addMethod(printTokensMethod())
        .addMethod(makeLinesMethod())
        .addMethod(usageMethod());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionArgumentMethod());
      spec.addMethod(optionsByNameMethod());
      spec.addMethod(optionParsersMethod());
    }
    if (allParameters.anyRequired()) {
      spec.addMethod(missingRequiredMethod());
    }

    spec.addField(err);
    spec.addField(programName);
    spec.addField(terminalWidth);
    spec.addField(exitHookField.get());
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addField(messages);
    }

    if (!namedOptions.isEmpty()) {
      spec.addField(FieldSpec.builder(mapOf(STRING, generatedType.optionType()), OPTIONS_BY_NAME)
          .initializer("optionsByName()")
          .addModifiers(PRIVATE, STATIC, FINAL)
          .build());
    }

    spec.addField(FieldSpec.builder(Pattern.class, SUSPICIOUS_PATTERN)
        .initializer("$T.compile($S)", Pattern.class, "-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build());

    spec.addType(parserState.define())
        .addType(optionEnum.define())
        .addType(impl.define())
        .addTypes(optionParser.define())
        .addTypes(parseResult.defineResultTypes());

    // move this elsewhere
    generatedTypes.parseResultWithRestType().ifPresent(resultWithRestType -> {
      FieldSpec result = FieldSpec.builder(sourceElement.typeName(), "result", PRIVATE, FINAL).build();
      FieldSpec rest = FieldSpec.builder(STRING_ARRAY, "rest", PRIVATE, FINAL).build();
      spec.addType(TypeSpec.classBuilder(resultWithRestType)
          .addModifiers(sourceElement.accessModifiers())
          .addModifiers(STATIC, FINAL)
          .addField(result)
          .addField(rest)
          .addMethod(constructorBuilder()
              .addParameter(ParameterSpec.builder(result.type, result.name).build())
              .addParameter(ParameterSpec.builder(rest.type, rest.name).build())
              .addStatement("this.$N = $N", result, result)
              .addStatement("this.$N = $N", rest, rest)
              .addModifiers(PRIVATE)
              .build())
          .addMethod(methodBuilder("getRest")
              .returns(rest.type)
              .addModifiers(sourceElement.accessModifiers())
              .addStatement("return $N", rest).build())
          .addMethod(methodBuilder("getResult")
              .returns(result.type)
              .addModifiers(sourceElement.accessModifiers())
              .addStatement("return $N", result).build())
          .build());
    });

    return spec.addModifiers(FINAL)
        .addModifiers(sourceElement.accessModifiers())
        .addJavadoc(javadoc()).build();
  }

  MethodSpec printOptionMethod() {
    ParameterSpec descriptionKey = builder(STRING, "descriptionKey").build();
    ParameterSpec message = builder(STRING, "message").build();
    ParameterSpec option = builder(generatedType.optionType(), "option").build();
    ParameterSpec names = builder(STRING, "names").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec s = builder(STRING, "s").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (allParameters.anyDescriptionKeys()) {
      code.addStatement("$T $N = $N.isEmpty() ? null : $N.get($N)", message.type, message, descriptionKey, messages, descriptionKey);
    }

    code.addStatement("$T $N = new $T<>()", tokens.type, tokens, ArrayList.class);
    code.addStatement("$N.add($N)", tokens, names);
    if (allParameters.anyDescriptionKeys()) {
      code.addStatement(CodeBlock.builder().add("$N.addAll($T.ofNullable($N)\n",
          tokens, Optional.class, message).indent()
          .add(".map($T::trim)\n", STRING)
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".map($T::asList)\n", Arrays.class)
          .add(".orElseGet(() -> $T.stream($N.description)\n", Arrays.class, option).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".collect($T.toList())))", Collectors.class)
          .unindent()
          .unindent()
          .build());
    } else {
      code.addStatement(CodeBlock.builder()
          .add("$T.stream($N.description)\n", Arrays.class, option).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".forEach($N::add)", tokens)
          .unindent()
          .build());
    }
    code.addStatement("$T $N = $T.join($S, $T.nCopies($N.length() + 1, $S))",
        STRING, continuationIndent, STRING, "", Collections.class, names, " ");
    code.addStatement("printTokens($N, $N)", continuationIndent, tokens);
    MethodSpec.Builder spec = methodBuilder("printOption")
        .addParameter(option)
        .addParameter(names)
        .addModifiers(PRIVATE)
        .addCode(code.build());
    if (allParameters.anyDescriptionKeys()) {
      spec.addParameter(descriptionKey);
    }
    return spec.build();
  }

  private MethodSpec printTokensMethod() {
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec lines = builder(LIST_OF_STRING, "lines").build();
    ParameterSpec line = builder(STRING, "line").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = makeLines($N, $N)", lines.type, lines, continuationIndent, tokens);
    code.add("for ($T $N : $N)\n", STRING, line, lines).indent()
        .addStatement("$N.println($N)", err, line)
        .unindent();
    return methodBuilder("printTokens")
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .addParameter(continuationIndent)
        .addParameter(tokens)
        .build();
  }

  private MethodSpec makeLinesMethod() {
    ParameterSpec result = builder(LIST_OF_STRING, "result").build();
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec i = builder(INT, "i").build();
    ParameterSpec fresh = builder(BOOLEAN, "fresh").build();
    ParameterSpec line = builder(StringBuilder.class, "line").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    code.addStatement("$T $N = new $T()", line.type, line, StringBuilder.class);
    code.addStatement("$T $N = $L", INT, i, 0);
    code.beginControlFlow("while ($N < $N.size())", i, tokens);
    code.addStatement("$T $N = $N.get($N)", STRING, token, tokens, i);
    code.addStatement("$T $N = $N.length() == $L", BOOLEAN, fresh, line, 0);
    code.beginControlFlow("if (!$N && $N.length() + $N.length() + 1 > $N)",
        fresh, token, line, terminalWidth);
    code.addStatement("$N.add($N.toString())", result, line);
    code.addStatement("$N.setLength(0)", line);
    code.addStatement("continue");
    code.endControlFlow();
    code.beginControlFlow("if ($N > 0)", i)
        .addStatement("$N.append($N ? $N : $S)", line, fresh, continuationIndent, " ")
        .endControlFlow();
    code.addStatement("$N.append($N)", line, token);
    code.addStatement("$N++", i);
    code.endControlFlow();

    code.beginControlFlow("if ($N.length() > 0)", line)
        .addStatement("$N.add($N.toString())", result, line)
        .endControlFlow();
    code.addStatement("return $N", result);
    return methodBuilder("makeLines")
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .addParameter(continuationIndent)
        .addParameter(tokens)
        .returns(LIST_OF_STRING)
        .build();
  }

  private MethodSpec withTerminalWidthMethod() {
    ParameterSpec width = builder(terminalWidth.type, "width").build();
    return methodBuilder("withTerminalWidth")
        .addParameter(width)
        .addStatement("this.$1N = $2N == 0 ? this.$1N : $2N", terminalWidth, width)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withExitHookMethod() {
    FieldSpec exitHook = exitHookField.get();
    ParameterSpec param = builder(exitHook.type, exitHook.name).build();
    return methodBuilder("withExitHook")
        .addParameter(param)
        .addStatement("this.$N = $N", exitHook, param)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withMessagesMethod() {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    spec.addParameter(resourceBundleParam);
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addStatement("this.$N = $N", messages, resourceBundleParam);
    } else {
      spec.addComment("no keys defined");
    }
    spec.addStatement("return this");
    return spec.returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withErrorStreamMethod() {
    ParameterSpec param = builder(err.type, err.name).build();
    return methodBuilder("withErrorStream")
        .addParameter(param)
        .addStatement("this.$N = $N", err, param)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }


  private CodeBlock javadoc() {
    String version = getClass().getPackage().getImplementationVersion();
    return CodeBlock.builder()
        .add("<h3>Generated by <a href=$S>jbock $L</a></h3>\n", PROJECT_URL, version)
        .add("<p>Use the default constructor to obtain an instance of this parser.</p>\n")
        .build();
  }

  private static MethodSpec readOptionArgumentMethod() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.add("if ($N.charAt(1) == '-' && $N.indexOf('=') >= 0)\n", token, token).indent()
        .addStatement("return $N.substring($N.indexOf('=') + 1)", token, token).unindent();

    code.add("if ($N.charAt(1) != '-' && $N.length() >= 3)\n", token, token).indent()
        .addStatement("return $N.substring(2)", token).unindent();

    code.add("if (!$N.hasNext())\n", it).indent()
        .addStatement("throw new $T($S + $N)", RuntimeException.class,
            "Missing value after token: ", token)
        .unindent();

    code.addStatement("return $N.next()", it);
    return methodBuilder("readOptionArgument")
        .addCode(code.build())
        .addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private MethodSpec usageMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("usage");

    ParameterSpec result = builder(LIST_OF_STRING, "result").build();

    spec.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    spec.addStatement("$N.add($S)", result, " ");
    spec.addStatement("$N.add($N)", result, programName);

    if (!namedOptions.optional().isEmpty()) {
      spec.addStatement("$N.add($S)", result, "[OPTION]...");
    }

    for (ConvertedParameter<NamedOption> option : namedOptions.required()) {
      spec.addStatement("$N.add($T.format($S, $S, $S))",
          result, STRING, "%s %s",
          option.parameter().names().get(0),
          option.parameter().paramLabel());
    }

    for (ConvertedParameter<PositionalParameter> param : positionalParameters.regular()) {
      if (param.isOptional()) {
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]");
      } else if (param.isRequired()) {
        spec.addStatement("$N.add($S)", result, param.enumName().snake().toUpperCase(Locale.US));
      } else {
        throw new AssertionError("all cases handled (param can't be flag)");
      }
    }

    positionalParameters.repeatable().ifPresent(param ->
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]..."));

    spec.addStatement("return $N", result);
    return spec.returns(LIST_OF_STRING).addModifiers(PRIVATE).build();
  }


  private MethodSpec optionsByNameMethod() {
    ParameterSpec result = builder(mapOf(STRING, generatedType.optionType()), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    long mapSize = namedOptions.stream()
        .map(ConvertedParameter::parameter)
        .map(NamedOption::names)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
    for (ConvertedParameter<NamedOption> namedOption : namedOptions.options()) {
      for (String dashedName : namedOption.parameter().names()) {
        code.addStatement("$N.put($S, $T.$L)", result, dashedName, generatedType.optionType(),
            namedOption.enumConstant());
      }
    }
    code.addStatement("return $N", result);

    return MethodSpec.methodBuilder("optionsByName").returns(result.type)
        .addCode(code.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private MethodSpec optionParsersMethod() {
    ParameterSpec parsers = builder(mapOf(generatedType.optionType(),
        generatedTypes.optionParserType()), "parsers").build();

    return MethodSpec.methodBuilder("optionParsers").returns(parsers.type)
        .addCode(optionParsersMethodCode(parsers))
        .addModifiers(PRIVATE, STATIC).build();
  }

  private CodeBlock optionParsersMethodCode(ParameterSpec parsers) {
    if (namedOptions.isEmpty()) {
      return CodeBlock.builder().addStatement("return $T.emptyMap()", Collections.class).build();
    }
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>($T.class)", parsers.type, parsers, EnumMap.class, generatedType.optionType());
    for (ConvertedParameter<NamedOption> param : namedOptions.options()) {
      String enumConstant = param.enumConstant();
      code.addStatement("$N.put($T.$L, new $T($T.$L))",
          parsers, generatedType.optionType(), enumConstant, optionParserType(param),
          generatedType.optionType(), enumConstant);
    }
    code.addStatement("return $N", parsers);
    return code.build();
  }

  private ClassName optionParserType(ConvertedParameter<NamedOption> param) {
    if (param.isRepeatable()) {
      return generatedTypes.repeatableOptionParserType();
    }
    if (param.isFlag()) {
      return generatedTypes.flagParserType();
    }
    return generatedTypes.regularOptionParserType();
  }

  private MethodSpec missingRequiredMethod() {
    ParameterSpec name = builder(STRING, "name").build();
    return methodBuilder("missingRequired")
        .returns(RuntimeException.class)
        .addStatement("return new $T($S + $N)", RuntimeException.class, "Missing required: ", name)
        .addParameter(name)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }
}
