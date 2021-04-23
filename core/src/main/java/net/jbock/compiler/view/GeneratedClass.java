package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.coerce.Util.addBreaks;
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

  private final Context context;
  private final Impl impl;
  private final GeneratedTypes generatedTypes;
  private final OptionParser optionParser;
  private final ParamParser paramParser;
  private final OptionEnum optionEnum;
  private final StatefulParser parserState;
  private final ParseResult parseResult;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out", PRIVATE)
      .initializer("$T.out", System.class).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec maxLineWidth = FieldSpec.builder(INT, "maxLineWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec exitHook;

  @Inject
  GeneratedClass(
      Context context,
      Impl impl,
      GeneratedTypes generatedTypes,
      OptionParser optionParser,
      ParamParser paramParser,
      OptionEnum optionEnum,
      StatefulParser parserState,
      ParseResult parseResult) {
    this.context = context;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.paramParser = paramParser;
    this.optionEnum = optionEnum;
    this.parserState = parserState;
    this.parseResult = parseResult;
    this.exitHook = context.exitHookField();
  }

  public TypeSpec define() {
    Modifier[] accessModifiers = context.getAccessModifiers();
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass())
        .addMethod(parseMethod(accessModifiers))
        .addMethod(parseOrExitMethod(accessModifiers))
        .addMethod(withMaxLineWidthMethod(accessModifiers))
        .addMethod(withMessagesMethod(accessModifiers))
        .addMethod(withResourceBundleMethod(accessModifiers))
        .addMethod(withExitHookMethod(accessModifiers))
        .addMethod(withErrorStreamMethod(accessModifiers));
    if (context.isHelpParameterEnabled()) {
      spec.addMethod(withHelpStreamMethod(accessModifiers));
    }
    spec.addMethod(printOnlineHelpMethod(accessModifiers))
        .addMethod(printWrapMethod())
        .addMethod(synopsisMethod())
        .addMethod(readOptionArgumentMethod());

    if (context.isHelpParameterEnabled()) {
      spec.addField(out);
    }
    spec.addField(err);
    spec.addField(maxLineWidth);
    spec.addField(exitHook);
    spec.addField(messages);
    if (!context.options().isEmpty()) {
      spec.addField(FieldSpec.builder(mapOf(STRING, generatedTypes.optionType()), "OPTIONS_BY_NAME")
          .initializer("$T.$N()", generatedTypes.optionType(), optionEnum.optionsByNameMethod())
          .addModifiers(PRIVATE, STATIC, FINAL)
          .build());
    }

    spec.addType(parserState.define())
        .addType(impl.define())
        .addType(optionEnum.define())
        .addTypes(optionParser.define())
        .addTypes(paramParser.define())
        .addTypes(parseResult.defineResultTypes());

    // move this elsewhere
    generatedTypes.parseResultWithRestType().ifPresent(resultWithRestType -> {
      FieldSpec result = FieldSpec.builder(generatedTypes.sourceType(), "result", PRIVATE, FINAL).build();
      FieldSpec rest = FieldSpec.builder(ArrayTypeName.of(String.class), "rest", PRIVATE, FINAL).build();
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
    List<Parameter> params = context.parameters();
    // 2 space padding on both sides
    int totalPadding = 4;
    int width = params.stream().map(Parameter::sample).mapToInt(String::length).max().orElse(0) + totalPadding;
    String format = "  %1$-" + (width - 2) + "s";
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec key = builder(STRING, "key").build();
    MethodSpec.Builder spec = methodBuilder("printOnlineHelp");
    ParameterSpec option = builder(generatedTypes.optionType(), "option").build();
    ParameterSpec message = builder(STRING, "message").build();
    spec.addStatement("printWrap($N, 8, $S, $S + synopsis())", printStream, "", "Usage: ");
    spec.beginControlFlow("for ($T $N : $T.values())", generatedTypes.optionType(), option, generatedTypes.optionType())
        .addStatement("$T $N = $N.getOrDefault($N.bundleKey, $T.join($S, $N.description)).trim()",
            STRING, message, messages, option, String.class, " ", option)
        .addStatement("$T $N = $T.format($S, $N.shape)", STRING, key, STRING, format, option)
        .addStatement("printWrap($N, $L, $N, $N)", printStream, width, key, message)
        .endControlFlow();
    return spec.addParameter(printStream)
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec printWrapMethod() {
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec continuationIndent = builder(INT, "continuationIndent").build();
    ParameterSpec i = builder(INT, "i").build();
    ParameterSpec trim = builder(STRING, "trim").build();
    ParameterSpec init = builder(STRING, "init").build();
    ParameterSpec input = builder(STRING, "input").build();
    ParameterSpec sb = builder(StringBuilder.class, "sb").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec tokens = builder(ArrayTypeName.of(String.class), "tokens").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("if ($N.isEmpty())", input)
        .addStatement("$T $N = $N.trim()", STRING, trim, init)
        .addStatement("$N.println($N.substring(0, $N.indexOf($N)) + $N)",
            printStream, init, init, trim, trim)
        .addStatement("return")
        .endControlFlow();
    code.addStatement("$T $N = $N.split($S, $L)", tokens.type, tokens, input, "\\s+", -1);
    code.addStatement("$T $N = new $T($N)", sb.type, sb, StringBuilder.class, init);
    code.beginControlFlow("for ($T $N : $N)", STRING, token, tokens);

    code.beginControlFlow("if ($N.length() + $N.length() + 1 > $N)",
        token, sb, maxLineWidth);
    code.beginControlFlow("if ($N.toString().isEmpty())", sb)
        .addStatement("$N.println($N)", printStream, token)
        .endControlFlow();
    code.beginControlFlow("else")
        .addStatement("$N.println($N)", printStream, sb)
        .addStatement("$N.setLength(0)", sb)
        .add("for ($T $N = 0; $N < $N; $N++)\n",
            INT, i, i, continuationIndent, i).indent()
        .addStatement(" $N.append(' ')", sb).unindent()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    code.endControlFlow();
    code.beginControlFlow("else")
        .add("if ($N.length() > 0 && !$T.isWhitespace($N.charAt($N.length() - 1)))\n",
            sb, Character.class, sb, sb).indent()
        .addStatement("$N.append(' ')", sb).unindent()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    code.endControlFlow();

    code.add("if ($N.length() > 0)\n", sb).indent()
        .addStatement("$N.println($N)", printStream, sb).unindent();
    return methodBuilder("printWrap")
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .addParameters(Arrays.asList(printStream, continuationIndent, init, input))
        .build();
  }

  private MethodSpec withMaxLineWidthMethod(Modifier[] accessModifiers) {
    ParameterSpec indentParam = builder(maxLineWidth.type, "chars").build();
    return methodBuilder("withMaxLineWidth")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", maxLineWidth, indentParam)
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

  private MethodSpec withResourceBundleMethod(Modifier[] accessModifiers) {
    ParameterSpec bundle = builder(ResourceBundle.class, "bundle").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("return withMessages($T.list($N.getKeys()).stream()\n", Collections.class, bundle).indent()
        .addStatement(".collect($T.toMap($T.identity(), $N::getString)))", Collectors.class, Function.class, bundle).unindent();
    return methodBuilder("withResourceBundle").addParameter(bundle)
        .returns(context.generatedClass())
        .addCode(code.build())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withHelpStreamMethod(Modifier[] accessModifiers) {
    return withPrintStreamMethod("withHelpStream", context, out, accessModifiers);
  }

  private MethodSpec withErrorStreamMethod(Modifier[] accessModifiers) {
    return withPrintStreamMethod("withErrorStream", context, err, accessModifiers);
  }

  private static MethodSpec withPrintStreamMethod(
      String methodName, Context context, FieldSpec stream, Modifier[] accessModifiers) {
    ParameterSpec param = builder(stream.type, stream.name).build();
    return methodBuilder(methodName)
        .addParameter(param)
        .addStatement("this.$N = $N", stream, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec parseMethod(Modifier[] accessModifiers) {

    ParameterSpec args = builder(Constants.STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType ->
        code.add("if ($N.length >= 1 && $S.equals($N[0]))\n", args, "--help", args).indent()
            .addStatement("return new $T()", helpRequestedType)
            .unindent());

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
    return CodeBlock.builder().add("Generated by <a href=\"" + PROJECT_URL + "\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>\n").build();
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

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("synopsis");

    ParameterSpec joiner = builder(StringJoiner.class, "joiner").build();

    spec.addCode("return new $T($S)", StringJoiner.class, " ");

    List<Parameter> requiredOptions = context.options().stream().filter(Parameter::isRequired).collect(Collectors.toList());
    List<Parameter> optionalOptions = context.options().stream().filter(p -> !p.isRequired()).collect(Collectors.toList());

    spec.addCode(".add($S)", context.programName());

    if (!optionalOptions.isEmpty()) {
      spec.addCode(".add($S)", "[options...]");
    }

    for (Parameter option : requiredOptions) {
      spec.addCode(addBreaks(".add($T.format($S, $T.$L.names.get(0), $T.$L.name().toLowerCase($T.US)))"),
          String.class, "%s <%s>",
          generatedTypes.optionType(), option.enumConstant(),
          generatedTypes.optionType(), option.enumConstant(), Locale.class);
    }

    for (Parameter param : context.params()) {
      if (param.isOptional()) {
        spec.addCode("$Z.add($S)", "[<" + param.enumName().snake() + ">]");
      } else if (param.isRequired()) {
        spec.addCode("$Z.add($S)", "<" + param.enumName().snake() + ">");
      } else if (param.isRepeatable()) {
        spec.addCode("$Z.add($S)", "<" + param.enumName().snake() + ">...");
      } else {
        throw new AssertionError("all cases handled (param can't be flag)");
      }
    }

    spec.addCode("$Z.toString();\n", joiner);
    return spec.returns(STRING).addModifiers(PRIVATE).build();
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
        .addStatement("printOnlineHelp($N)", out)
        .addStatement("$N.flush()", out)
        .addStatement("$N.accept($N, 0)", exitHook, result)
        .addStatement("throw new $T($S)", RuntimeException.class, "help requested")
        .endControlFlow());

    code.addStatement("(($T) $N).getError().printStackTrace($N)", generatedTypes.parsingFailedType(), result, err);
    if (!context.isHelpParameterEnabled()) {
      code.addStatement("printOnlineHelp($N)", err);
    } else {
      code.addStatement("printWrap($N, 8, $S, $S + synopsis())", err, "", "Usage: ");
    }
    code.addStatement("$N.println($S + (($T) $N).getError().getMessage())", err, "Error: ", generatedTypes.parsingFailedType(), result);
    if (context.isHelpParameterEnabled()) {
      code.addStatement("$N.println($S)", err, "Try '--help' for more information.");
    }
    code.addStatement("$N.flush()", err)
        .addStatement("$N.accept($N, 1)", exitHook, result)
        .addStatement("throw new $T($S)", RuntimeException.class, "parsing error");

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(accessModifiers)
        .returns(generatedTypes.parseSuccessType())
        .addCode(code.build())
        .build();
  }
}
