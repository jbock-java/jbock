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

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PUBLIC, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(STRING, "shortName", PUBLIC, FINAL).build();

  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  private static final FieldSpec trash = FieldSpec.builder(STRING_LIST, "trash", PRIVATE, FINAL)
      .build();

  private final ExecutableElement constructor;

  private final ClassName generatedClass;
  private final ClassName binderClass;
  private final Option option;
  private final ClassName optionTypeClass;
  private final ClassName keysClass;
  private final ClassName argumentClass;

  private final MethodSpec addNext;
  private final MethodSpec whichOption;
  private final MethodSpec checkConflict;

  private final FieldSpec shortFlags;
  private final FieldSpec longFlags;
  private final FieldSpec longNames;
  private final FieldSpec shortNames;
  private final FieldSpec optMap;
  private final FieldSpec value;
  private final FieldSpec token;
  private final FieldSpec listInitializer;
  private final FieldSpec optionType;

  private final TypeName optionMapType;

  Analyser(ExecutableElement constructor, ClassName generatedClass) {
    this.constructor = constructor;
    this.generatedClass = generatedClass;
    this.keysClass = generatedClass.nestedClass("Keys");
    this.argumentClass = generatedClass.nestedClass("Argument");
    this.binderClass = generatedClass.nestedClass("Binder");
    this.optionTypeClass = generatedClass.nestedClass("OptionType");
    ParameterizedTypeName listOfArgumentType = ParameterizedTypeName.get(
        ClassName.get(List.class), argumentClass);
    this.optionType = FieldSpec.builder(optionTypeClass, "type", PUBLIC, FINAL).build();
    this.option = Option.create(constructor, generatedClass.nestedClass("Option"), optionTypeClass, optionType);
    this.optionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, listOfArgumentType);
    ParameterizedTypeName listInitType = ParameterizedTypeName.get(
        ClassName.get(Function.class), option.optionClass, listOfArgumentType);
    ParameterSpec o = ParameterSpec.builder(option.optionClass, "o").build();
    this.listInitializer = FieldSpec.builder(listInitType, "NEW_LIST")
        .initializer("$N -> new $T<>()", o, ArrayList.class)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
    TypeName soType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    TypeName entryType = ParameterizedTypeName.get(
        ClassName.get(AbstractMap.Entry.class), option.optionClass, STRING);
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
    this.value = FieldSpec.builder(STRING, "value").addModifiers(PUBLIC, FINAL).build();
    this.token = FieldSpec.builder(STRING, "token").addModifiers(PUBLIC, FINAL).build();
    this.whichOption = whichOptionMethod(keysClass, longFlags, shortFlags, longNames, shortNames, entryType);
    this.checkConflict = checkConflictMethod(optionMapType, option.optionClass, optionTypeClass, optionType);
    this.addNext = addNextMethod(keysClass, whichOption, entryType, optionMapType, argumentClass,
        option.optionClass, checkConflict, listInitializer, optionType, optionTypeClass);
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
    return TypeSpec.classBuilder(generatedClass)
        .addType(Keys.create(option.optionClass, optionTypeClass, keysClass, longFlags,
            shortFlags, longNames, shortNames, optionType).define())
        .addType(Option.create(constructor, option.optionClass, optionTypeClass, optionType).define())
        .addType(Argument.create(argumentClass, value, token).define())
        .addType(Binder.create(binderClass, option, argumentClass, optMap, trash, value, constructor).define())
        .addType(OptionType.define(optionTypeClass))
        .addAnnotation(generatedAnnotation())
        .addField(listInitializer)
        .addMethod(privateConstructor())
        .addMethod(checkConflict)
        .addMethod(addNext)
        .addMethod(whichOption)
        .addMethod(parseMethod())
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec keys = ParameterSpec.builder(keysClass, "keys").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    builder.addStatement("$T $N = new $T<>()", trash.type, trash, ArrayList.class);
    builder.addStatement("$T $N = new $T()", keys.type, keys, keysClass);
    builder.addStatement("$T $N = new $T<>($T.class)", optMap.type, optMap, EnumMap.class, option.optionClass);

    // read args
    builder.addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);
    builder.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$N($N, $N, $N, $N)", addNext, keys, optMap, trash, it)
        .endControlFlow();
    builder.addStatement("return new $T($N, $N)", binderClass, optMap, trash);
    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .returns(binderClass)
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec whichOptionMethod(ClassName keysClass,
                                              FieldSpec longFlags, FieldSpec shortFlags,
                                              FieldSpec longNames, FieldSpec shortNames,
                                              TypeName entryType) {
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
            .addStatement("return new $T<>($N.$N.get($N), $N)",
                SimpleImmutableEntry.class, keys, longFlags, st, token)
            .endControlFlow()
          .beginControlFlow("if ($N >= 0 && $N.$N.containsKey($N.substring(0, $N)))",
              ie, keys, longNames, st, ie)
            .addStatement("return new $T<>($N.$N.get($N.substring(0, $N)), $N)",
                SimpleImmutableEntry.class, keys, longNames, st, ie, token)
            .endControlFlow()
          .addStatement("return null")
          .endControlFlow();

        builder.beginControlFlow("if ($N.startsWith($S))", token, "-")
          .addStatement("$T $N = $N.substring(1)", STRING, st, token)
          .beginControlFlow("if ($N.isEmpty())", st)
            .addStatement("return null")
            .endControlFlow()
          .beginControlFlow("if ($N.length() == 1 && $N.$N.containsKey($N))", st, keys, shortFlags, st)
            .addStatement("return new $T<>($N.$N.get($N), $N)",
                SimpleImmutableEntry.class, keys, shortFlags, st, token)
            .endControlFlow()
          .beginControlFlow("if ($N.$N.containsKey($N.substring(0, 1)))", keys, shortNames, st)
            .addStatement("return new $T<>($N.$N.get($N.substring(0, 1)), $N)",
                SimpleImmutableEntry.class, keys, shortNames, st, token)
            .endControlFlow()
          .endControlFlow();

    builder.addStatement("return null");

    //@formatter:on
    return MethodSpec.methodBuilder("whichOption")
        .addParameters(Arrays.asList(keys, token))
        .addModifiers(STATIC, PRIVATE)
        .returns(entryType)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec addNextMethod(ClassName keysClass, MethodSpec whichOption,
                                          TypeName entryType, TypeName optionMapType,
                                          ClassName argumentClass,
                                          ClassName optionClass,
                                          MethodSpec checkConflict,
                                          FieldSpec listInitializer,
                                          FieldSpec optionType,
                                          ClassName optionTypeClass) {
    ParameterSpec keys = ParameterSpec.builder(keysClass, "keys").build();
    ParameterSpec optionMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec entry = ParameterSpec.builder(entryType, "entry").build();
    ParameterSpec ie = ParameterSpec.builder(INT, "idx_eq").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    //@formatter:off
    CodeBlock block = CodeBlock.builder()
        .addStatement("$T $N = $N.next()", STRING, token, it)
        .addStatement("$T $N = $N($N, $N)", entry.type, entry, whichOption, keys, token)
        .beginControlFlow("if ($N == null)", entry)
          .addStatement("$N.add($N)", trash, token)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$T $N = $N.getKey()", option.type, option, entry)
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG)
          .addStatement("$N($N, $N, $N)", checkConflict, optionMap, option, token)
          .addStatement("$N.computeIfAbsent($N, $N).add(new $T($S, $N, $L))",
              optionMap, option, listInitializer, argumentClass, "t", token, true)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$T $N = $N.getValue().indexOf('=')", INT, ie, entry)
        .beginControlFlow("if ($N < 0)", ie)
          .beginControlFlow("if ($N.getValue().length() > 2)", entry)
            .addStatement("$N($N, $N, $N)", checkConflict, optionMap, option, token)
            .addStatement("$N.computeIfAbsent($N, $N).add(new $T($N.getValue().substring(2), $N, $L))",
                optionMap, option, listInitializer, argumentClass, entry, token, true)
            .addStatement("return")
            .endControlFlow()
          .beginControlFlow("if (!$N.hasNext())", it)
            .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing value: ", token)
            .endControlFlow()
          .addStatement("$N($N, $N, $N)", checkConflict, optionMap, option, token)
          .addStatement("$N.computeIfAbsent($N, $N).add(new $T($N.next(), $N, $L))",
              optionMap, option, listInitializer, argumentClass, it, token, false)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$N($N, $N, $N)", checkConflict, optionMap, option, token)
        .addStatement("$N.computeIfAbsent($N, $N).add(new $T($N.getValue().substring($N + 1), $N, $L))",
            optionMap, option, listInitializer, argumentClass, entry, ie, token, true)
        .build();

    //@formatter:on
    return MethodSpec.methodBuilder("addNext")
        .addParameters(Arrays.asList(keys, optionMap, trash, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(block)
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
