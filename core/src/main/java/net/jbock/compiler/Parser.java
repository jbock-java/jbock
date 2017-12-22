package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOf;
import static net.jbock.compiler.Util.optionalOfSubtype;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

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
        .addMethod(printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
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
        .addModifiers(PUBLIC, STATIC)
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
    ParameterSpec result = ParameterSpec.builder(optionalOfSubtype(TypeName.get(context.sourceType.asType())), "result")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    // begin try block
    builder.beginControlFlow("try");

    builder.addStatement("$T $N = parse($T.asList($N))",
        result.type, result, Arrays.class, args);

    builder.beginControlFlow("if (!$N.isPresent())", result)
        .addStatement("$T.$N($N, $N)", option.type, option.printUsageMethod, out, indent)
        .endControlFlow();

    builder.addStatement("return $N.map($T.identity())",
        result, Function.class);

    // end try block
    builder.endControlFlow();

    // begin catch block
    builder.beginControlFlow("catch ($T $N)", IllegalArgumentException.class, e);

    builder.addStatement("$N.print($S)", out, "Usage: ");
    builder.addStatement("$N.println($T.$N())", out, option.type, option.synopsisMethod);
    builder.addStatement("$N.println($N.getMessage())", out, e);
    if (!context.helpDisabled) {
      builder.addStatement("$N.println($S)", out, "Try '--help' for more information.");
    }
    builder.addStatement("return $T.empty()", Optional.class);

    // end catch block
    builder.endControlFlow();

    return builder
        .addParameters(Arrays.asList(args, out, indent))
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private MethodSpec parseMethodInternal() {

    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec dd = ParameterSpec.builder(STRING, "dd").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())))
        .addModifiers(PRIVATE, STATIC);

    if (context.simplePositional()) {
      return builder
          .addStatement("return $T.build($N, $T.empty())",
              helper.type, tokens, OptionalInt.class)
          .build();
    }

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
        .addCode(codeInsideParsingLoop(helper, it, dd))
        .endControlFlow();

    builder.addStatement(returnFromParseExpression(helper,
        CodeBlock.builder().add("$T.empty()", OptionalInt.class).build()));
    return builder.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec helper,
      ParameterSpec it,
      ParameterSpec dd) {

    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (!context.helpDisabled) {
      builder.beginControlFlow("if ($S.equals($N))", "--help", token)
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

  private MethodSpec printUsageMethod() {
    ParameterSpec out = ParameterSpec.builder(PrintStream.class, "out").build();
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    return MethodSpec.methodBuilder("printUsage")
        .addStatement("$T.$N($N, $N)",
            option.type, option.printUsageMethod, out, indent)
        .addModifiers(PRIVATE, STATIC)
        .addParameters(Arrays.asList(out, indent))
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
