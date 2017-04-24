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
import static net.jbock.compiler.Option.constructorArgumentsForJavadoc;

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
  private static final FieldSpec otherTokens = FieldSpec.builder(STRING_LIST, "otherTokens", PRIVATE, FINAL)
      .build();

  private final Constructor constructor;

  private final ClassName binderClass;
  private final Option option;
  private final ClassName optionTypeClass;
  private final ClassName keysClass;

  private final MethodSpec read;
  private final MethodSpec whichOption;
  private final MethodSpec checkConflict;
  private final MethodSpec trimToken;
  private final MethodSpec readArgument;

  private final FieldSpec shortFlags;
  private final FieldSpec longFlags;
  private final FieldSpec longNames;
  private final FieldSpec shortNames;
  private final FieldSpec optMap;
  private final FieldSpec optionType;

  private final TypeName optionMapType;

  static Analyser create(Constructor constructor) {
    return new Analyser(constructor);
  }

  private Analyser(Constructor constructor) {
    this.constructor = constructor;
    this.keysClass = constructor.generatedClass.nestedClass("Keys");
    this.binderClass = constructor.generatedClass.nestedClass("Binder");
    this.optionTypeClass = constructor.generatedClass.nestedClass("OptionType");
    this.optionType = FieldSpec.builder(optionTypeClass, "type", PRIVATE, FINAL).build();
    this.option = Option.create(constructor,
        constructor.generatedClass.nestedClass("Option"), optionTypeClass, optionType);
    TypeName soType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    ParameterizedTypeName listOfArgumentType = ParameterizedTypeName.get(
        ClassName.get(List.class), STRING);
    this.optionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, listOfArgumentType);
    this.trimToken = trimTokenMethod();
    this.readArgument = readArgumentMethod(option.optionClass, optionType, optionTypeClass, trimToken);
    this.optMap = FieldSpec.builder(optionMapType, "optMap")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.shortFlags = FieldSpec.builder(soType, "shortFlags")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.longFlags = FieldSpec.builder(soType, "longFlags")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.longNames = FieldSpec.builder(soType, "longNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.shortNames = FieldSpec.builder(soType, "shortNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.whichOption = whichOptionMethod(keysClass, longFlags, shortFlags, longNames, shortNames, option.optionClass);
    this.checkConflict = checkConflictMethod(optionMapType, option.optionClass, optionTypeClass, optionType);
    this.read = readMethod(keysClass, whichOption, readArgument, optionMapType,
        option.optionClass, checkConflict);
  }

  private static MethodSpec checkConflictMethod(TypeName optionMapType, ClassName optionClass,
                                                ClassName optionTypeClass, FieldSpec optionType) {
    ParameterSpec optionMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    CodeBlock block = CodeBlock.builder()
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.LIST)
        .addStatement("return")
        .endControlFlow()
        .beginControlFlow("if ($N.containsKey($N))", optionMap, option)
        .addStatement("$T $N = $N.$N == $T.$L ? $S : $S", STRING, message, option, optionType,
            optionTypeClass, OptionType.FLAG, "Duplicate flag", "Conflicting token")
        .addStatement("throw new $T($N + $S + $N)", IllegalArgumentException.class,
            message, ": ", token)
        .endControlFlow()
        .build();
    return MethodSpec.methodBuilder("checkConflict")
        .addParameters(Arrays.asList(optionMap, option, token))
        .addCode(block)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(constructor.generatedClass)
        .addType(Keys.create(option.optionClass, optionTypeClass, keysClass, longFlags,
            shortFlags, longNames, shortNames, optionType).define())
        .addType(Option.create(constructor, option.optionClass, optionTypeClass, optionType).define())
        .addType(Binder.create(binderClass, option, optMap,
            otherTokens, constructor).define())
        .addType(OptionType.define(optionTypeClass))
        .addAnnotation(generatedAnnotation())
        .addMethod(privateConstructor())
        .addMethod(checkConflict)
        .addMethod(read)
        .addMethod(whichOption)
        .addMethod(trimToken)
        .addMethod(readArgument)
        .addMethod(parseMethod())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec keys = ParameterSpec.builder(keysClass, "keys").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec stop = ParameterSpec.builder(TypeName.BOOLEAN, "stop").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $L", TypeName.BOOLEAN, stop, false)
        .addStatement("$T $N = new $T<>()", otherTokens.type, otherTokens, ArrayList.class)
        .addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class)
        .addStatement("$T $N = new $T()", keys.type, keys, keysClass)
        .addStatement("$T $N = new $T<>($T.class)", optMap.type, optMap, EnumMap.class, option.optionClass)
        .addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS)
        .beginControlFlow("while ($N.hasNext())", it)
          .addStatement("$T $N = $N.next()", STRING, token, it)
          .beginControlFlow("if ($N == null)", token)
            .addStatement("throw new $T($S)", IllegalArgumentException.class, "null token")
            .endControlFlow()
          .beginControlFlow("else if ($N)", stop)
            .addStatement("$N.add($N)", rest, token)
            .endControlFlow()
          .beginControlFlow("else if ($N.equals($S))", token, constructor.stopword)
            .addStatement("$N = $L", stop, true)
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N($N, $N, $N, $N, $N)", read, token, keys, optMap, otherTokens, it)
            .endControlFlow()
          .endControlFlow()
        .addStatement("return new $T($N, $N, $N)", binderClass, optMap, otherTokens, rest);
    //@formatter:on
    TypeName originalClass = constructor.enclosingType;
    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .addJavadoc("Parses the command line arguments and performs basic validation,\n" +
                "depending on the constructor arguments and their annotations.\n" +
                "\n" +
                "@param args command line arguments\n" +
                "@throws $T if the input is invalid or ambiguous\n" +
                "@return a binder for constructing {@link $T}\n" +
                "\n" +
                "@see $T#$T($L)\n",
            IllegalArgumentException.class,
            constructor.enclosingType,
            originalClass, originalClass, constructorArgumentsForJavadoc(constructor))
        .returns(binderClass)
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec whichOptionMethod(ClassName keysClass,
                                              FieldSpec longFlags, FieldSpec shortFlags,
                                              FieldSpec longNames, FieldSpec shortNames,
                                              ClassName optionClass) {
    ParameterSpec keys = ParameterSpec.builder(keysClass, "keys").build();
    ParameterSpec st = ParameterSpec.builder(STRING, "st").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec ie = ParameterSpec.builder(INT, "ie").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if ($N.startsWith($S))", token, "--")
          .addStatement("$T $N = $N.substring(2)", STRING, st, token)
          .addStatement("$T $N = $N.indexOf('=')", INT, ie, st)
          .beginControlFlow("if ($N < 0 && $N.$N.containsKey($N))", ie, keys, longNames, st)
            .addStatement("throw new $T($S + $N)",
                IllegalArgumentException.class, "Missing '=' after ", token)
            .endControlFlow()
          .beginControlFlow("if ($N < 0 && $N.$N.containsKey($N))", ie, keys, longFlags, st)
            .addStatement("return $N.$N.get($N)",
                keys, longFlags, st)
            .endControlFlow()
          .beginControlFlow("if ($N >= 0 && $N.$N.containsKey($N.substring(0, $N)))",
              ie, keys, longNames, st, ie)
            .addStatement("return $N.$N.get($N.substring(0, $N))",
                keys, longNames, st, ie)
            .endControlFlow()
          .addStatement("return null")
          .endControlFlow();

        builder.beginControlFlow("if ($N.startsWith($S))", token, "-")
          .addStatement("$T $N = $N.substring(1)", STRING, st, token)
          .beginControlFlow("if ($N.isEmpty())", st)
            .addStatement("return null")
            .endControlFlow()
          .beginControlFlow("if ($N.length() == 1 && $N.$N.containsKey($N))", st, keys, shortFlags, st)
            .addStatement("return $N.$N.get($N)",
                keys, shortFlags, st)
            .endControlFlow()
          .beginControlFlow("if ($N.$N.containsKey($N.substring(0, 1)))", keys, shortNames, st)
            .addStatement("return $N.$N.get($N.substring(0, 1))",
                keys, shortNames, st)
            .endControlFlow()
          .endControlFlow();

    builder.addStatement("return null");

    //@formatter:on
    return MethodSpec.methodBuilder("readOption")
        .addParameters(Arrays.asList(keys, token))
        .addModifiers(STATIC, PRIVATE)
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }


  private static MethodSpec trimTokenMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if ($N.startsWith($S))", token, "--")
        .addStatement("return $N.substring(2)", token)
        .endControlFlow()
        .beginControlFlow("if ($N.startsWith($S))", token, "-")
        .addStatement("return $N.substring(1)", token)
        .endControlFlow()
        .addStatement("throw new $T()", AssertionError.class);
    return MethodSpec.methodBuilder("trimToken")
        .addParameter(token)
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec readArgumentMethod(ClassName optionClass,
                                               FieldSpec optionType,
                                               ClassName optionTypeClass,
                                               MethodSpec trimToken) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec st = ParameterSpec.builder(STRING, "st").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec ie = ParameterSpec.builder(INT, "idx_eq").build();
    ParameterSpec next = ParameterSpec.builder(STRING, "next").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N($N)", STRING, st, trimToken, token)
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG)
          .addStatement("return $S", "t")
          .endControlFlow()
        .addStatement("$T $N = $N.indexOf('=')", INT, ie, st)
        .beginControlFlow("if ($N < 0)", ie)
          .beginControlFlow("if ($N.length() > 1)", st)
            .addStatement("return $N.substring(1)", st)
            .endControlFlow()
          .beginControlFlow("if (!$N.hasNext())", it)
            .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing value: ", token)
            .endControlFlow()
          .addStatement("$T $N = $N.next()", STRING, next, it)
          .beginControlFlow("if ($N.startsWith($S))", next, "-")
            .addStatement("throw new $T($S + $N +\n     $S + $N)", IllegalArgumentException.class,
                "The argument to ", token, " may not start with '-', use the long form instead: ", next)
            .endControlFlow()
          .addStatement("return $N", next)
          .endControlFlow()
        .addStatement("return $N.substring($N + 1)", st, ie);
    //@formatter:on
    return MethodSpec.methodBuilder("readArgument")
        .addParameters(Arrays.asList(option, token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec readMethod(ClassName keysClass,
                                       MethodSpec whichOption,
                                       MethodSpec nextArgument,
                                       TypeName optionMapType,
                                       ClassName optionClass,
                                       MethodSpec checkConflict) {
    ParameterSpec keys = ParameterSpec.builder(keysClass, "keys").build();
    ParameterSpec optionMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N($N, $N)", option.type, option, whichOption, keys, token)
        .beginControlFlow("if ($N == null)", option)
          .addStatement("$N.add($N)", otherTokens, token)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$N($N, $N, $N)", checkConflict, optionMap, option, token)
        .addStatement("$T $N = $N($N, $N, $N)", STRING, argument, nextArgument, option, token, it)
        .addStatement("$N.computeIfAbsent($N, $N -> new $T<>()).add($N)",
              optionMap, option, ignore, ArrayList.class, argument);
    //@formatter:on
    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(token, keys, optionMap, otherTokens, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec privateConstructor() {
    return MethodSpec.constructorBuilder()
        .addStatement("throw new $T()", UnsupportedOperationException.class)
        .addModifiers(PRIVATE)
        .build();
  }

  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", Processor.class.getName())
        .build();
  }
}
