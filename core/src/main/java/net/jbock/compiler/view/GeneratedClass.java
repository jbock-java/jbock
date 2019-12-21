package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.Parameter;

import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Consumer;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.coerce.Util.addBreaks;
import static net.jbock.compiler.Constants.ENTRY_STRING_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;

/**
 * Generates the *_Parser class.
 */
public final class GeneratedClass {

  private static final int DEFAULT_WRAP_AFTER = 80;
  private static final int EXITCODE_ON_ERROR = 1;

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  private final Context context;
  private final Option option;
  private final ParserState state;
  private final Impl impl;
  private final ParseResult parseResult;

  private final MethodSpec readOptionArgumentMethod;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out", PRIVATE)
      .initializer("$T.out", System.class).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec maxLineWidth = FieldSpec.builder(INT, "maxLineWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec runBeforeExit;

  private GeneratedClass(
      Context context,
      Option option,
      ParserState state,
      Impl impl,
      ParseResult parseResult,
      MethodSpec readOptionArgumentMethod,
      FieldSpec runBeforeExit) {
    this.context = context;
    this.option = option;
    this.state = state;
    this.impl = impl;
    this.parseResult = parseResult;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.runBeforeExit = runBeforeExit;
  }

  public static GeneratedClass create(Context context) {
    MethodSpec readOptionArgumentMethod = readOptionArgumentMethod();
    Option option = Option.create(context);
    Impl impl = Impl.create(context);
    ParserState state = ParserState.create(context, option);
    ParseResult parseResult = ParseResult.create(context);
    FieldSpec runBeforeExit = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), context.parseResultType()), "runBeforeExit").addModifiers(PRIVATE)
        .initializer("r -> {}")
        .build();
    return new GeneratedClass(context, option, state, impl, parseResult, readOptionArgumentMethod,
        runBeforeExit);
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass())
        .addModifiers(FINAL)
        .addModifiers(context.getAccessModifiers())
        .addType(state.define())
        .addType(impl.define())
        .addType(option.define())
        .addType(OptionParser.define(context))
        .addType(FlagParser.define(context))
        .addType(RegularOptionParser.define(context))
        .addType(ParamParser.define(context))
        .addType(RegularParamParser.define(context))
        .addTypes(parseResult.defineResultTypes())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addMethod(createMethod())
        .addMethod(parseMethod())
        .addMethod(parseOrExitMethod())
        .addMethod(parseMethodOverloadIterator())
        .addMethod(buildRowsMethod())
        .addMethod(printOnlineHelpMethod())
        .addMethod(printWrapMethod())
        .addMethod(withErrorStreamMethod())
        .addMethod(maxLineWidthMethod())
        .addMethod(withMessagesMethod())
        .addMethod(withResourceBundleMethod())
        .addMethod(runBeforeExitMethod())
        .addMethod(readOptionArgumentMethod)
        .addMethod(synopsisMethod());

    if (context.isHelpParameterEnabled()) {
      spec.addMethod(withOutputStreamMethod());
      spec.addField(out);
    }
    spec.addFields(Arrays.asList(err, maxLineWidth, runBeforeExit, messages));
    return spec.addJavadoc(javadoc()).build();
  }

  private MethodSpec maxLineWidthMethod() {
    ParameterSpec indentParam = builder(maxLineWidth.type, "chars").build();
    return methodBuilder("maxLineWidth")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", maxLineWidth, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec runBeforeExitMethod() {
    ParameterSpec param = builder(runBeforeExit.type, runBeforeExit.name).build();
    return methodBuilder("runBeforeExit")
        .addParameter(param)
        .addStatement("this.$N = $N", runBeforeExit, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec withMessagesMethod() {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    return spec.addParameter(resourceBundleParam)
        .addStatement("this.$N = $T.requireNonNull($N)", messages, Objects.class, resourceBundleParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec withResourceBundleMethod() {
    ParameterSpec bundle = builder(ResourceBundle.class, "bundle").build();
    ParameterSpec map = builder(STRING_TO_STRING_MAP, "map").build();
    ParameterSpec name = builder(STRING, "name").build();
    MethodSpec.Builder spec = methodBuilder("withResourceBundle");
    spec.addStatement("$T $N = new $T<>()", map.type, map, HashMap.class);
    spec.beginControlFlow("for ($T $N : $T.list($N.getKeys()))", STRING, name, Collections.class, bundle)
        .addStatement("$N.put($N, $N.getString($N))", map, name, bundle, name)
        .endControlFlow();
    spec.addStatement("return withMessages($N)", map);
    return spec.addParameter(bundle)
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec withOutputStreamMethod() {
    return withPrintStreamMethod("withOutputStream", context, out);
  }

  private MethodSpec withErrorStreamMethod() {
    return withPrintStreamMethod("withErrorStream", context, err);
  }

  private static MethodSpec withPrintStreamMethod(
      String methodName, Context context, FieldSpec stream) {
    ParameterSpec param = builder(stream.type, stream.name).build();
    return methodBuilder(methodName)
        .addParameter(param)
        .addStatement("this.$N = $T.requireNonNull($N)", stream, Objects.class, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec args = builder(Constants.STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    context.helpRequestedType().ifPresent(helpRequestedType ->
        spec.beginControlFlow("if ($N.length >= 1 && $S.equals($N[0]))", args, "--help", args)
            .addStatement("return new $T()", helpRequestedType)
            .endControlFlow());

    spec.beginControlFlow("try")
        .addStatement("return new $T(parse($T.asList($N).iterator()))", context.parsingSuccessType(), Arrays.class, args)
        .endControlFlow();

    spec.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addStatement("return new $T($N)",
            context.parsingFailedType(), e)
        .endControlFlow();

    return spec.addParameter(args)
        .returns(context.parseResultType())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec parseMethodOverloadIterator() {

    ParameterSpec stateParam = builder(context.parserStateType(), "state").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec optionParam = builder(context.optionType(), "option").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec positionParam = builder(INT, "position").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse")
        .addParameter(it)
        .returns(context.sourceType());

    spec.addStatement("$T $N = $L", positionParam.type, positionParam, 0);
    spec.addStatement("$T $N = new $T()", stateParam.type, stateParam, stateParam.type);

    // begin parsing loop
    spec.beginControlFlow("while ($N.hasNext())", it);

    spec.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.allowEscape()) {
      ParameterSpec t = builder(STRING, "t").build();
      spec.beginControlFlow("if ($S.equals($N))", "--", token);

      spec.beginControlFlow("while ($N.hasNext())", it)
          .addStatement("$T $N = $N.next()", STRING, t, it)
          .beginControlFlow("if ($N >= $N.$N.size())", positionParam, stateParam, state.positionalParsersField())
          .addStatement(throwInvalidOptionStatement(t, "Excess param"))
          .endControlFlow()
          .addStatement("$N += $N.$N.get($N).read($N)", positionParam, stateParam, state.positionalParsersField(), positionParam, t)
          .endControlFlow()
          .addStatement("return $N.build()", stateParam);

      spec.endControlFlow();
    }

    spec.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, stateParam, state.tryReadOption(), token);

    spec.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N.get($N).read($N, $N, $N)", stateParam, state.parsersField(), optionParam, optionParam, token, it)
        .addStatement("continue")
        .endControlFlow();

    // handle unknown token
    spec.beginControlFlow("if (!$N.isEmpty() && $N.charAt(0) == '-')", token, token)
        .addStatement(throwInvalidOptionStatement(token, "Invalid option"))
        .endControlFlow();

    spec.beginControlFlow("if ($N >= $N.$N.size())", positionParam, stateParam, state.positionalParsersField())
        .addStatement(throwInvalidOptionStatement(token, "Excess param"))
        .endControlFlow()
        .addStatement("$N += $N.$N.get($N).read($N)", positionParam, stateParam, state.positionalParsersField(), positionParam, token);

    // end parsing loop
    spec.endControlFlow();

    spec.addStatement("return $N.build()", stateParam);
    return spec.build();
  }

  private static CodeBlock throwInvalidOptionStatement(ParameterSpec token, String message) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            message + ": ", token)
        .build();
  }

  private MethodSpec parseOrExitMethod() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(context.parseResultType(), "result").build();
    MethodSpec.Builder spec = methodBuilder("parseOrExit");

    spec.addStatement("$T $N = parse($N)", result.type, result, args);

    spec.beginControlFlow("if ($N instanceof $T)", result, context.parsingSuccessType())
        .addStatement("return (($T) $N).getResult()", context.parsingSuccessType(), result)
        .endControlFlow();

    context.helpRequestedType().ifPresent(helpRequestedType -> {
      spec.beginControlFlow("if ($N instanceof $T)", result, helpRequestedType);
      ParameterSpec help = builder(helpRequestedType, "helpResult").build();
      spec.addStatement("$T $N = ($T) $N", help.type, help, help.type, result);
      spec.addStatement("printOnlineHelp($N)", out);
      spec.addStatement("$N.flush()", out);
      spec.addStatement("$N.accept($N)", runBeforeExit, result)
          .addStatement("$T.exit(0)", System.class);
      spec.endControlFlow();
    });

    ParameterSpec error = builder(context.parsingFailedType(), "errorResult").build();
    spec.beginControlFlow("if ($N instanceof $T)", result, context.parsingFailedType())
        .addStatement("$T $N = ($T) $N", error.type, error, error.type, result)
        .addStatement("$N.getError().printStackTrace($N)", error, err)
        .addStatement("$N.println($S + $N.getError().getMessage())", err, "Error: ", error)
        .addStatement("printOnlineHelp($N)", err);
    if (context.isHelpParameterEnabled()) {
      spec.addStatement("$N.println($S)", err, "Try '--help' for more information.");
    }
    spec.addStatement("$N.flush()", err);
    spec.addStatement("$N.accept($N)", runBeforeExit, result)
        .addStatement("$T.exit($L)", System.class, EXITCODE_ON_ERROR)
        .endControlFlow();

    spec.addStatement("throw new $T($S)", RuntimeException.class, "all cases handled");

    return spec.addParameter(args)
        .addModifiers(context.getAccessModifiers())
        .returns(context.sourceType())
        .build();
  }

  private MethodSpec buildRowsMethod() {
    ParameterSpec rows = builder(Constants.listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec optionParam = builder(context.optionType(), "option").build();
    ParameterSpec message = builder(STRING, "message").build();
    return MethodSpec.methodBuilder("buildRows")
        .returns(rows.type)
        .addStatement("$T $N = new $T<>()", rows.type, rows, ArrayList.class)
        .beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type)
        .addStatement("$T $N = $N.getOrDefault($N.bundleKey, $T.join($S, $N.description)).trim()",
            STRING, message, messages, optionParam, String.class, " ", optionParam)
        .addStatement("$N.add(new $T($N.shape, $N))", rows, ParameterizedTypeName.get(ClassName.get(AbstractMap.SimpleImmutableEntry.class), STRING, STRING),
            optionParam, message)
        .endControlFlow()
        .addStatement("return $N", rows)
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec printOnlineHelpMethod() {
    List<Parameter> params = context.parameters();
    // 2 space padding on both sides
    int totalPadding = 4;
    int width = params.stream().map(Parameter::shape).mapToInt(String::length).max().orElse(0) + totalPadding;
    String format = "  %1$-" + (width - 2) + "s";
    ParameterSpec row = builder(ENTRY_STRING_STRING, "row").build();
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec key = builder(STRING, "key").build();
    MethodSpec.Builder spec = methodBuilder("printOnlineHelp");
    spec.addStatement("printWrap($N, 8, $S, $S + synopsis())", printStream, "", "Usage: ");
    spec.addStatement("$N.println()", printStream);
    spec.beginControlFlow("for ($T $N : buildRows())", row.type, row)
        .addStatement("$T $N = $T.format($S, $N.getKey())", STRING, key, STRING, format, row)
        .addStatement("printWrap($N, $L, $N, $N.getValue())", printStream, width, key, row)
        .endControlFlow();
    return spec.addParameter(printStream)
        .addModifiers(context.getAccessModifiers())
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
    MethodSpec.Builder spec = methodBuilder("printWrap");
    spec.beginControlFlow("if ($N.isEmpty())", input)
        .addStatement("$T $N = $N.trim()", STRING, trim, init)
        .addStatement("$N.println($N.substring(0, $N.indexOf($N)) + $N)",
            printStream, init, init, trim, trim)
        .addStatement("return")
        .endControlFlow();
    spec.addStatement("$T $N = $N.split($S, $L)", tokens.type, tokens, input, "\\s+", -1);
    spec.addStatement("$T $N = new $T($N)", sb.type, sb, StringBuilder.class, init);
    spec.beginControlFlow("for ($T $N : $N)", STRING, token, tokens);

    spec.beginControlFlow("if ($N.length() + $N.length() + 1 > $N)",
        token, sb, maxLineWidth);
    spec.beginControlFlow("if ($N.toString().isEmpty())", sb)
        .addStatement("$N.println($N)", printStream, token)
        .endControlFlow();
    spec.beginControlFlow("else")
        .addStatement("$N.println($N)", printStream, sb)
        .addStatement("$N.setLength(0)", sb)
        .beginControlFlow("for ($T $N = 0; $N < $N; $N++)",
            INT, i, i, continuationIndent, i)
        .addStatement("$N.append(' ')", sb)
        .endControlFlow()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    spec.endControlFlow();
    spec.beginControlFlow("else")
        .beginControlFlow("if ($N.length() > 0 && !$T.isWhitespace($N.charAt($N.length() - 1)))",
            sb, Character.class, sb, sb)
        .addStatement("$N.append(' ')", sb)
        .endControlFlow()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    spec.endControlFlow();

    spec.beginControlFlow("if ($N.length() > 0)", sb);
    spec.addStatement("$N.println($N)", printStream, sb);
    spec.endControlFlow();
    return spec.addModifiers(context.getAccessModifiers())
        .addParameters(Arrays.asList(printStream, continuationIndent, init, input))
        .build();
  }

  private MethodSpec createMethod() {
    MethodSpec.Builder builder = methodBuilder("create");
    builder.addStatement("return new $T()", context.generatedClass());
    return builder.addModifiers(STATIC)
        .addModifiers(context.getAccessModifiers())
        .returns(context.generatedClass())
        .build();
  }

  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by <a href=\"" + PROJECT_URL + "\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>").build();
  }

  private static MethodSpec readOptionArgumentMethod() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = builder(INT, "index").build();
    ParameterSpec isLong = builder(BOOLEAN, "isLong").build();
    MethodSpec.Builder builder = methodBuilder("readOptionArgument");

    builder.addStatement("$T $N = $N.charAt(1) == '-'", BOOLEAN, isLong, token);
    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N && $N >= 0)", isLong, index)
        .addStatement("return $N.substring($N + 1)", token, index)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() >= 3)", isLong, token)
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement(CodeBlock.builder()
            .add("throw new $T($S + $N)", IllegalArgumentException.class,
                "Missing value after token: ", token)
            .build())
        .endControlFlow();

    return builder.addStatement("return $N.next()", it)
        .addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(STATIC, PRIVATE)
        .build();
  }

  private MethodSpec synopsisMethod() {
    CodeBlock.Builder code = CodeBlock.builder();
    ParameterSpec joiner = builder(StringJoiner.class, "joiner").build();

    code.add("return new $T($S)", StringJoiner.class, " ");

    Map<Boolean, List<Parameter>> partitionedOptions = context.parameters().stream()
        .filter(Parameter::isNotPositional)
        .collect(partitioningBy(Parameter::isRequired));

    List<Parameter> requiredNonpos = partitionedOptions.get(true);
    List<Parameter> optionalNonpos = partitionedOptions.get(false);

    List<Parameter> positional = context.parameters().stream()
        .filter(Parameter::isPositional)
        .collect(toList());

    code.add(".add($S)", context.programName());

    if (!optionalNonpos.isEmpty()) {
      code.add(".add($S)", "[options...]");
    }

    for (Parameter param : requiredNonpos) {
      code.add(addBreaks(".add($T.format($S, $T.$L.names.get(0), $T.$L.name().toLowerCase($T.US)))"),
          String.class, "%s <%s>",
          context.optionType(), param.enumConstant(),
          context.optionType(), param.enumConstant(), Locale.class);
    }

    for (Parameter param : positional) {
      if (param.isOptional()) {
        code.add("$Z.add($S)", "[<" + param.enumConstantLower() + ">]");
      } else if (param.isRequired()) {
        code.add("$Z.add($S)", "<" + param.enumConstantLower() + ">");
      } else if (param.isRepeatable()) {
        code.add("$Z.add($S)", "<" + param.enumConstantLower() + ">...");
      } else {
        throw new AssertionError("all cases handled (repeatable can't be flag)");
      }
    }

    code.add("$Z.toString();\n", joiner);
    MethodSpec.Builder result = MethodSpec.methodBuilder("synopsis")
        .addCode(code.build())
        .returns(STRING);

    return result.addModifiers(context.getAccessModifiers()).build();
  }
}
