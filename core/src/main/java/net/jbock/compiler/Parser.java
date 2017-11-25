package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Generates the *_Parser class.
 */
final class Parser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PRIVATE, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(ClassName.get(Character.class),
      "shortName", PRIVATE, FINAL).build();

  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  static final FieldSpec otherTokens = FieldSpec.builder(STRING_LIST, "otherTokens", FINAL)
      .build();

  final JbockContext context;

  final Option option;

  final Helper helper;
  final Impl impl;

  static Parser create(JbockContext context) {
    return new Parser(context);
  }

  private Parser(JbockContext context) {

    this.context = context;
    this.option = Option.create(context);
    this.helper = Helper.create(
        context,
        option);
    this.impl = Impl.create(context, option, helper);

  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(OptionType.define(option.optionType))
        .addJavadoc(generatedInfo())
        .addMethod(parseMethod())
        .addMethod(option.printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "freeToken").build();
    ParameterSpec helper = ParameterSpec.builder(this.helper.helperClass, "helper").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec tokenRead = ParameterSpec.builder(BOOLEAN, "tokenRead").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.add("\n");
    builder.addStatement("$T $N = new $T()", helper.type, helper, this.helper.helperClass);
    builder.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, ARGS);

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

    // Begin parsing loop
    builder.add("\n");
    builder.beginControlFlow("while ($N.hasNext())", it);

    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.stopword != null) {
      builder.beginControlFlow("if ($N.equals($N))", token, stopword)
          .addStatement("$N.forEachRemaining($N::add)", it, rest)
          .addStatement("break")
          .endControlFlow();
    }

    builder.addStatement("$T $N = $N.$N($N, $N)",
        tokenRead.type, tokenRead, helper, this.helper.readMethod, token, it);

    builder.beginControlFlow("if (!$N)", tokenRead);
    if (context.otherTokens) {
      builder.addStatement("$N.add($N)", otherTokens, token);
    } else {
      builder.addStatement("throw new $T($S + $N)",
          IllegalArgumentException.class, "Unknown token: ", token);
    }
    builder.endControlFlow();

    // End parsing loop
    builder.endControlFlow();

    builder.add("\n");
    builder.addStatement("return new $T($N, $N, $N)",
        impl.implClass, helper, otherTokens, rest);

    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .returns(context.returnType())
        .addModifiers(PUBLIC, STATIC)
        .build();
  }


  private static List<ParameterSpec> readMethodArguments(
      JbockContext context,
      List<ParameterSpec> arguments) {
    if (context.otherTokens) {
      return arguments;
    }
    return arguments.subList(0, arguments.size() - 1);
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

  private CodeBlock generatedInfo() {
    return CodeBlock.builder().add("Generated by $L\n\n" +
            "@see <a href=\"https://github.com/h908714124/jbock\">jbock on github</a>\n",
        Processor.class.getName()).build();
  }

  private MethodSpec privateConstructor() {
    return MethodSpec.constructorBuilder().addModifiers(PRIVATE).build();
  }
}
