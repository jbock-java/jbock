package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.Parameter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.ENTRY_STRING_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;
import static net.jbock.compiler.Constants.listOf;

/**
 * Generates the *_Parser class.
 */
public final class Parser {

  private static final int DEFAULT_WRAP_AFTER = 80;
  private static final int EXITCODE_ON_ERROR = 1;

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  private final Context context;
  private final Tokenizer tokenizer;
  private final Option option;
  private final ParserState state;
  private final Impl impl;
  private final ParseResult parseResult;

  private final MethodSpec readValidArgumentMethod;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out", PRIVATE)
      .initializer("$T.out", System.class).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec runBeforeExit;

  private final FieldSpec maxLineWidth = FieldSpec.builder(INT, "maxLineWidth")
      .initializer("$L", DEFAULT_WRAP_AFTER)
      .addModifiers(PRIVATE).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages")
      .addModifiers(PRIVATE).build();

  private Parser(
      Context context,
      Tokenizer tokenizer,
      Option option,
      ParserState state,
      Impl impl,
      ParseResult parseResult,
      MethodSpec readValidArgumentMethod,
      FieldSpec runBeforeExit) {
    this.context = context;
    this.tokenizer = tokenizer;
    this.option = option;
    this.state = state;
    this.impl = impl;
    this.parseResult = parseResult;
    this.readValidArgumentMethod = readValidArgumentMethod;
    this.runBeforeExit = runBeforeExit;
  }

