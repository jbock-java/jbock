package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  final Context context;
  final OptionType optionType;
  final Option option;
  final Helper helper;
  final Impl impl;

  private Parser(
      Context context,
      OptionType optionType,
      Option option,
      Helper helper,
      Impl impl) {
    this.context = context;
    this.optionType = optionType;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
  }

  static Parser create(Context context) {
    ClassName implType = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    OptionType optionType = OptionType.create(context);
    Option option = Option.create(context, optionType);
    Helper helper = Helper.create(context, implType, optionType, option);
    Impl impl = Impl.create(context, implType, optionType, option, helper);
    return new Parser(context, optionType, option, helper, impl);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(optionType.define())
        .addMethod(parseMethod())
        .addMethod(printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec otherTokens = ParameterSpec.builder(LIST_OF_STRING, "otherTokens").build();
    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec rest = ParameterSpec.builder(LIST_OF_STRING, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec tokenRead = ParameterSpec.builder(BOOLEAN, "tokenRead").build();
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.add("\n");
    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);

    if (context.stopword != null) {
      builder.add("\n");
      builder.addStatement("$T $N = $S", stopword.type, stopword, context.stopword);
    }

    builder.add("\n");
    if (context.otherTokens) {
      builder.addStatement("$T $N = new $T<>()", otherTokens.type, otherTokens, ArrayList.class);
    } else {
      builder.addStatement("$T $N = $T.emptyList()", otherTokens.type, otherTokens, Collections.class);
    }

    if (context.rest) {
      builder.addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class);
    } else {
      builder.addStatement("$T $N = $T.emptyList()", rest.type, rest, Collections.class);
    }

    if (context.grouping) {

      builder.add("\n");
      ParameterSpec firstToken = ParameterSpec.builder(STRING, "firstToken").build();

      // begin handle first token
      builder.beginControlFlow("if ($N.hasNext())", it);

      builder.addStatement("$T $N = $N.next()", STRING, firstToken, it);

      if (context.stopword != null) {
        builder.beginControlFlow("if ($N.equals($N))", firstToken, stopword)
            .addStatement("$N.forEachRemaining($N::add)", it, rest)
            .addStatement("return $N.$N($N, $N)",
                helper, this.helper.buildMethod, otherTokens, rest)
            .endControlFlow();
      }

      builder.addStatement("$T $N = $N.$N($N, $N)",
          tokenRead.type, tokenRead, helper, this.helper.readGroupMethod, firstToken, it);

      builder.beginControlFlow("if (!$N)", tokenRead);
      if (context.otherTokens) {
        builder.addStatement("$N.add($N)", otherTokens, firstToken);
      } else {
        builder.addStatement("throw new $T($S + $N)",
            IllegalArgumentException.class, "Unknown token: ", firstToken);
      }

      builder.endControlFlow();

      // end handle first token
      builder.endControlFlow();
    }

    ParameterSpec freetoken = ParameterSpec.builder(STRING, "freeToken").build();

    // Begin parsing loop
    builder.add("\n");
    builder.beginControlFlow("while ($N.hasNext())", it);

    builder.addStatement("$T $N = $N.next()", STRING, freetoken, it);

    if (context.stopword != null) {
      builder.beginControlFlow("if ($N.equals($N))", freetoken, stopword)
          .addStatement("$N.forEachRemaining($N::add)", it, rest)
          .addStatement("return $N.$N($N, $N)",
              helper, this.helper.buildMethod, otherTokens, rest)
          .endControlFlow();
    }

    builder.addStatement("$T $N = $N.$N($N, $N)",
        tokenRead.type, tokenRead, helper, this.helper.readMethod, freetoken, it);

    builder.beginControlFlow("if (!$N)", tokenRead);
    if (context.otherTokens) {
      builder.addStatement("$N.add($N)", otherTokens, freetoken);
    } else {
      builder.addStatement("throw new $T($S + $N)",
          IllegalArgumentException.class, "Unknown token: ", freetoken);
    }
    builder.endControlFlow();

    // End parsing loop
    builder.endControlFlow();

    builder.add("\n");
    builder.addStatement("return $N.$N($N, $N)",
        helper, this.helper.buildMethod, otherTokens, rest);

    return MethodSpec.methodBuilder("parse")
        .addParameter(args)
        .addCode(builder.build())
        .returns(TypeName.get(context.sourceType.asType()))
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private MethodSpec printUsageMethod() {
    ParameterSpec option = ParameterSpec.builder(this.option.type, "option").build();
    ParameterSpec out = ParameterSpec.builder(ClassName.get(PrintStream.class), "out").build();
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    return MethodSpec.methodBuilder("printUsage")
        .beginControlFlow("for ($T $N: $T.values())", option.type, option, option.type)
        .addStatement("$N.println($N.describe($N))", out, option, indent)
        .endControlFlow()
        .addModifiers(STATIC, PUBLIC)
        .addParameters(Arrays.asList(out, indent))
        .build();
  }

  static CodeBlock repetitionError(
      ParameterSpec option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .addStatement("throw new $T($S + $N + $S + $N + $S)",
            IllegalArgumentException.class,
            "Found token: ", token, ", but option ", option, " is not repeatable")
        .build();
  }

  static CodeBlock throwRepetitionErrorInGroup(
      ParameterSpec option,
      ParameterSpec originalToken) {
    return CodeBlock.builder()
        .addStatement("throw new $T($S + $N + $S + $N + $S)",
            IllegalArgumentException.class,
            "In option group ",
            originalToken, ": option ", option, " is not repeatable")
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
