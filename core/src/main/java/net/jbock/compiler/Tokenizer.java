package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOf;
import static net.jbock.compiler.Util.optionalOfSubtype;

final class Tokenizer {

  final ClassName type;
  private final Context context;
  private final Option option;
  private final Helper helper;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out")
      .addModifiers(PRIVATE, FINAL).build();

  private final FieldSpec indent = FieldSpec.builder(INT, "indent")
      .addModifiers(PRIVATE, FINAL).build();

  private Tokenizer(ClassName type, Context context, Option option, Helper helper) {
    this.type = type;
    this.context = context;
    this.option = option;
    this.helper = helper;
  }

  static Tokenizer create(Context context, Option option, Helper helper) {
    ClassName builderClass = context.generatedClass.nestedClass("Tokenizer");
    return new Tokenizer(builderClass, context, option, helper);
  }


  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addModifiers(STATIC, PRIVATE)
        .addMethod(parseMethod())
        .addMethod(parseMethodInternal())
        .addMethod(privateConstructor())
        .addField(indent)
        .addField(out);
    return builder.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    builder.beginControlFlow("try")
        .addCode(parseMethodTryBlock(args))
        .endControlFlow();

    builder.beginControlFlow("catch ($T $N)", IllegalArgumentException.class, e)
        .addCode(parseMethodCatchBlock(e))
        .endControlFlow();

    return builder
        .addParameter(args)
        .addModifiers(PRIVATE)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .build();
  }

  private CodeBlock parseMethodTryBlock(
      ParameterSpec args) {
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

  private CodeBlock parseMethodCatchBlock(ParameterSpec e) {
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

  private MethodSpec parseMethodInternal() {

    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec dd = ParameterSpec.builder(STRING, "dd").build();
    ParameterSpec count = ParameterSpec.builder(INT, "count").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())))
        .addModifiers(PRIVATE);

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

  private MethodSpec privateConstructor() {
    ParameterSpec outParam = ParameterSpec.builder(out.type, out.name).build();
    ParameterSpec indentParam = ParameterSpec.builder(indent.type, indent.name).build();

    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", out, outParam)
        .addStatement("this.$N = $N", indent, indentParam)
        .addParameter(outParam)
        .addParameter(indentParam)
        .addModifiers(PRIVATE)
        .build();
  }
}
