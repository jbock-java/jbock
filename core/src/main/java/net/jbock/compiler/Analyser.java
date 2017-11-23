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
import java.util.EnumMap;
import java.util.EnumSet;
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
  final ClassName keysClass;

  private final MethodSpec read;
  private final MethodSpec readOption;
  private final MethodSpec readArgument;
  private final MethodSpec removeFirstFlag;

  final FieldSpec longNames;
  final FieldSpec shortNames;
  final FieldSpec optMap;
  final FieldSpec sMap;
  final FieldSpec flags;

  private final TypeName optMapType;
  private final TypeName sMapType;
  private final TypeName flagsType;
  private final ClassName optionTypeClass;

  static Analyser create(Context context) {
    return new Analyser(context);
  }

  private Analyser(Processor.Context context) {
    this.context = context;
    this.keysClass = context.generatedClass.nestedClass("Names");
    this.implClass = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    this.optionTypeClass = context.generatedClass.nestedClass("OptionType");
    FieldSpec optionType = FieldSpec.builder(optionTypeClass, "type", PRIVATE, FINAL).build();
    this.option = Option.create(context,
        context.generatedClass.nestedClass("Option"), optionTypeClass, optionType);
    TypeName soType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    ParameterizedTypeName listOfArgumentType = ParameterizedTypeName.get(
        ClassName.get(List.class), STRING);
    this.optMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, listOfArgumentType);
    this.sMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, STRING);
    this.flagsType = ParameterizedTypeName.get(ClassName.get(Set.class),
        option.optionClass);

    this.readArgument = readArgumentMethod();
    this.removeFirstFlag = removeFirstFlagMethod();

    this.optMap = FieldSpec.builder(optMapType, "optMap")
        .addModifiers(FINAL)
        .build();

    this.sMap = FieldSpec.builder(sMapType, "sMap")
        .addModifiers(FINAL)
        .build();

    this.flags = FieldSpec.builder(flagsType, "flags")
        .addModifiers(FINAL)
        .build();

    this.longNames = FieldSpec.builder(soType, "longNames")
        .addModifiers(PRIVATE, FINAL)
        .build();

    this.shortNames = FieldSpec.builder(soType, "shortNames")
        .addModifiers(PRIVATE, FINAL)
        .build();

    this.readOption = readOptionMethod(keysClass, longNames, shortNames, option.optionClass);

    this.read = readMethod(
        context,
        keysClass,
        readOption,
        readArgument,
        optMapType,
        sMapType,
        flagsType,
        option.optionClass,
        optionType,
        optionTypeClass,
        removeFirstFlag);
  }

  private static MethodSpec removeFirstFlagMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("removeFirstFlag");

    builder.beginControlFlow("if ($N.length() <= 2 || $N.startsWith($S))", token, token, "--")
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
        .addType(Names.create(this).define())
        .addType(option.define())
        .addType(Impl.create(this).define())
        .addType(OptionType.define(optionTypeClass))
        .addJavadoc(generatedInfo())
        .addMethod(parseMethod())
        .addMethod(read)
        .addMethod(readOption)
        .addMethod(readArgument)
        .addMethod(removeFirstFlag)
        .addMethod(option.printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optMap = ParameterSpec.builder(optMapType, "optMap").build();
    ParameterSpec sMap = ParameterSpec.builder(sMapType, "sMap").build();
    ParameterSpec flags = ParameterSpec.builder(flagsType, "flags").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec tokenRead = ParameterSpec.builder(BOOLEAN, "tokenRead").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    if (context.stopword != null) {
      builder.addStatement("$T $N = $S", stopword.type, stopword, context.stopword);
    }

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

    builder.addStatement("$T $N = new $T()", names.type, names, keysClass)
        .addStatement("$T $N = new $T<>($T.class)", optMap.type, optMap, EnumMap.class, option.optionClass)
        .addStatement("$T $N = new $T<>($T.class)", sMap.type, sMap, EnumMap.class, option.optionClass)
        .addStatement("$T $N = $T.noneOf($T.class)", flags.type, flags, EnumSet.class, option.optionClass)
        .addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);

    // Begin parsing loop
    builder.beginControlFlow("while ($N.hasNext())", it);

    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.stopword != null) {
      builder.beginControlFlow("if ($N.equals($N))", token, stopword);
      builder.beginControlFlow("while ($N.hasNext())", it)
          .addStatement("$N.add($N.next())", rest, it)
          .endControlFlow();
      builder.addStatement("break");
      builder.endControlFlow();
    }

    builder.addStatement("$T $N = $N($N, $N, $N, $N, $N, $N)",
        tokenRead.type, tokenRead, read, token, names, optMap, sMap, flags, it);

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

    builder.addStatement("return new $T($N, $N, $N, $N, $N)",
        implClass, optMap, sMap, flags, otherTokens, rest);

    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .returns(context.returnType())
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec readOptionMethod(
      ClassName keysClass,
      FieldSpec longNames, FieldSpec shortNames,
      ClassName optionClass) {
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec idxe = ParameterSpec.builder(INT, "idxe").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N.length() < 2 || !$N.startsWith($S))", token, token, "-")
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if (!$N.startsWith($S))", token, "--")
        .addStatement("return $N.$N.get($N.substring(1, 2))", names, shortNames, token)
        .endControlFlow();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, idxe, token);

    builder.beginControlFlow("if ($N < 0)", idxe)
        .addStatement("return $N.$N.get($N.substring(2))", names, longNames, token)
        .endControlFlow();

    builder.addStatement("return $N.$N.get($N.substring(2, $N))", names, longNames, token, idxe);

    return MethodSpec.methodBuilder("readOption")
        .addParameters(Arrays.asList(names, token))
        .addModifiers(STATIC, PRIVATE)
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readArgumentMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec idxe = ParameterSpec.builder(INT, "idxe").build();
    ParameterSpec isLong = ParameterSpec.builder(BOOLEAN, "isLong").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("assert $N.startsWith($S)", token, "-");

    builder.addStatement("$T $N = $N.startsWith($S)", BOOLEAN, isLong, token, "--");
    builder.addStatement("$T $N = $N.indexOf('=')", INT, idxe, token);

    builder.beginControlFlow("if ($N && $N >= 0)", isLong, idxe)
        .add("// long with equals\n")
        .addStatement("return $N.substring($N + 1)", token, idxe)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
        .add("// attached short\n")
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

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
   *
   * <p>If the token is an <em>option group</em>, then all tokens of this
   * group are consumed, along with its argument (unless the group ends with
   * a flag).</p>
   */
  private static MethodSpec readMethod(
      Context context,
      ClassName keysClass,
      MethodSpec readOption,
      MethodSpec readArgument,
      TypeName optionMapType,
      TypeName sMapType,
      TypeName flagsType,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec removeFirstFlag) {

    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
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
        .addStatement("$T $N = $N($N, $N)", option.type, option, readOption, names, token);

    // "other" token
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return $L", false)
        .endControlFlow();

    // begin option group loop
    builder.beginControlFlow("while ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG);

    builder.beginControlFlow("if (!$N.add($N))", flags, option)
        .add(throwRepetitionErrorInGroup(option, originalToken))
        .endControlFlow();
    builder.addStatement("$N = $N($N)", token, removeFirstFlag, token);
    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("return $L", true)
        .endControlFlow();
    builder.addStatement("$N = $N($N, $N)", option, readOption, names, token);
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Unknown token in option group: ", originalToken)
        .endControlFlow();

    // end option group loop
    builder.endControlFlow();

    // if we got here, the option must be binding, so read next token
    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readArgument, token, it);

    // option is repeatable
    builder.beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.REPEATABLE)
        .addStatement("$T $N = $N.computeIfAbsent($N, $N -> new $T<>())",
            bucket.type, bucket, optMap, option, ignore, ArrayList.class)
        .addStatement("$N.add($N)", bucket, argument)
        .endControlFlow();

    // option is optional or required
    builder.beginControlFlow("else")
        .beginControlFlow("if ($N.containsKey($N))", sMap, option)
        .add(repetitionError(option, token))
        .endControlFlow()
        .addStatement("$N.put($N, $N)", sMap, option, argument)
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(
            originalToken, names, optMap, sMap, flags, it))
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
