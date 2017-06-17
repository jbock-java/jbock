package net.jbock.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Processor.Constructor;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.OptionType.REPEATABLE;

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
  static final FieldSpec otherTokens = FieldSpec.builder(STRING_LIST, "otherTokens", PRIVATE, FINAL)
      .build();

  final Constructor constructor;

  final ClassName binderClass;
  final Option option;
  final ClassName keysClass;

  private final MethodSpec read;
  private final MethodSpec readOption;
  private final MethodSpec checkConflict;
  private final MethodSpec readArgument;
  private final MethodSpec removeFirstFlag;

  final FieldSpec longNames;
  final FieldSpec shortNames;
  final FieldSpec optMap;

  private final TypeName optionMapType;
  private final ClassName optionTypeClass;

  static Analyser create(Constructor constructor) {
    return new Analyser(constructor);
  }

  private Analyser(Constructor constructor) {
    this.constructor = constructor;
    this.keysClass = constructor.generatedClass.nestedClass("Names");
    this.binderClass = constructor.generatedClass.nestedClass("Binder");
    this.optionTypeClass = constructor.generatedClass.nestedClass("OptionType");
    FieldSpec optionType = FieldSpec.builder(optionTypeClass, "type", PRIVATE, FINAL).build();
    this.option = Option.create(constructor,
        constructor.generatedClass.nestedClass("Option"), optionTypeClass, optionType);
    TypeName soType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    ParameterizedTypeName listOfArgumentType = ParameterizedTypeName.get(
        ClassName.get(List.class), STRING);
    this.optionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, listOfArgumentType);
    this.readArgument = readArgumentMethod();
    this.removeFirstFlag = removeFirstFlagMethod();
    this.optMap = FieldSpec.builder(optionMapType, "optMap")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.longNames = FieldSpec.builder(soType, "longNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.shortNames = FieldSpec.builder(soType, "shortNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.readOption = readOptionMethod(keysClass, longNames, shortNames, option.optionClass);
    this.checkConflict = checkConflictMethod(optionMapType, option.optionClass, optionTypeClass, optionType);
    this.read = readMethod(keysClass, readOption, readArgument, optionMapType,
        option.optionClass, optionType, optionTypeClass, checkConflict, removeFirstFlag);
  }

  private static MethodSpec removeFirstFlagMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("removeFirstFlag")
        .beginControlFlow("if ($N.length() <= 2 || $N.startsWith($S))",
            token, token, "--")
        .addStatement("return null")
        .endControlFlow()
        .addStatement("return $S + $N.substring(2)", "-", token)
        .addParameter(token)
        .addModifiers(PRIVATE, STATIC)
        .returns(STRING)
        .build();
  }

  private static MethodSpec checkConflictMethod(
      TypeName optionMapType,
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec optionType) {
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    ParameterSpec bucket = ParameterSpec.builder(STRING_LIST, "bucket").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();
    CodeBlock block = CodeBlock.builder()
        .addStatement("$T $N = $N.computeIfAbsent($N, $N -> new $T<>())",
            bucket.type, bucket, optMap, option, ignore, ArrayList.class)
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, REPEATABLE)
        .addStatement("return $N", bucket)
        .endControlFlow()
        .beginControlFlow("if (!$N.isEmpty())", bucket)
        .addStatement("$T $N = $N.$N == $T.$L ?\n  $S :\n  $S", STRING, message, option, optionType,
            optionTypeClass, OptionType.FLAG, "Duplicate flag", "Conflicting token")
        .addStatement("throw new $T($N + $S + $N)", IllegalArgumentException.class,
            message, ": ", token)
        .endControlFlow()
        .addStatement("return $N", bucket)
        .build();
    return MethodSpec.methodBuilder("checkConflict")
        .addParameters(Arrays.asList(optMap, option, token))
        .addCode(block)
        .returns(STRING_LIST)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(constructor.generatedClass)
        .addType(Names.create(this).define())
        .addType(option.define())
        .addType(Binder.create(this).define())
        .addType(OptionType.define(optionTypeClass))
        .addAnnotation(generatedAnnotation())
        .addMethod(parseMethod())
        .addMethod(read)
        .addMethod(readOption)
        .addMethod(readArgument)
        .addMethod(checkConflict)
        .addMethod(removeFirstFlag)
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec stop = ParameterSpec.builder(TypeName.BOOLEAN, "stop").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    if (constructor.stopword != null) {
      builder.addStatement("$T $N = $L", TypeName.BOOLEAN, stop, false);
    }
    //@formatter:off
    builder.addStatement("$T $N = new $T<>()", otherTokens.type, otherTokens, ArrayList.class)
      .addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class)
      .addStatement("$T $N = new $T()", names.type, names, keysClass)
      .addStatement("$T $N = new $T<>($T.class)", optMap.type, optMap, EnumMap.class, option.optionClass)
      .addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS)
      .beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it)
        .beginControlFlow("if ($N == null)", token)
          .addStatement("throw new $T($S)", IllegalArgumentException.class, "null token")
          .endControlFlow();
    //@formatter:on
    if (constructor.stopword != null) {
      builder.beginControlFlow("if ($N)", stop)
          .addStatement("$N.add($N)", rest, token)
          .addStatement("continue")
          .endControlFlow()
          .beginControlFlow("if ($N.equals($S))", token, constructor.stopword)
          .addStatement("$N = $L", stop, true)
          .addStatement("continue")
          .endControlFlow();
    }
    builder.addStatement("$N($N, $N, $N, $N, $N)",
        read, token, names, optMap, otherTokens, it)
        .endControlFlow()
        .addStatement("return new $T($N, $N, $N)", binderClass, optMap, otherTokens, rest);
    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .returns(binderClass)
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
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if ($N.length() < 2 || !$N.startsWith($S))", token, token, "-")
          .addStatement("return null")
           .endControlFlow()
        .beginControlFlow("if (!$N.startsWith($S))", token, "--")
          .addStatement("return $N.$N.get($N.substring(1, 2))", names, shortNames, token)
          .endControlFlow()
        .addStatement("$T $N = $N.indexOf('=')", INT, idxe, token)
        .beginControlFlow("if ($N < 0)", idxe)
          .addStatement("return $N.$N.get($N.substring(2))", names, longNames, token)
          .endControlFlow()
        .addStatement("return $N.$N.get($N.substring(2, $N))", names, longNames, token, idxe);
    //@formatter:on
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
    ParameterSpec isLong = ParameterSpec.builder(TypeName.BOOLEAN, "isLong").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if (!$N.startsWith($S))", token, "-")
          .addStatement("throw new AssertionError($S)", "invalid token")
          .endControlFlow()
        .addStatement("$T $N = $N.startsWith($S)", TypeName.BOOLEAN, isLong, token, "--")
        .addStatement("$T $N = $N.indexOf('=')", INT, idxe, token)
        .beginControlFlow("if ($N && $N >= 0)", isLong, idxe)
          .add("// long with equals\n")
          .addStatement("return $N.substring($N + 1)", token, idxe)
          .endControlFlow()
        .beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
          .add("// attached short\n")
          .addStatement("return $N.substring(2)", token)
          .endControlFlow()
        .beginControlFlow("if (!$N.hasNext())", it)
          .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing value: ", token)
          .endControlFlow()
        .addStatement("return $N.next()", it);
    //@formatter:on
    return MethodSpec.methodBuilder("readArgument")
        .addParameters(Arrays.asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec readMethod(
      ClassName keysClass,
      MethodSpec readOption,
      MethodSpec readArgument,
      TypeName optionMapType,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec checkConflict,
      MethodSpec removeFirstFlag) {
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optMap").build();
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec bucket = ParameterSpec.builder(STRING_LIST, "bucket").build();

    ParameterSpec originalToken = ParameterSpec.builder(STRING, "originalToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N", STRING, token, originalToken)
        .addStatement("$T $N = $N($N, $N)", option.type, option, readOption, names, token)
        .beginControlFlow("if ($N == null)", option)
          .addStatement("$N.add($N)", otherTokens, token)
          .addStatement("return")
          .endControlFlow()
        .beginControlFlow("while ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG)
          .addStatement("$T $N = $N($N, $N, $N)", bucket.type, bucket, checkConflict, optMap, option, token)
          .addStatement("$N.add($S)", bucket, "t")
          .addStatement("$N = $N($N)", token, removeFirstFlag, token)
          .beginControlFlow("if ($N == null)", token)
            .addStatement("return")
            .endControlFlow()
          .addStatement("$N = $N($N, $N)", option, readOption, names, token)
          .beginControlFlow("if ($N == null)", option)
            .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
                "invalid token: ", originalToken)
            .endControlFlow()
          .endControlFlow()
        .addStatement("$T $N = $N($N, $N, $N)", bucket.type, bucket, checkConflict, optMap, option, token)
        .addStatement("$N.add($N($N, $N))", bucket, readArgument, token, it);
    //@formatter:on
    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(originalToken, names, optMap, otherTokens, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(builder.build())
        .build();
  }

  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", Processor.class.getName())
        .build();
  }
}
