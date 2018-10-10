package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.io.PrintStream;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOf;

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

  private final FieldSpec resourceBundle = FieldSpec.builder(ResourceBundle.class, "resourceBundle")
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
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    Option option = Option.create(context);
    Impl impl = Impl.create(context, option);
    Helper helper = Helper.create(context, option);
    ParseResult parseResult = ParseResult.create(context);
    Tokenizer builder = Tokenizer.create(context, helper);
    return new Parser(context, builder, option, helper, impl, parseResult, readNextMethod, readArgumentMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass);
    if (context.sourceType.getModifiers().contains(PUBLIC)) {
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
        .addMethod(addPublicIfNecessary(withResourceBundleMethod()))
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
        .addType(parseResult.define())
        .addField(out)
        .addField(err)
        .addField(indent)
        .addField(errorExitCode)
        .addField(resourceBundle)
        .addMethod(readArgumentMethod)
        .addMethod(readNextMethod)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec.Builder withIndentMethod() {
    ParameterSpec indentParam = ParameterSpec.builder(indent.type, indent.name).build();
    return MethodSpec.methodBuilder("withIndent")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", indent, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder withErrorExitCodeMethod() {
    ParameterSpec errorExitCodeParam = ParameterSpec.builder(errorExitCode.type, errorExitCode.name).build();
    return MethodSpec.methodBuilder("withErrorExitCode")
        .addParameter(errorExitCodeParam)
        .addStatement("this.$N = $N", errorExitCode, errorExitCodeParam)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder withResourceBundleMethod() {
    ParameterSpec resourceBundleParam = ParameterSpec.builder(resourceBundle.type, resourceBundle.name).build();
    return MethodSpec.methodBuilder("withResourceBundle")
        .addParameter(resourceBundleParam)
        .addStatement("this.$N = $N", resourceBundle, resourceBundleParam)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder withOutputStreamMethod() {
    return withPrintStreamMethod("withOutputStream", context, out);
  }

  private MethodSpec.Builder withErrorStreamMethod() {
    return withPrintStreamMethod("withErrorStream", context, err);
  }

  private static MethodSpec.Builder withPrintStreamMethod(
      String methodName, Context context, FieldSpec stream) {
    ParameterSpec param = ParameterSpec.builder(stream.type, stream.name).build();
    return MethodSpec.methodBuilder(methodName)
        .addParameter(param)
        .addStatement("this.$N = $N", stream, param)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder parseMethod() {

    ParameterSpec args = ParameterSpec.builder(STRING_ARRAY, "args")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    ParameterSpec paramTokenizer = ParameterSpec.builder(context.tokenizerType(), "tokenizer").build();
    ParameterSpec paramErrStream = ParameterSpec.builder(context.indentPrinterType(), "errStream").build();
    ParameterSpec paramOutStream;
    if (context.addHelp) {
      paramOutStream = ParameterSpec.builder(context.indentPrinterType(), "outStream").build();
      spec.addStatement("$T $N = new $T($N, $N)", context.indentPrinterType(), paramOutStream, context.indentPrinterType(), out, indent);
    } else {
      paramOutStream = paramErrStream;
    }
    ParameterSpec paramMessages = ParameterSpec.builder(context.messagesType(), "messages").build();
    spec.addStatement("$T $N = new $T($N, $N)", context.indentPrinterType(), paramErrStream, context.indentPrinterType(), err, indent);
    spec.addStatement("$T $N = new $T($N)", context.messagesType(), paramMessages, context.messagesType(), resourceBundle);
    spec.addStatement("$T $N = new $T($N, $N, $N)",
        paramTokenizer.type, paramTokenizer, paramTokenizer.type, paramOutStream, paramErrStream, paramMessages);
    spec.addStatement("return $N.parse($N)", paramTokenizer, args);

    return spec.addParameter(args)
        .returns(context.parseResultType());
  }

  private MethodSpec.Builder parseOrExitMethod() {

    ParameterSpec args = ParameterSpec.builder(STRING_ARRAY, "args")
        .build();
    ParameterSpec result = ParameterSpec.builder(optionalOf(TypeName.get(context.sourceType.asType())), "result")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    spec.addStatement("$T $N = parse($N)", context.parseResultType(), result, args);

    spec.beginControlFlow("if ($N.$N != null)", result, parseResult.result)
        .addStatement("return $N.$N", result, parseResult.result)
        .endControlFlow();

    spec.addStatement("$T.exit($N.$N ? 0 : $N)", System.class, result, parseResult.success, errorExitCode);
    spec.addStatement("throw new $T($S)", AssertionError.class, "We should never get here.");

    return spec.addParameter(args)
        .returns(TypeName.get(context.sourceType.asType()));
  }


  private MethodSpec.Builder createMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("create");
    builder.addStatement("return new $T()", context.generatedClass);
    return builder.addModifiers(STATIC)
        .returns(context.generatedClass);
  }


  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by " +
            "<a href=\"https://github.com/h908714124/jbock\">jbock " +
            getClass().getPackage().getImplementationVersion() +
            "</a>\n",
        Processor.class.getName()).build();
  }

  private MethodSpec addPublicIfNecessary(MethodSpec.Builder spec) {
    return addPublicIfNecessary(context, spec);
  }

  static MethodSpec addPublicIfNecessary(Context context, MethodSpec.Builder spec) {
    if (context.sourceType.getModifiers().contains(PUBLIC)) {
      return spec.addModifiers(PUBLIC).build();
    }
    return spec.build();
  }

  private static MethodSpec readArgumentMethod(
      MethodSpec readNextMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec isLong = ParameterSpec.builder(BOOLEAN, "isLong").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("readArgument");

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
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement(CodeBlock.builder()
            .add("throw new $T($S + $N)", IllegalArgumentException.class,
                "Missing value after token: ", token)
            .build())
        .endControlFlow();

    builder.addStatement("return $N.next()", it);

    return MethodSpec.methodBuilder("readNext")
        .addParameters(asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC, PRIVATE)
        .build();
  }
}
