package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.io.PrintStream;
import java.util.OptionalInt;

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
  private final IndentPrinter indentPrinter;
  private final Tokenizer tokenizer;
  private final Option option;
  private final Helper helper;
  private final Impl impl;

  private final MethodSpec readNextMethod;
  private final MethodSpec readArgumentMethod;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out")
      .initializer("$T.out", System.class)
      .addModifiers(PRIVATE).build();

  private final FieldSpec indent = FieldSpec.builder(INT, "indent")
      .initializer("$L", DEFAULT_INDENT)
      .addModifiers(PRIVATE).build();

  private Parser(
      Context context,
      IndentPrinter indentPrinter,
      Tokenizer tokenizer,
      Option option,
      Helper helper,
      Impl impl,
      MethodSpec readNextMethod,
      MethodSpec readArgumentMethod) {
    this.context = context;
    this.indentPrinter = indentPrinter;
    this.tokenizer = tokenizer;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
    this.readNextMethod = readNextMethod;
    this.readArgumentMethod = readArgumentMethod;
  }

  static Parser create(Context context) {
    ClassName implType = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    IndentPrinter indentPrinter = IndentPrinter.create(context);
    Option option = Option.create(context);
    Impl impl = Impl.create(option, implType);
    Helper helper = Helper.create(context, impl, option);
    Tokenizer builder = Tokenizer.create(context, option, helper, indentPrinter);
    return new Parser(context, indentPrinter, builder, option, helper, impl, readNextMethod, readArgumentMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass);
    if (context.sourceType.getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    return spec.addMethod(addPublicIfNecessary(outMethod()))
        .addMethod(addPublicIfNecessary(indentMethod()))
        .addType(tokenizer.define())
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(OptionParser.define(context))
        .addType(FlagOptionParser.define(context))
        .addType(RegularOptionParser.define(context))
        .addType(RepeatableOptionParser.define(context))
        .addType(indentPrinter.define())
        .addField(out)
        .addField(indent)
        .addMethod(readArgumentMethod)
        .addMethod(readNextMethod)
        .addMethod(mapOptionalIntMethod())
        .addMethod(addPublicIfNecessary(createMethod()))
        .addMethod(addPublicIfNecessary(parseMethod()))
        .addMethod(addPublicIfNecessary(parseOrExitMethodConvenience()))
        .addMethod(addPublicIfNecessary(parseOrExitMethod()))
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec mapOptionalIntMethod() {
    ParameterSpec opt = ParameterSpec.builder(optionalOf(TypeName.get(Integer.class)), "opt").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("mapOptionalInt");
    spec.beginControlFlow("if (!$N.isPresent())", opt)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();
    return spec
        .addStatement("return $T.of($N.get())", OptionalInt.class, opt)
        .returns(OptionalInt.class)
        .addModifiers(PRIVATE, STATIC)
        .addParameter(opt)
        .build();
  }

  private MethodSpec.Builder indentMethod() {
    ParameterSpec indentParam = ParameterSpec.builder(indent.type, indent.name).build();
    return MethodSpec.methodBuilder("indent")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", indent, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder outMethod() {
    ParameterSpec outParam = ParameterSpec.builder(out.type, out.name).build();
    return MethodSpec.methodBuilder("out")
        .addParameter(outParam)
        .addStatement("this.$N = $N", out, outParam)
        .addStatement("return this")
        .returns(context.generatedClass);
  }

  private MethodSpec.Builder parseMethod() {

    ParameterSpec args = ParameterSpec.builder(STRING_ARRAY, "args")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    ParameterSpec paramTokenizer = ParameterSpec.builder(tokenizer.type, "tokenizer").build();
    spec.addStatement("$T $N = new $T(new $T($N, $N))",
        paramTokenizer.type, paramTokenizer, paramTokenizer.type, indentPrinter.type, out, indent);
    spec.addStatement("return $N.parse($N)", paramTokenizer, args);

    return spec.addParameter(args)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())));
  }


  private MethodSpec.Builder parseOrExitMethodConvenience() {

    ParameterSpec args = ParameterSpec.builder(STRING_ARRAY, "args")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    spec.addStatement("return parseOrExit($N, $L)", args, DEFAULT_EXITCODE_ON_ERROR);

    return spec.addParameter(args)
        .returns(TypeName.get(context.sourceType.asType()));
  }

  private MethodSpec.Builder parseOrExitMethod() {

    ParameterSpec args = ParameterSpec.builder(STRING_ARRAY, "args")
        .build();
    ParameterSpec statusIfError = ParameterSpec.builder(TypeName.INT, "statusIfError")
        .build();
    ParameterSpec result = ParameterSpec.builder(optionalOf(TypeName.get(context.sourceType.asType())), "result")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    ParameterSpec paramTokenizer = ParameterSpec.builder(tokenizer.type, "tokenizer").build();

    spec.addStatement("$T $N = new $T(new $T($N, $N))",
        paramTokenizer.type, paramTokenizer, paramTokenizer.type, indentPrinter.type, out, indent);
    spec.addStatement("$T $N = $N.parse($N)", result.type, result, paramTokenizer, args);

    spec.beginControlFlow("if ($N.isPresent())", result)
        .addStatement("return $N.get()", result)
        .endControlFlow();

    spec.addStatement("$T.exit($N)", System.class, statusIfError);
    spec.addStatement("throw new $T($S)", IllegalStateException.class, "We should never get here.");

    return spec.addParameter(args)
        .addParameter(statusIfError)
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
            "</a>",
        Processor.class.getName()).build();
  }

  private MethodSpec addPublicIfNecessary(MethodSpec.Builder spec) {
    if (context.sourceType.getModifiers().contains(PUBLIC)) {
      return spec.addModifiers(PUBLIC).build();
    }
    return spec.build();
  }

  static MethodSpec readArgumentMethod(
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

    builder.beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

    builder.addStatement("return $N($N, $N)", readNextMethod, token, it);

    return builder.addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(STATIC, PRIVATE)
        .build();
  }

  static MethodSpec readNextMethod() {
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
