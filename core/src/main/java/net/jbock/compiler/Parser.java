package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.io.PrintStream;

import static javax.lang.model.element.Modifier.PRIVATE;
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
  private final Tokenizer builder;
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
      Tokenizer builder,
      Option option,
      Helper helper,
      Impl impl) {
    this.context = context;
    this.builder = builder;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
  }

  static Parser create(Context context) {
    ClassName implType = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    OptionType optionType = OptionType.create(context);
    Option option = Option.create(context, optionType);
    Impl impl = Impl.create(option, implType);
    Helper helper = Helper.create(context, impl, option);
    Tokenizer builder = Tokenizer.create(context, option, helper);
    return new Parser(context, builder, option, helper, impl);
  }

  TypeSpec define() {
    ParameterSpec outParam = ParameterSpec.builder(out.type, out.name).build();
    ParameterSpec indentParam = ParameterSpec.builder(indent.type, indent.name).build();

    return TypeSpec.classBuilder(context.generatedClass)
        .addMethod(MethodSpec.methodBuilder("out")
            .addParameter(outParam)
            .addStatement("this.$N = $N", out, outParam)
            .addStatement("return this")
            .returns(context.generatedClass)
            .build())
        .addMethod(MethodSpec.methodBuilder("indent")
            .addParameter(indentParam)
            .addStatement("this.$N = $N", indent, indentParam)
            .addStatement("return this")
            .returns(context.generatedClass)
            .build())
        .addType(builder.define())
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(option.optionType.define())
        .addField(out)
        .addField(indent)
        .addMethod(createMethod())
        .addMethod(parseMethod())
        .addMethod(parseOrExitMethodConvenience())
        .addMethod(parseOrExitMethod())
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    ParameterSpec parser = ParameterSpec.builder(this.builder.type, "parser").build();
    builder.addStatement("$T $N = new $T($N, $N)", parser.type, parser, parser.type, out, indent);
    builder.addStatement("return $N.parse($N)", parser, args);

    return builder
        .addParameter(args)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .build();
  }


  private MethodSpec parseOrExitMethodConvenience() {

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    builder.addStatement("return parseOrExit($N, $L)", args, DEFAULT_EXITCODE_ON_ERROR);

    return builder
        .addParameter(args)
        .returns(TypeName.get(context.sourceType.asType()))
        .build();
  }

  private MethodSpec parseOrExitMethod() {

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec statusIfError = ParameterSpec.builder(TypeName.INT, "statusIfError")
        .build();
    ParameterSpec result = ParameterSpec.builder(optionalOf(TypeName.get(context.sourceType.asType())), "result")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_NAME_PARSE_OR_EXIT);

    ParameterSpec parser = ParameterSpec.builder(this.builder.type, "parser").build();

    builder.addStatement("$T $N = new $T($N, $N)", parser.type, parser, parser.type, out, indent);
    builder.addStatement("$T $N = $N.parse($N)", result.type, result, parser, args);

    builder.beginControlFlow("if ($N.isPresent())", result)
        .addStatement("return $N.get()", result)
        .endControlFlow();

    builder.addStatement("$T.exit($N)", System.class, statusIfError);
    builder.addStatement("throw new $T($S)", IllegalStateException.class, "We should never get here.");

    return builder
        .addParameter(args)
        .addParameter(statusIfError)
        .returns(TypeName.get(context.sourceType.asType()))
        .build();
  }


  private MethodSpec createMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("create");
    builder.addStatement("return new $T()", context.generatedClass);
    return builder.addModifiers(STATIC)
        .returns(context.generatedClass)
        .build();
  }


  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by $L\n\n" +
            "@see <a href=\"https://github.com/h908714124/jbock\">jbock on github</a>\n",
        Processor.class.getName()).build();
  }
}
