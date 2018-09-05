package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.io.PrintStream;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.STRING;
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
      Impl impl) {
    this.context = context;
    this.indentPrinter = indentPrinter;
    this.tokenizer = tokenizer;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
  }

  static Parser create(Context context) {
    ClassName implType = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    IndentPrinter indentPrinter = IndentPrinter.create(context);
    Option option = Option.create(context);
    Impl impl = Impl.create(option, implType);
    Helper helper = Helper.create(context, impl, option);
    Tokenizer builder = Tokenizer.create(context, option, helper, indentPrinter);
    return new Parser(context, indentPrinter, builder, option, helper, impl);
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
        .addType(OptionType.define(context))
        .addType(indentPrinter.define())
        .addField(out)
        .addField(indent)
        .addMethod(addPublicIfNecessary(createMethod()))
        .addMethod(addPublicIfNecessary(parseMethod()))
        .addMethod(addPublicIfNecessary(parseOrExitMethodConvenience()))
        .addMethod(addPublicIfNecessary(parseOrExitMethod()))
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc())
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

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
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

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    spec.addStatement("return parseOrExit($N, $L)", args, DEFAULT_EXITCODE_ON_ERROR);

    return spec.addParameter(args)
        .returns(TypeName.get(context.sourceType.asType()));
  }

  private MethodSpec.Builder parseOrExitMethod() {

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
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
}
