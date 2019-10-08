package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.STRING_STRING_MAP;

/**
 * Generates the *_Parser class.
 */
final class Parser {

  private static final int DEFAULT_INDENT = 7;
  private static final int DEFAULT_EXITCODE_ON_ERROR = 1;

  private static final String METHOD_NAME_PARSE_OR_EXIT = "parseOrExit";

  private final Context context;
  private final Tokenizer tokenizer;
  private final Option option;
  private final Helper helper;
  private final Impl impl;
  private final ParseResult parseResult;

  private final MethodSpec readNextMethod;
  private final MethodSpec readArgumentMethod;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out")
      .initializer("$T.out", System.class)
      .addModifiers(PRIVATE).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err")
      .initializer("$T.err", System.class)
      .addModifiers(PRIVATE).build();

  private final FieldSpec indent = FieldSpec.builder(INT, "indent")
      .initializer("$L", DEFAULT_INDENT)
      .addModifiers(PRIVATE).build();

  private final FieldSpec errorExitCode = FieldSpec.builder(INT, "errorExitCode")
      .initializer("$L", DEFAULT_EXITCODE_ON_ERROR)
      .addModifiers(PRIVATE).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_STRING_MAP, "messages")
      .addModifiers(PRIVATE).build();

  private Parser(
      Context context,
      Tokenizer tokenizer,
      Option option,
      Helper helper,
      Impl impl,
      ParseResult parseResult,
      MethodSpec readNextMethod,
      MethodSpec readArgumentMethod) {
    this.context = context;
    this.tokenizer = tokenizer;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
    this.parseResult = parseResult;
    this.readNextMethod = readNextMethod;
    this.readArgumentMethod = readArgumentMethod;
  }

  static Parser create(Context context) {
    checkOnlyOnePositionalList(context.parameters);
    checkRankConsistentWithPosition(context.parameters);
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    Option option = Option.create(context);
    Impl impl = Impl.create(context, option);
    Helper helper = Helper.create(context, option);
    ParseResult parseResult = ParseResult.create(context);
    Tokenizer builder = Tokenizer.create(context, helper);
    return new Parser(context, builder, option, helper, impl, parseResult, readNextMethod, readArgumentMethod);
  }

  private static void checkOnlyOnePositionalList(List<Param> allParams) {
    allParams.stream()
        .filter(Param::isRepeatable)
        .filter(Param::isPositional)
        .skip(1).findAny().ifPresent(p -> {
      throw p.validationError("There can only be one one repeatable positional parameter.");
    });
  }

  private static void checkRankConsistentWithPosition(List<Param> allParams) {
    int currentOrdinal = -1;
    for (Param param : allParams) {
      OptionalInt order = param.positionalOrder();
      if (!order.isPresent()) {
        continue;
      }
      if (order.getAsInt() < currentOrdinal) {
        throw param.validationError("Invalid position: Optional parameters must come " +
            "after required parameters. Repeatable parameters must come last.");
      }
      currentOrdinal = order.getAsInt();
    }
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass());
    spec.addModifiers(FINAL);
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    spec.addMethod(addPublicIfNecessary(createMethod()))
        .addMethod(addPublicIfNecessary(parseMethod()))
        .addMethod(addPublicIfNecessary(parseOrExitMethod()));
    if (context.addHelp) {
      spec.addMethod(addPublicIfNecessary(withOutputStreamMethod()));
    }
    return spec.addMethod(addPublicIfNecessary(withErrorStreamMethod()))
        .addMethod(addPublicIfNecessary(withIndentMethod()))
        .addMethod(addPublicIfNecessary(withErrorExitCodeMethod()))
        .addMethod(addPublicIfNecessary(withMessagesMethod()))
        .addMethod(addPublicIfNecessary(withResourceBundleMethod()))
        .addMethod(addPublicIfNecessary(withMessagesMethodInputStream()))
        .addType(tokenizer.define())
        .addType(impl.define())
        .addType(option.define())
        .addType(helper.define())
        .addType(OptionParser.define(context))
        .addType(FlagOptionParser.define(context))
        .addType(RegularOptionParser.define(context))
        .addType(RepeatableOptionParser.define(context))
        .addType(IndentPrinter.create(context).define())
        .addType(Messages.create(context).define())
        .addTypes(parseResult.define())
        .addField(out)
        .addField(err)
        .addField(indent)
        .addField(errorExitCode)
        .addField(messages)
        .addMethod(readArgumentMethod)
        .addMethod(readNextMethod)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec.Builder withIndentMethod() {
    ParameterSpec indentParam = builder(indent.type, indent.name).build();
    return methodBuilder("withIndent")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", indent, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder withErrorExitCodeMethod() {
    ParameterSpec errorExitCodeParam = builder(errorExitCode.type, errorExitCode.name).build();
    return methodBuilder("withErrorExitCode")
        .addParameter(errorExitCodeParam)
        .addStatement("this.$N = $N", errorExitCode, errorExitCodeParam)
        .addStatement("return this")
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder withMessagesMethod() {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    spec.beginControlFlow("if ($N != null)", messages)
        .addStatement("throw new $T($S)", IllegalStateException.class, "setting messages twice")
        .endControlFlow();
    return spec.addParameter(resourceBundleParam)
        .addStatement("this.$N = $T.requireNonNull($N)", messages, Objects.class, resourceBundleParam)
        .addStatement("return this")
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder withResourceBundleMethod() {
    ParameterSpec bundle = builder(ResourceBundle.class, "bundle").build();
    ParameterSpec map = builder(STRING_STRING_MAP, "map").build();
    ParameterSpec name = builder(STRING, "name").build();
    MethodSpec.Builder spec = methodBuilder("withResourceBundle");
    spec.addStatement("$T $N = new $T<>()", map.type, map, HashMap.class);
    spec.beginControlFlow("for ($T $N :$T.list($N.getKeys()))", STRING, name, Collections.class, bundle)
        .addStatement("$N.put($N, $N.getString($N))", map, name, bundle, name)
        .endControlFlow();
    spec.addStatement("return withMessages($N)", map);
    return spec.addParameter(bundle)
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder withMessagesMethodInputStream() {
    ParameterSpec stream = builder(InputStream.class, "stream").build();
    ParameterSpec map = builder(STRING_STRING_MAP, "map").build();
    ParameterSpec name = builder(STRING, "name").build();
    ParameterSpec properties = builder(Properties.class, "properties").build();
    ParameterSpec exception = builder(IOException.class, "exception").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    spec.beginControlFlow("if ($N == null)", stream)
        .addStatement("return withMessages($T.emptyMap())", Collections.class)
        .endControlFlow();
    spec.beginControlFlow("try");
    // BEGIN TRY BODY
    spec.addStatement("$T $N = new $T()", properties.type, properties, Properties.class)
        .addStatement("$N.load($N)", properties, stream)
        .addStatement("$T $N = new $T<>()", map.type, map, HashMap.class);
    spec.beginControlFlow("for ($T $N : $N.stringPropertyNames())", STRING, name, properties)
        .addStatement("$N.put($N, $N.getProperty($N))", map, name, properties, name)
        .endControlFlow();
    spec.addStatement("return withMessages($N)", map);
    // END TRY BODY
    spec.endControlFlow();
    spec.beginControlFlow("catch ($T $N)", exception.type, exception)
        .addStatement("throw new $T($N)", RuntimeException.class, exception)
        .endControlFlow();
    return spec.addParameter(stream)
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder withOutputStreamMethod() {
    return withPrintStreamMethod("withOutputStream", context, out);
  }

  private MethodSpec.Builder withErrorStreamMethod() {
    return withPrintStreamMethod("withErrorStream", context, err);
  }

  private static MethodSpec.Builder withPrintStreamMethod(
      String methodName, Context context, FieldSpec stream) {
    ParameterSpec param = builder(stream.type, stream.name).build();
    return methodBuilder(methodName)
        .addParameter(param)
        .addStatement("this.$N = $T.requireNonNull($N)", stream, Objects.class, param)
        .addStatement("return this")
        .returns(context.generatedClass());
  }

  private MethodSpec.Builder parseMethod() {

    ParameterSpec args = builder(STRING_ARRAY, "args")
        .build();
    MethodSpec.Builder spec = methodBuilder("parse");

    ParameterSpec paramTokenizer = builder(context.tokenizerType(), "tokenizer").build();
    ParameterSpec paramErrStream = builder(context.indentPrinterType(), "errStream").build();
    ParameterSpec paramOutStream;
    if (context.addHelp) {
      paramOutStream = builder(context.indentPrinterType(), "outStream").build();
      spec.addStatement("$T $N = new $T($N, $N)", context.indentPrinterType(), paramOutStream, context.indentPrinterType(), out, indent);
    } else {
      paramOutStream = paramErrStream;
    }
    ParameterSpec paramMessages = builder(context.messagesType(), "msg").build();
    spec.addStatement("$T $N = new $T($N, $N)", context.indentPrinterType(), paramErrStream, context.indentPrinterType(), err, indent);
    spec.addStatement("$T $N = new $T($N == null ? $T.emptyMap() : $N)", context.messagesType(), paramMessages, context.messagesType(), messages, Collections.class, messages);
    spec.addStatement("$T $N = new $T($N, $N, $N)",
        paramTokenizer.type, paramTokenizer, paramTokenizer.type, paramOutStream, paramErrStream, paramMessages);
    spec.addStatement("return $N.parse($N)", paramTokenizer, args);

    return spec.addParameter(args)
        .returns(context.parseResultType());
  }

  private MethodSpec.Builder parseOrExitMethod() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(context.parseResultType(), "result").build();
    MethodSpec.Builder spec = methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    spec.addStatement("$T $N = parse($N)", result.type, result, args);

    spec.beginControlFlow("if ($N instanceof $T)", result, context.successParseResultType())
        .addStatement("return (($T) $N).result()", context.successParseResultType(), result)
        .endControlFlow();

    spec.beginControlFlow("if ($N instanceof $T)", result, context.helpPrintedParseResultType())
        .addStatement("$T.exit(0)", System.class)
        .endControlFlow();

    spec.beginControlFlow("if ($N instanceof $T)", result, context.errorParseResultType())
        .addStatement("$T.exit($N)", System.class, errorExitCode)
        .endControlFlow();

    spec.addComment("all cases handled");
    spec.addStatement("throw new $T($S)", AssertionError.class, "never thrown");

    return spec.addParameter(args)
        .returns(TypeName.get(context.sourceElement().asType()));
  }


  private MethodSpec.Builder createMethod() {
    MethodSpec.Builder builder = methodBuilder("create");
    builder.addStatement("return new $T()", context.generatedClass());
    return builder.addModifiers(STATIC)
        .returns(context.generatedClass());
  }


  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by\n" +
        "<a href=\"https://github.com/h908714124/jbock\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>\n").build();
  }

  private MethodSpec addPublicIfNecessary(MethodSpec.Builder spec) {
    return addPublicIfNecessary(context, spec);
  }

  static MethodSpec addPublicIfNecessary(Context context, MethodSpec.Builder spec) {
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      return spec.addModifiers(PUBLIC).build();
    }
    return spec.build();
  }

  private static MethodSpec readArgumentMethod(
      MethodSpec readNextMethod) {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = builder(INT, "index").build();
    ParameterSpec isLong = builder(BOOLEAN, "isLong").build();
    MethodSpec.Builder builder = methodBuilder("readArgument");

    builder.addStatement("$T $N = $N.charAt(1) == '-'", BOOLEAN, isLong, token);
    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N && $N >= 0)", isLong, index)
        .addStatement("return $N.substring($N + 1)", token, index)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() >= 3)", isLong, token)
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

    builder.addStatement("return $N($N, $N)", readNextMethod, token, it);

    return builder.addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(STATIC, PRIVATE)
        .build();
  }

  private static MethodSpec readNextMethod() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement(CodeBlock.builder()
            .add("throw new $T($S + $N)", IllegalArgumentException.class,
                "Missing value after token: ", token)
            .build())
        .endControlFlow();

    builder.addStatement("return $N.next()", it);

    return methodBuilder("readNext")
        .addParameters(asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC, PRIVATE)
        .build();
  }
}
