package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.ITERATOR_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;

/**
 * Generates the *_Parser class.
 */
public final class Parser {

  private static final int DEFAULT_INDENT = 4;
  private static final int DEFAULT_EXITCODE_ON_ERROR = 1;

  private static final String METHOD_NAME_PARSE_OR_EXIT = "parseOrExit";

  private final Context context;
  private final Tokenizer tokenizer;
  private final Option option;
  private final ParserState state;
  private final Impl impl;
  private final ParseResult parseResult;

  private final MethodSpec readNextMethod;
  private final MethodSpec readValidArgumentMethod;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out").addModifiers(PRIVATE).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err").addModifiers(PRIVATE).build();

  private final FieldSpec indent = FieldSpec.builder(INT, "indent")
      .initializer("$L", DEFAULT_INDENT)
      .addModifiers(PRIVATE).build();

  private final FieldSpec errorExitCode = FieldSpec.builder(INT, "errorExitCode")
      .initializer("$L", DEFAULT_EXITCODE_ON_ERROR)
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
      MethodSpec readNextMethod,
      MethodSpec readValidArgumentMethod) {
    this.context = context;
    this.tokenizer = tokenizer;
    this.option = option;
    this.state = state;
    this.impl = impl;
    this.parseResult = parseResult;
    this.readNextMethod = readNextMethod;
    this.readValidArgumentMethod = readValidArgumentMethod;
  }

  public static Parser create(Context context) {
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readValidArgumentMethod = readValidArgumentMethod(readNextMethod);
    Option option = Option.create(context);
    Impl impl = Impl.create(context);
    ParserState state = ParserState.create(context, option);
    ParseResult parseResult = ParseResult.create(context);
    Tokenizer builder = Tokenizer.create(context, state);
    return new Parser(context, builder, option, state, impl, parseResult, readNextMethod, readValidArgumentMethod);
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
        .addType(FlagOptionParser.define(context))
        .addType(RegularOptionParser.define(context))
        .addType(RepeatableOptionParser.define(context))
        .addType(PositionalOptionParser.define(context))
        .addType(RegularPositionalOptionParser.define(context))
        .addType(RepeatablePositionalOptionParser.define(context))
        .addType(IndentPrinter.create(context).define())
        .addType(Messages.create(context).define())
        .addTypes(parseResult.defineResultTypes())
        .addFields(Arrays.asList(out, err, indent, errorExitCode, messages))
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addMethod(createMethod())
        .addMethod(parseMethod())
        .addMethod(parseOrExitMethod())
        .addMethod(withErrorStreamMethod())
        .addMethod(withIndentMethod())
        .addMethod(withErrorExitCodeMethod())
        .addMethod(withMessagesMethod())
        .addMethod(withResourceBundleMethod())
        .addMethod(readValidArgumentMethod)
        .addMethod(readNextMethod);

    withOutputStreamMethod().ifPresent(spec::addMethod);

    return spec.addJavadoc(javadoc()).build();
  }

  private MethodSpec withIndentMethod() {
    ParameterSpec indentParam = builder(indent.type, indent.name).build();
    return methodBuilder("withIndent")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", indent, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec withErrorExitCodeMethod() {
    ParameterSpec errorExitCodeParam = builder(errorExitCode.type, errorExitCode.name).build();
    return methodBuilder("withErrorExitCode")
        .addParameter(errorExitCodeParam)
        .addStatement("this.$N = $N", errorExitCode, errorExitCodeParam)
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

  private Optional<MethodSpec> withOutputStreamMethod() {
    return context.isHelpParameterEnabled() ?
        Optional.of(withPrintStreamMethod("withOutputStream", context, out)) :
        Optional.empty();
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
    MethodSpec.Builder spec = methodBuilder("parse");

    ParameterSpec paramTokenizer = builder(context.tokenizerType(), "tokenizer").build();
    ParameterSpec paramErrStream = builder(context.indentPrinterType(), "errStream").build();
    ParameterSpec paramOutStream;
    if (context.isHelpParameterEnabled()) {
      paramOutStream = builder(context.indentPrinterType(), "outStream").build();
      spec.addStatement("$T $N = new $T($N == null ? $T.out : $N, $N)", context.indentPrinterType(), paramOutStream, context.indentPrinterType(),
          out, System.class, out, indent);
    } else {
      paramOutStream = paramErrStream;
    }
    ParameterSpec paramMessages = builder(context.messagesType(), "msg").build();
    return spec.addStatement("$T $N = new $T($N == null ? $T.out : $N, $N)", context.indentPrinterType(), paramErrStream, context.indentPrinterType(),
        err, System.class, err, indent)
        .addStatement("$T $N = new $T($N == null ? $T.emptyMap() : $N)", context.messagesType(), paramMessages, context.messagesType(), messages, Collections.class, messages)
        .addStatement("return new $T($N, $N, $N).parse($N)", paramTokenizer.type,
            paramOutStream, paramErrStream, paramMessages, args)
        .addParameter(args)
        .addModifiers(context.getAccessModifiers())
        .returns(context.parseResultType())
        .build();
  }

  private MethodSpec parseOrExitMethod() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(context.parseResultType(), "result").build();
    MethodSpec.Builder spec = methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    spec.addStatement("$T $N = parse($N)", result.type, result, args);

    spec.beginControlFlow("if ($N instanceof $T)", result, context.parsingSuccessType())
        .addStatement("return (($T) $N).result()", context.parsingSuccessType(), result)
        .endControlFlow();

    context.helpPrintedType().ifPresent(helpPrintedType ->
        spec.beginControlFlow("if ($N instanceof $T)", result, helpPrintedType)
            .addStatement("$T.exit(0)", System.class)
            .endControlFlow());

    spec.beginControlFlow("if ($N instanceof $T)", result, context.parsingFailedType())
        .addStatement("$T.exit($N)", System.class, errorExitCode)
        .endControlFlow();

    spec.addComment("all cases handled");
    spec.addStatement("throw new $T($S)", AssertionError.class, "never thrown");

    return spec.addParameter(args)
        .addModifiers(context.getAccessModifiers())
        .returns(context.sourceElement())
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
    return CodeBlock.builder().add("Generated by\n" +
        "<a href=\"https://github.com/h908714124/jbock\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>\n").build();
  }

  private static MethodSpec readValidArgumentMethod(
      MethodSpec readNextMethod) {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(ITERATOR_OF_STRING, "it").build();
    ParameterSpec index = builder(INT, "index").build();
    ParameterSpec isLong = builder(BOOLEAN, "isLong").build();
    MethodSpec.Builder builder = methodBuilder("readValidArgument");

    builder.beginControlFlow("if ($N.length() < 2)", token)
        .addStatement("throw new $T()", IllegalArgumentException.class)
        .endControlFlow();
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
    ParameterSpec it = builder(ITERATOR_OF_STRING, "it").build();
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
