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
import net.jbock.compiler.Processor.Context;

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PRIVATE, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(ClassName.get(Character.class),
      "shortName", PRIVATE, FINAL).build();

  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  static final FieldSpec otherTokens = FieldSpec.builder(STRING_LIST, "otherTokens", FINAL)
      .build();

  final Context context;

  final ClassName implClass;
  final Option option;
  final ClassName helperClass;

  private final MethodSpec read;
  private final MethodSpec readRegularOptionMethod;
  private final MethodSpec readArgumentMethod;
  private final MethodSpec chopOffShortFlagMethod;

  final FieldSpec longNamesField;
  final FieldSpec shortNamesField;

  final TypeName optMapType;
  final TypeName sMapType;
  final TypeName flagsType;
  final ClassName optionType;

  final Helper helper;

  static Analyser create(Context context) {
    return new Analyser(context);
  }

  private Analyser(Processor.Context context) {
    this.context = context;
    this.helperClass = context.generatedClass.nestedClass("Helper");
    this.implClass = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    this.optionType = context.generatedClass.nestedClass("OptionType");

    FieldSpec optionTypeField = FieldSpec.builder(optionType, "type", PRIVATE, FINAL).build();

    this.option = Option.create(context,
        context.generatedClass.nestedClass("Option"), optionType, optionTypeField);

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

    this.readRegularOptionMethod = readRegularOptionMethod(
        helperClass, longNamesField, shortNamesField, option.optionClass);

    this.helper = Helper.create(
        optMapType,
        sMapType,
        flagsType,
        option,
        helperClass,
        longNamesField,
        shortNamesField);

    this.read = readMethod(
        context,
        helperClass,
        readRegularOptionMethod,
        readArgumentMethod,
        optMapType,
        sMapType,
        flagsType,
        option.optionClass,
        optionTypeField,
        optionType,
        chopOffShortFlagMethod,
        helper.addMethod,
        helper.addFlagMethod);
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
        .addType(OptionType.define(optionType))
        .addJavadoc(generatedInfo())
        .addMethod(parseMethod(helper))
        .addMethod(read)
        .addMethod(readRegularOptionMethod)
        .addMethod(readArgumentMethod)
        .addMethod(chopOffShortFlagMethod)
        .addMethod(option.printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod(Helper helper) {

    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec names = ParameterSpec.builder(helperClass, "helper").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec tokenRead = ParameterSpec.builder(BOOLEAN, "tokenRead").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.add("\n");
    builder.addStatement("$T $N = new $T()", names.type, names, helperClass);
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

    builder.addStatement("$T $N = $N($N, $N, $N)",
        tokenRead.type, tokenRead, read, token, names, it);

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
        implClass, names, otherTokens, rest);

    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .returns(context.returnType())
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      ClassName keysClass,
      FieldSpec longNames, FieldSpec shortNames,
      ClassName optionClass) {
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.charAt(1) != '-')", token)
        .add("// short option\n")
        .addStatement("return $N.$N.get($N.substring(1, 2))", names, shortNames, token)
        .endControlFlow();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N < 0)", index)
        .addStatement("return $N.$N.get($N.substring(2))", names, longNames, token)
        .endControlFlow();

    builder.addStatement("return $N.$N.get($N.substring(2, $N))", names, longNames, token, index);

    return MethodSpec.methodBuilder("readRegularOption")
        .addParameters(Arrays.asList(names, token))
        .addModifiers(STATIC, PRIVATE)
        .returns(optionClass)
        .addCode(builder.build())
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

  /**
   * <p>The read method takes one token. If the token is
   * <em>known</em>, its argument is  also consumed (unless it's a flag),
   * and this information is added to one of
   * {@code sMap}, {@code optMap}, {@code flags}, and {@code true} is returned.
   * Otherwise, none of these collections are modified, and {@code false}
   * is returned.</p>
   * <p>
   * <p>If the token is an <em>option group</em>, then all tokens of this
   * group are consumed, along with its argument (unless the group ends with
   * a flag).</p>
   */
  private static MethodSpec readMethod(
      Context context,
      ClassName keysClass,
      MethodSpec readRegularOptionMethod,
      MethodSpec readArgumentMethod,
      TypeName optionMapType,
      TypeName sMapType,
      TypeName flagsType,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec chopOffShortFlagMethod,
      MethodSpec addMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec helper = ParameterSpec.builder(keysClass, "helper").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optMap").build();
    ParameterSpec sMap = ParameterSpec.builder(sMapType, "sMap").build();
    ParameterSpec flags = ParameterSpec.builder(flagsType, "flags").build();
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec bucket = ParameterSpec.builder(STRING_LIST, "bucket").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();

    ParameterSpec originalToken = ParameterSpec.builder(STRING, "originalToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();

    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N", STRING, token, originalToken)
        .addStatement("$T $N = $N($N, $N)", option.type, option, readRegularOptionMethod, helper, token);

    // unknown token
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return $L", false)
        .endControlFlow();

    // begin option group loop
    builder.beginControlFlow("while ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG);

    builder.beginControlFlow("if (!$N.$N($N))", helper, addFlagMethod, option)
        .add(throwRepetitionErrorInGroup(option, originalToken))
        .endControlFlow();
    builder.addStatement("$N = $N($N)", token, chopOffShortFlagMethod, token);
    builder.beginControlFlow("if ($N == null)", token)
        .add("// done reading flags\n")
        .addStatement("return $L", true)
        .endControlFlow();
    builder.addStatement("$N = $N($N, $N)", option, readRegularOptionMethod, helper, token);
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Unknown token in option group: ", originalToken)
        .endControlFlow();

    // end option group loop
    builder.endControlFlow();

    // if we got here, the option must be binding, so read next token
    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readArgumentMethod, token, it);

    builder.beginControlFlow("if (!$N.$N($N, $N))", helper, addMethod, option, argument)
        .add(repetitionError(option, token))
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(
            originalToken, helper, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(builder.build())
        .returns(BOOLEAN)
        .build();
  }

  private static List<ParameterSpec> readMethodArguments(
      Context context,
      List<ParameterSpec> arguments) {
    if (context.otherTokens) {
      return arguments;
    }
    return arguments.subList(0, arguments.size() - 1);
  }

  private static CodeBlock repetitionError(
      ParameterSpec option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .addStatement("throw new $T($S + $N + $S + $N + $S)",
            IllegalArgumentException.class,
            "Found token: ", token, ", but option ", option, " is not repeatable")
        .build();

  }

  private static CodeBlock throwRepetitionErrorInGroup(
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
    return MethodSpec.constructorBuilder().build();
  }
}
