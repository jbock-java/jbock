package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.Context;
import net.jbock.compiler.Description;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
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

  private static final int CONTINUATION_INDENT_USAGE = 8;
  static final String OPTIONS_BY_NAME = "OPTIONS_BY_NAME";
  static final String SUSPICIOUS_PATTERN = "SUSPICIOUS";

  private final Context context;
  private final Description description;
  private final Impl impl;
  private final GeneratedTypes generatedTypes;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser parserState;
  private final ParseResult parseResult;

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec terminalWidth = FieldSpec.builder(INT, "terminalWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec programName;

  private final FieldSpec exitHook;

  @Inject
  GeneratedClass(
      Context context,
      Description description,
      Impl impl,
      GeneratedTypes generatedTypes,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser parserState,
      ParseResult parseResult) {
    this.context = context;
    this.description = description;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.parserState = parserState;
    this.parseResult = parseResult;
    this.exitHook = context.exitHookField();
    this.programName = FieldSpec.builder(STRING, "programName", PRIVATE)
        .initializer("$S", context.programName()).build();
  }

  public TypeSpec define() {
    Modifier[] accessModifiers = context.getAccessModifiers();
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass())
        .addMethod(parseMethod(accessModifiers))
        .addMethod(parseOrExitMethod(accessModifiers))
        .addMethod(withProgramNameMethod(accessModifiers))
        .addMethod(withTerminalWidthMethod(accessModifiers))
        .addMethod(withMessagesMethod(accessModifiers))
        .addMethod(withExitHookMethod(accessModifiers))
        .addMethod(withErrorStreamMethod(accessModifiers));
    spec.addMethod(printOnlineHelpMethod(accessModifiers))
        .addMethod(printOptionMethod())
        .addMethod(printTokensMethod())
        .addMethod(makeLinesMethod())
        .addMethod(usageMethod());
    if (!context.options().isEmpty()) {
      spec.addMethod(readOptionArgumentMethod());
      spec.addMethod(optionsByNameMethod());
      spec.addMethod(optionParsersMethod());
    }
    if (context.parameters().stream().anyMatch(Coercion::isRequired)) {
      spec.addMethod(missingRequiredMethod());
    }

    spec.addField(err);
    spec.addField(programName);
    spec.addField(terminalWidth);
    spec.addField(exitHook);
    spec.addField(messages);

    if (!context.options().isEmpty()) {
      spec.addField(FieldSpec.builder(mapOf(STRING, generatedTypes.optionType()), OPTIONS_BY_NAME)
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
      FieldSpec result = FieldSpec.builder(generatedTypes.sourceType(), "result", PRIVATE, FINAL).build();
      FieldSpec rest = FieldSpec.builder(STRING_ARRAY, "rest", PRIVATE, FINAL).build();
      spec.addType(TypeSpec.classBuilder(resultWithRestType)
          .addModifiers(accessModifiers)
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
              .addModifiers(accessModifiers)
              .addStatement("return $N", rest).build())
          .addMethod(methodBuilder("getResult")
              .returns(result.type)
              .addModifiers(accessModifiers)
              .addStatement("return $N", result).build())
          .build());
    });

    return spec.addModifiers(FINAL)
        .addModifiers(accessModifiers)
        .addJavadoc(javadoc()).build();
  }

  private MethodSpec printOnlineHelpMethod(Modifier[] accessModifiers) {
    CodeBlock.Builder code = CodeBlock.builder();
    String continuationIndent = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));

    if (description.getValue().length > 0) {
      ParameterSpec descriptionBuilder = builder(LIST_OF_STRING, "description").build();
      code.addStatement("$T $N = new $T<>()", descriptionBuilder.type, descriptionBuilder, ArrayList.class);
      for (String line : description.getValue()) {
        code.addStatement("$T.addAll($N, $S.split($S, $L))", Collections.class, descriptionBuilder, line, "\\s+", -1);
      }
      code.addStatement("printTokens($S, $N)", "", descriptionBuilder);
      code.addStatement("$N.println()", err);
    }

    code.addStatement("$N.println($S)", err, "USAGE");
    code.addStatement("printTokens($S, usage())", continuationIndent);

    if (!context.params().isEmpty()) {
      code.addStatement("$N.println()", err);
      code.addStatement("$N.println($S)", err, "PARAMETERS");
    }
    context.params().forEach(p ->
        code.addStatement("printOption($T.$L, $S, $S)",
            generatedTypes.optionType(), p.enumConstant(),
            p.parameter().bundleKey().orElse(""),
            p.sample()));
    if (!context.options().isEmpty()) {
      code.addStatement("$N.println()", err);
      code.addStatement("$N.println($S)", err, "OPTIONS");
    }
    context.options().forEach(p ->
        code.addStatement("printOption($T.$L, $S, $S)",
            generatedTypes.optionType(), p.enumConstant(),
            p.parameter().bundleKey().orElse(""),
            p.sample()));
    return methodBuilder("printOnlineHelp")
        .addModifiers(accessModifiers)
        .addCode(code.build())
        .build();
  }

  MethodSpec printOptionMethod() {
    List<Coercion<? extends AbstractParameter>> params = context.parameters();
    int totalPadding = 3;
    int width = params.stream()
        .map(Coercion::sample)
        .mapToInt(String::length).max().orElse(0) + totalPadding;
    String format = "  %1$-" + (width - 2) + "s";
    ParameterSpec messageKey = builder(STRING, "messageKey").build();
    ParameterSpec message = builder(STRING, "message").build();
    ParameterSpec option = builder(generatedTypes.optionType(), "option").build();
    ParameterSpec names = builder(STRING, "names").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec s = builder(STRING, "s").build();
    ParameterSpec namesPadded = builder(STRING, "names_padded_" + width + "_characters").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $T.format($S, $N)", namesPadded.type, namesPadded, STRING, format, names);
    code.addStatement("$T $N = $N.isEmpty() ? null : $N.get($N)", message.type, message, messageKey, messages, messageKey);
    code.addStatement("$T $N = new $T<>()", tokens.type, tokens, ArrayList.class);
    code.addStatement("$N.add($N)", tokens, namesPadded);
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
    code.addStatement("printTokens($S, $N)",
        String.join("", Collections.nCopies(width + 1, " ")), tokens);
    return methodBuilder("printOption")
        .addParameter(option)
        .addParameter(messageKey)
        .addParameter(names)
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .build();
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

  private MethodSpec withProgramNameMethod(Modifier[] accessModifiers) {
    ParameterSpec programNameParam = builder(STRING, "programName").build();
    return methodBuilder("withProgramName")
        .addParameter(programNameParam)
        .addStatement("this.$N = $N", programName, programNameParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withTerminalWidthMethod(Modifier[] accessModifiers) {
    ParameterSpec width = builder(terminalWidth.type, "width").build();
    return methodBuilder("withTerminalWidth")
        .addParameter(width)
        .addStatement("this.$1N = $2N == 0 ? this.$1N : $2N", terminalWidth, width)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withExitHookMethod(Modifier[] accessModifiers) {
    ParameterSpec param = builder(exitHook.type, exitHook.name).build();
    return methodBuilder("withExitHook")
        .addParameter(param)
        .addStatement("this.$N = $N", exitHook, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withMessagesMethod(Modifier[] accessModifiers) {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    return spec.addParameter(resourceBundleParam)
        .addStatement("this.$N = $N", messages, resourceBundleParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withErrorStreamMethod(Modifier[] accessModifiers) {
    ParameterSpec param = builder(err.type, err.name).build();
    return methodBuilder("withErrorStream")
        .addParameter(param)
        .addStatement("this.$N = $N", err, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec parseMethod(Modifier[] accessModifiers) {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();


    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> {
      if (context.parameters().stream().anyMatch(Coercion::isRequired)) {
        code.add("if ($N.length == 0)\n",
            args).indent()
            .addStatement("return new $T()", helpRequestedType)
            .unindent();
      }
      code.add("if ($1N.length == 1 && ($2S.equals($1N[0]) || $3S.equals($1N[0])))\n",
          args, "--help", "-h").indent()
          .addStatement("return new $T()", helpRequestedType)
          .unindent();
    });

    ParameterSpec state = builder(generatedTypes.statefulParserType(), "state").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec result = builder(generatedTypes.parseSuccessType(), "result").build();
    code.addStatement("$T $N = new $T()", state.type, state, state.type);
    code.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);
    code.beginControlFlow("try")
        .addStatement("$T $N = $N.parse($N)", result.type, result, state, it)
        .addStatement("return new $T($N)", generatedTypes.parsingSuccessWrapperType(), result)
        .endControlFlow();

    code.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addStatement("return new $T($N)",
            generatedTypes.parsingFailedType(), e)
        .endControlFlow();

    return MethodSpec.methodBuilder("parse").addParameter(args)
        .returns(generatedTypes.parseResultType())
        .addCode(code.build())
        .addModifiers(accessModifiers)
        .addJavadoc("This parse method has no side effects.\n" +
            "Consider {@link #parseOrExit()} instead which does standard error-handling\n" +
            "like printing error messages, and potentially shutting down the JVM.\n")
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

    List<Coercion<NamedOption>> requiredOptions = context.options().stream().filter(Coercion::isRequired).collect(Collectors.toList());
    List<Coercion<NamedOption>> optionalOptions = context.options().stream().filter(p -> !p.isRequired()).collect(Collectors.toList());

    spec.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    spec.addStatement("$N.add($S)", result, " ");
    spec.addStatement("$N.add($N)", result, programName);

    if (!optionalOptions.isEmpty()) {
      spec.addStatement("$N.add($S)", result, "[OPTION]...");
    }

    for (Coercion<NamedOption> option : requiredOptions) {
      spec.addStatement("$N.add($T.format($S, $S, $S))",
          result, STRING, "%s %s",
          option.parameter().dashedNames().get(0),
          option.enumConstant().toUpperCase(Locale.US));
    }

    for (Coercion<PositionalParameter> param : context.params()) {
      if (param.isOptional()) {
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]");
      } else if (param.isRequired()) {
        spec.addStatement("$N.add($S)", result, param.enumName().snake().toUpperCase(Locale.US));
      } else if (param.isRepeatable()) {
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]...");
      } else {
        throw new AssertionError("all cases handled (param can't be flag)");
      }
    }

    spec.addStatement("return $N", result);
    return spec.returns(LIST_OF_STRING).addModifiers(PRIVATE).build();
  }

  private MethodSpec parseOrExitMethod(Modifier[] accessModifiers) {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.addStatement("$T $N = parse($N)", result.type, result, args);

    code.add("if ($N instanceof $T)\n", result, generatedTypes.parsingSuccessWrapperType()).indent()
        .addStatement("return (($T) $N).$L()", generatedTypes.parsingSuccessWrapperType(), result, context.getSuccessResultMethodName())
        .unindent();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> code
        .beginControlFlow("if ($N instanceof $T)", result, helpRequestedType)
        .addStatement("printOnlineHelp()")
        .addStatement("$N.flush()", err)
        .addStatement("$N.accept($N)", exitHook, result)
        .addStatement("throw new $T($S)", RuntimeException.class, "help requested")
        .endControlFlow());

    code.addStatement("$N.println($S + (($T) $N).getError().getMessage())", err, "Error: ", generatedTypes.parsingFailedType(), result);
    if (context.isHelpParameterEnabled()) {
      code.addStatement("printTokens($S, usage())", String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " ")));
    } else {
      code.addStatement("printOnlineHelp()");
    }
    if (context.isHelpParameterEnabled()) {
      code.addStatement("$N.println($S + $N + $S)", err, "Try '", programName, " --help' for more information.");
    }
    code.addStatement("$N.flush()", err)
        .addStatement("$N.accept($N)", exitHook, result)
        .addStatement("throw new $T($S)", RuntimeException.class, "parsing error");

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(accessModifiers)
        .returns(generatedTypes.parseSuccessType())
        .addCode(code.build())
        .build();
  }

  private MethodSpec optionsByNameMethod() {
    ClassName optionType = generatedTypes.optionType();
    ParameterSpec result = builder(mapOf(STRING, optionType), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    long mapSize = context.options().stream()
        .map(Coercion::parameter)
        .map(NamedOption::dashedNames)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
    for (Coercion<NamedOption> namedOption : context.options()) {
      for (String dashedName : namedOption.parameter().dashedNames()) {
        code.addStatement("$N.put($S, $T.$L)", result, dashedName, generatedTypes.optionType(),
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
    ParameterSpec parsers = builder(mapOf(generatedTypes.optionType(), generatedTypes.optionParserType()), "parsers").build();

    return MethodSpec.methodBuilder("optionParsers").returns(parsers.type)
        .addCode(optionParsersMethodCode(context, generatedTypes, parsers))
        .addModifiers(PRIVATE, STATIC).build();
  }

  private CodeBlock optionParsersMethodCode(Context context, GeneratedTypes generatedTypes, ParameterSpec parsers) {
    List<Coercion<NamedOption>> options = context.options();
    if (options.isEmpty()) {
      return CodeBlock.builder().addStatement("return $T.emptyMap()", Collections.class).build();
    }
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>($T.class)", parsers.type, parsers, EnumMap.class, generatedTypes.optionType());
    for (Coercion<NamedOption> param : options) {
      String enumConstant = param.enumConstant();
      code.addStatement("$N.put($T.$L, new $T($T.$L))",
          parsers, generatedTypes.optionType(), enumConstant, optionParserType(generatedTypes, param),
          generatedTypes.optionType(), enumConstant);
    }
    code.addStatement("return $N", parsers);
    return code.build();
  }

  private static ClassName optionParserType(GeneratedTypes generatedTypes, Coercion<NamedOption> param) {
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
