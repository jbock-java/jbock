package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.JbockContext;

final class Analyser {

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

  final ClassName implClass;
  final Option option;
  final ClassName helperClass;

  private final MethodSpec readArgumentMethod;
  private final MethodSpec chopOffShortFlagMethod;

  final FieldSpec longNamesField;
  final FieldSpec shortNamesField;

  final TypeName optMapType;
  final TypeName sMapType;
  final TypeName flagsType;

  final Helper helper;

  static Analyser create(JbockContext context) {
    return new Analyser(context);
  }

  private Analyser(JbockContext context) {
    this.context = context;
    this.helperClass = context.generatedClass.nestedClass("Helper");
    this.implClass = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");

    this.option = Option.create(context);

    TypeName stringOptionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    ParameterizedTypeName stringListType = ParameterizedTypeName.get(
        ClassName.get(List.class), STRING);

    this.optMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, stringListType);
    this.sMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, STRING);
    this.flagsType = ParameterizedTypeName.get(ClassName.get(Set.class),
        option.optionClass);

    this.readArgumentMethod = readArgumentMethod();
    this.chopOffShortFlagMethod = chopOffFlagShortMethod();

    this.longNamesField = FieldSpec.builder(stringOptionMapType, "longNames")
        .addModifiers(FINAL)
        .build();

    this.shortNamesField = FieldSpec.builder(stringOptionMapType, "shortNames")
        .addModifiers(FINAL)
        .build();

    this.helper = Helper.create(
        context,
        readArgumentMethod,
        chopOffShortFlagMethod,
        optMapType,
        sMapType,
        flagsType,
        option,
        helperClass,
        longNamesField,
        shortNamesField);
  }

  private static MethodSpec chopOffFlagShortMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("chopOffShortFlag");

    builder.beginControlFlow("if ($N.length() <= 2 || $N.charAt(1) == '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    builder.addStatement("return '-' + $N.substring(2)", token);

    return builder.addParameter(token)
        .addModifiers(PRIVATE, STATIC)
        .returns(STRING)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(Impl.create(this, helper).define())
        .addType(OptionType.define(option.optionType))
        .addJavadoc(generatedInfo())
        .addMethod(parseMethod())
        .addMethod(readArgumentMethod)
        .addMethod(chopOffShortFlagMethod)
        .addMethod(option.printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "freeToken").build();
    ParameterSpec helper = ParameterSpec.builder(helperClass, "helper").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec tokenRead = ParameterSpec.builder(BOOLEAN, "tokenRead").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.add("\n");
    builder.addStatement("$T $N = new $T()", helper.type, helper, helperClass);
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
        implClass, helper, otherTokens, rest);

    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .returns(context.returnType())
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec readArgumentMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec isLong = ParameterSpec.builder(BOOLEAN, "isLong").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.charAt(1) == '-'", BOOLEAN, isLong, token);
    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N && $N >= 0)", isLong, index)
        .add("// attached long\n")
        .addStatement("return $N.substring($N + 1)", token, index)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
        .add("// attached short\n")
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

    builder.add("// not attached\n");
    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing value after token: ", token)
        .endControlFlow();

    builder.addStatement("return $N.next()", it);

    return MethodSpec.methodBuilder("readArgument")
        .addParameters(Arrays.asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(PRIVATE, STATIC)
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
