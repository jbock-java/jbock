package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.*;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.*;
import static net.jbock.compiler.Util.optionalOf;
import static net.jbock.compiler.Util.optionalOfSubtype;

/**
 * Generates the *_Parser class.
 */
final class Parser {

  private static final int DEFAULT_INDENT = 7;

  private final Context context;
  private final Option option;
  private final Helper helper;
  private final Impl impl;

  private Parser(
      Context context,
      Option option,
      Helper helper,
      Impl impl) {
    this.context = context;
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
    return new Parser(context, option, helper, impl);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(option.optionType.define())
        .addMethod(parseMethodConvenience())
        .addMethod(parseMethod())
        .addMethod(parseMethodInternal())
        .addMethod(privateConstructor())
        .addModifiers(FINAL)
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec parseMethodConvenience() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec out = ParameterSpec.builder(PrintStream.class, "out")
        .build();
    return builder
        .addStatement("return parse($N, $N, $L)", args, out, DEFAULT_INDENT)
        .addParameters(Arrays.asList(args, out))
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .addModifiers(STATIC)
        .build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec out = ParameterSpec.builder(PrintStream.class, "out")
        .build();
    ParameterSpec indent = ParameterSpec.builder(INT, "indent")
        .build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    builder.beginControlFlow("try")
        .addCode(parseMethodTryBlock(args, out, indent))
        .endControlFlow();

    builder.beginControlFlow("catch ($T $N)", IllegalArgumentException.class, e)
        .addCode(parseMethodCatchBlock(out, indent, e))
        .endControlFlow();

    return builder
        .addParameters(Arrays.asList(args, out, indent))
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .addModifiers(STATIC)
        .build();
  }

  private CodeBlock parseMethodCatchBlock(
      ParameterSpec out,
      ParameterSpec indent,
      ParameterSpec e) {
    CodeBlock.Builder builder = CodeBlock.builder();
    if (context.helpDisabled) {
      builder.addStatement("$T.$N($N, $N)", option.type, option.printUsageMethod, out, indent);
      builder.addStatement("$N.println($N.getMessage())", out, e);
    } else {
      builder.addStatement("$N.print($S)", out, "Usage: ");
      builder.addStatement("$N.println($T.$N())", out, option.type, option.synopsisMethod);
      builder.addStatement("$N.println($N.getMessage())", out, e);
      builder.addStatement("$N.print($S)", out, "Try '");
      builder.addStatement("$N.print($S)", out, context.programName);
      builder.addStatement("$N.println($S)", out, " --help' for more information.");
    }
    builder.addStatement("return $T.empty()", Optional.class);
    return builder.build();
  }

  private CodeBlock parseMethodTryBlock(
      ParameterSpec args,
      ParameterSpec out,
      ParameterSpec indent) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec result = ParameterSpec.builder(optionalOfSubtype(TypeName.get(context.sourceType.asType())), "result")
        .build();
    builder.addStatement("$T $N = parse($T.asList($N))",
        result.type, result, Arrays.class, args);

    builder.beginControlFlow("if (!$N.isPresent())", result)
        .addStatement("$T.$N($N, $N)", option.type, option.printUsageMethod, out, indent)
        .endControlFlow();

    builder.addStatement("return $N.map($T.identity())",
        result, Function.class);
    return builder.build();
  }

  private MethodSpec parseMethodInternal() {

    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec dd = ParameterSpec.builder(STRING, "dd").build();
    ParameterSpec count = ParameterSpec.builder(INT, "count").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())))
        .addModifiers(PRIVATE, STATIC);

    builder.addStatement("$T $N = 0", INT, count);
    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, tokens);

    if (context.stopword) {
      builder.addStatement("$T $N = $S", dd.type, dd, "--");
    }

    if (!context.positionalParameters.isEmpty()) {
      builder.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
    }

    builder.beginControlFlow("while ($N.hasNext())", it)
        .addCode(codeInsideParsingLoop(helper, it, dd, count))
        .endControlFlow();

    builder.addStatement(returnFromParseExpression(helper,
        CodeBlock.builder().add("$T.empty()", OptionalInt.class).build()));
    return builder.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec helper,
      ParameterSpec it,
      ParameterSpec dd,
      ParameterSpec count) {

    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (!context.helpDisabled) {
      builder.beginControlFlow("if ($N++ == 0 && !$N.hasNext() && $S.equals($N))", count, it, "--help", token)
          .addStatement("return $T.empty()", Optional.class)
          .endControlFlow();
    }

    if (context.stopword) {

      builder.beginControlFlow("if ($N.equals($N))", dd, token)
          .addStatement("$T $N = $T.of($N.size())", OptionalInt.class, this.helper.ddIndexParameter,
              OptionalInt.class, this.helper.positionalParameter)
          .addStatement("$N.forEachRemaining($N::add)", it, this.helper.positionalParameter)
          .addStatement(returnFromParseExpression(helper, CodeBlock.builder().add("$N", this.helper.ddIndexParameter).build()))
          .endControlFlow();
    }

    builder.addStatement("$T $N = $N.$N($N)", option.type, optionParam, helper, this.helper.readRegularOptionMethod, token);

    builder.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N($N, $N, $N)",
            helper, this.helper.readMethod, optionParam, token, it)
        .endControlFlow();

    if (context.positionalParameters.isEmpty()) {

      builder.beginControlFlow("else")
          .addStatement(throwInvalidOptionStatement(token))
          .endControlFlow();

    } else {

      if (!context.ignoreDashes) {
        builder.beginControlFlow("else if (!$N.isEmpty() && $N.charAt(0) == '-')",
            token, token)
            .addStatement(throwInvalidOptionStatement(token))
            .endControlFlow();
      }

      builder.beginControlFlow("else")
          .addStatement("$N.add($N)", this.helper.positionalParameter, token)
          .endControlFlow();
    }

    return builder.build();
  }

  private CodeBlock throwInvalidOptionStatement(ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private CodeBlock returnFromParseExpression(ParameterSpec helper, CodeBlock param) {
    if (context.positionalParameters.isEmpty()) {
      return CodeBlock.builder()
          .add("return $N.build()", helper)
          .build();
    }
    return CodeBlock.builder()
        .add("return $N.build($N, $L)",
            helper, this.helper.positionalParameter, param)
        .build();
  }

  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by $L\n\n" +
            "@see <a href=\"https://github.com/h908714124/jbock\">jbock on github</a>\n",
        Processor.class.getName()).build();
  }

  private MethodSpec privateConstructor() {
    return MethodSpec.constructorBuilder()
        .addModifiers(PRIVATE)
        .build();
  }
}