  public static Parser create(Context context) {
    MethodSpec readValidArgumentMethod = readValidArgumentMethod();
    Option option = Option.create(context);
    Impl impl = Impl.create(context);
    ParserState state = ParserState.create(context, option);
    ParseResult parseResult = ParseResult.create(context);
    Tokenizer builder = Tokenizer.create(context, state);
    FieldSpec runBeforeExit = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), context.parseResultType()), "runBeforeExit").addModifiers(PRIVATE)
        .initializer("r -> {}")
        .build();
    return new Parser(context, builder, option, state, impl, parseResult, readValidArgumentMethod,
        runBeforeExit);
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass())
        .addModifiers(FINAL)
        .addModifiers(context.getAccessModifiers())
        .addType(tokenizer.define())
        .addType(state.define())
        .addType(impl.define())
        .addType(option.define())
        .addType(OptionParser.define(context))
        .addType(FlagParser.define(context))
        .addType(RegularOptionParser.define(context))
        .addType(ParamParser.define(context))
        .addType(RegularParamParser.define(context))
        .addType(Messages.create(context).define())
        .addTypes(parseResult.defineResultTypes())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addMethod(createMethod())
        .addMethod(parseMethod())
        .addMethod(parseOrExitMethod())
        .addMethod(printOnlineHelpMethod())
        .addMethod(printWrapMethod())
        .addMethod(withErrorStreamMethod())
        .addMethod(maxLineWidthMethod())
        .addMethod(withMessagesMethod())
        .addMethod(withResourceBundleMethod())
        .addMethod(runBeforeExitMethod())
        .addMethod(readValidArgumentMethod);

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

    ParameterSpec args = builder(STRING_ARRAY, "args").build();

    ParameterSpec paramTokenizer = builder(context.tokenizerType(), "tokenizer").build();
    ParameterSpec paramMessages = builder(context.messagesType(), "msg").build();
    return methodBuilder("parse")
        .addStatement("$T $N = new $T($N == null ? $T.emptyMap() : $N)", context.messagesType(), paramMessages, context.messagesType(), messages, Collections.class, messages)
        .addStatement("return new $T($N).parse($N)", paramTokenizer.type, paramMessages, args)
        .addParameter(args)
        .addModifiers(context.getAccessModifiers())
        .returns(context.parseResultType())
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
      spec.addStatement("printOnlineHelp($N, $N.getSynopsis(), $N.getRows(), $N)", out, help, help, maxLineWidth);
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
        .addStatement("printOnlineHelp($N, $N.getSynopsis(), $N.getRows(), $N)", err, error, error, maxLineWidth);
    if (context.isHelpParameterEnabled()) {
      spec.addStatement("$N.println($S)", err, "Try '--help' for more information.");
    }
    spec.addStatement("$N.flush()", err);
    spec.addStatement("$N.accept($N)", runBeforeExit, result)
        .addStatement("$T.exit($L)", System.class, EXITCODE_ON_ERROR)
        .endControlFlow();

    spec.addStatement("throw new $T($S)", AssertionError.class, "all cases handled");

    return spec.addParameter(args)
        .addModifiers(context.getAccessModifiers())
        .returns(context.sourceElement())
        .build();
  }

  private MethodSpec printOnlineHelpMethod() {
    List<Parameter> params = context.parameters();
    // 2 space padding on both sides
    int totalPadding = 4;
    int width = params.stream().map(Parameter::shape).mapToInt(String::length).max().orElse(0) + totalPadding;
    String format = "  %1$-" + (width - 2) + "s";
    ParameterSpec maxLineWidth = builder(INT, "maxLineWidth").build();
    ParameterSpec synopsis = builder(STRING, "synopsis").build();
    ParameterSpec rows = builder(listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec row = builder(ENTRY_STRING_STRING, "row").build();
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec key = builder(STRING, "key").build();
    ParameterSpec keyWidth = builder(INT, "keyWidth").build();
    ParameterSpec keyFormat = builder(STRING, "keyFormat").build();
    MethodSpec.Builder spec = methodBuilder("printOnlineHelp");
    spec.addStatement("$T $N = $L", keyWidth.type, keyWidth, width)
        .addStatement("$T $N = $S", keyFormat.type, keyFormat, format)
        .addStatement("printWrap($N, 8, $S, $S + $N, $N)", printStream, "", "Usage: ", synopsis, maxLineWidth);
    spec.addStatement("$N.println()", printStream);
    spec.beginControlFlow("for ($T $N : $N)", row.type, row, rows)
        .addStatement("$T $N = $T.format($N, $N.getKey())", STRING, key, STRING, keyFormat, row)
        .addStatement("printWrap($N, $N, $N, $N.getValue(), $N)", printStream, keyWidth, key, row, maxLineWidth)
        .endControlFlow();
    return spec.addParameters(Arrays.asList(printStream, synopsis, rows, maxLineWidth))
        .addModifiers(STATIC)
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
    ParameterSpec row = builder(StringBuilder.class, "row").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec tokens = builder(ArrayTypeName.of(String.class), "tokens").build();
    ParameterSpec maxLineWidth = builder(INT, "maxLineWidth").build();
    MethodSpec.Builder spec = methodBuilder("printWrap");
    spec.beginControlFlow("if ($N.isEmpty())", input)
        .addStatement("$T $N = $N.trim()", STRING, trim, init)
        .addStatement("$N.println($N.substring(0, $N.indexOf($N)) + $N)",
            printStream, init, init, trim, trim)
        .addStatement("return")
        .endControlFlow();
    spec.addStatement("$T $N = $N.split($S, $L)", tokens.type, tokens, input, "\\s+", -1);
    spec.addStatement("$T $N = new $T($N)", row.type, row, StringBuilder.class, init);
    spec.beginControlFlow("for ($T $N : $N)", STRING, token, tokens);

    spec.beginControlFlow("if ($N.length() + $N.length() + 1 > $N)",
        token, row, maxLineWidth);
    spec.beginControlFlow("if ($N.toString().isEmpty())", row)
        .addStatement("$N.println($N)", printStream, token)
        .endControlFlow();
    spec.beginControlFlow("else")
        .addStatement("$N.println($N)", printStream, row)
        .addStatement("$N.setLength(0)", row)
        .beginControlFlow("for ($T $N = 0; $N < $N; $N++)",
            INT, i, i, continuationIndent, i)
        .addStatement("$N.append(' ')", row)
        .endControlFlow()
        .addStatement("$N.append($N)", row, token)
        .endControlFlow();
    spec.endControlFlow();
    spec.beginControlFlow("else")
        .beginControlFlow("if ($N.length() > 0 && !$T.isWhitespace($N.charAt($N.length() - 1)))",
            row, Character.class, row, row)
        .addStatement("$N.append(' ')", row)
        .endControlFlow()
        .addStatement("$N.append($N)", row, token)
        .endControlFlow();
    spec.endControlFlow();

    spec.beginControlFlow("if ($N.length() > 0)", row);
    spec.addStatement("$N.println($N)", printStream, row);
    spec.endControlFlow();
    return spec.addModifiers(STATIC)
        .addModifiers(context.getAccessModifiers())
        .addParameters(Arrays.asList(printStream, continuationIndent, init, input, maxLineWidth))
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
    return CodeBlock.builder().add("Generated by\n<a href=\"" + PROJECT_URL + "\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>\n").build();
  }

  private static MethodSpec readValidArgumentMethod() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = builder(INT, "index").build();
    ParameterSpec isLong = builder(BOOLEAN, "isLong").build();
    MethodSpec.Builder builder = methodBuilder("readValidArgument");

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
}
