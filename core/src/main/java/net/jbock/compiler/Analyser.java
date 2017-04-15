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
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.Names.isFlag;

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PUBLIC, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(STRING, "shortName", PUBLIC, FINAL).build();

  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final ParameterizedTypeName STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);
  private static final ParameterizedTypeName STRING_SET = ParameterizedTypeName.get(
      ClassName.get(Set.class), STRING);
  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();

  private final ExecutableElement constructor;
  private final ClassName generatedClass;
  private final MethodSpec getParam;
  private final MethodSpec getBool;

  private static final FieldSpec SHORT_FLAGS = FieldSpec.builder(STRING_SET, "SHORT_FLAGS")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .build();
  private static final FieldSpec LONG_FLAGS = FieldSpec.builder(STRING_SET, "LONG_FLAGS")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .build();
  private static final FieldSpec LONG_NAMES = FieldSpec.builder(STRING_SET, "LONG_NAMES")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .build();
  private static final FieldSpec SHORT_NAMES = FieldSpec.builder(STRING_SET, "SHORT_NAMES")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .build();

  private final MethodSpec addNext;

  private static final FieldSpec longOptions = FieldSpec.builder(STRING_MAP, "longOptions", PRIVATE, FINAL)
      .build();

  private static final FieldSpec shortOptions = FieldSpec.builder(STRING_MAP, "shortOptions", PRIVATE, FINAL)
      .build();

  private static final FieldSpec trash = FieldSpec.builder(STRING_LIST, "trash", PRIVATE, FINAL)
      .build();

  private final ClassName argumentInfo;
  private final ClassName optionInfo;
  private final MethodSpec getOptions;

  Analyser(ExecutableElement constructor, ClassName generatedClass) {
    this.constructor = constructor;
    this.generatedClass = generatedClass;
    this.argumentInfo = generatedClass.nestedClass("Argument");
    this.optionInfo = generatedClass.nestedClass("Option");
    this.getParam = getParam(optionInfo);
    this.getBool = getBool(optionInfo, getParam);
    this.getOptions = getOptions(optionInfo);
    this.addNext = addNext();
  }

  private static MethodSpec getOptions(ClassName optionInfo) {
    return MethodSpec.methodBuilder("options")
        .addCode(options(optionInfo))
        .returns(ParameterizedTypeName.get(ClassName.get(List.class), optionInfo))
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec getBool(ClassName optionInfo, MethodSpec getParam) {
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("return $T.valueOf($N($N))", Boolean.class, getParam, option);
    return MethodSpec.methodBuilder("getBool")
        .addParameter(option)
        .addCode(builder.build())
        .returns(TypeName.BOOLEAN)
        .addModifiers(PRIVATE)
        .build();
  }

  private static MethodSpec getParam(ClassName optionInfo) {
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    ParameterSpec longValue = ParameterSpec.builder(STRING, "longValue").build();
    ParameterSpec shortValue = ParameterSpec.builder(STRING, "shortValue").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = null, $N = null", STRING, longValue, shortValue);
    builder.beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
        .addStatement("$N = this.$N.get($N.$N)", longValue, longOptions, option, LONG_NAME)
        .endControlFlow();
    builder.beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
        .addStatement("$N = this.$N.get($N.$N)", shortValue, shortOptions, option, SHORT_NAME)
        .endControlFlow();
    builder.beginControlFlow("if ($N == null)", longValue)
        .addStatement("return $N", shortValue)
        .endControlFlow();
    builder.beginControlFlow("else if ($N == null)", shortValue)
        .addStatement("return $N", longValue)
        .endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("throw new $T($S + $N.$N + $S + $N.$N)", IllegalArgumentException.class,
            "Competing arguments: --", option, LONG_NAME, " versus -", option, SHORT_NAME)
        .endControlFlow();
    return MethodSpec.methodBuilder("param")
        .addParameter(option)
        .addCode(builder.build())
        .returns(STRING)
        .addModifiers(PRIVATE)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(generatedClass)
        .addStaticBlock(initSets(constructor))
        .addType(ArgumentInfo.create(optionInfo, argumentInfo).define())
        .addType(OptionInfo.create(constructor, optionInfo).define())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .build())
        .addMethod(MethodSpec.methodBuilder("parse")
            .addCode(bindingCall())
            .addModifiers(PUBLIC)
            .returns(ClassName.get(asType(constructor.getEnclosingElement())))
            .build())
        .addMethod(getParam)
        .addMethod(getBool)
        .addMethod(addNext)
        .addMethod(MethodSpec.methodBuilder("arguments")
            .addCode(arguments())
            .returns(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo))
            .addModifiers(PUBLIC)
            .build())
        .addMethod(MethodSpec.methodBuilder("trash")
            .addStatement("return $N", trash)
            .returns(trash.type)
            .addModifiers(PUBLIC)
            .build())
        .addMethod(getOptions)
        .addModifiers(PUBLIC, FINAL)
        .addFields(Arrays.asList(LONG_FLAGS, SHORT_FLAGS, LONG_NAMES, SHORT_NAMES))
        .addFields(Arrays.asList(longOptions, shortOptions, trash))
        .addMethod(privateConstructor())
        .addMethod(MethodSpec.methodBuilder("init")
            .addParameter(ARGS)
            .addCode(initMaps())
            .returns(generatedClass)
            .addModifiers(PUBLIC, STATIC)
            .build())
        .build();
  }

  private static CodeBlock initSets(ExecutableElement constructor) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec shortFlags = ParameterSpec.builder(STRING_SET, "shortFlags")
        .build();
    ParameterSpec longFlags = ParameterSpec.builder(STRING_SET, "longFlags")
        .build();
    ParameterSpec longNames = ParameterSpec.builder(STRING_SET, "longNames")
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(STRING_SET, "shortNames")
        .build();
    builder.addStatement("$T $N = new $T<>()", longFlags.type, longFlags, HashSet.class);
    builder.addStatement("$T $N = new $T<>()", shortFlags.type, shortFlags, HashSet.class);
    builder.addStatement("$T $N = new $T<>()", longNames.type, longNames, HashSet.class);
    builder.addStatement("$T $N = new $T<>()", shortNames.type, shortNames, HashSet.class);
    List<Names> names = constructor.getParameters()
        .stream()
        .map(Names::create)
        .collect(Collectors.toList());
    names.stream()
        .filter(name -> name.flag)
        .map(name -> name.shortName)
        .filter(Objects::nonNull)
        .forEach(name -> builder.addStatement("$N.add($S)", shortFlags, name));
    names.stream()
        .filter(name -> name.flag)
        .map(name -> name.longName)
        .filter(Objects::nonNull)
        .forEach(name -> builder.addStatement("$N.add($S)", longFlags, name));
    names.stream()
        .filter(name -> !name.flag)
        .map(name -> name.shortName)
        .filter(Objects::nonNull)
        .forEach(name -> builder.addStatement("$N.add($S)", shortNames, name));
    names.stream()
        .filter(name -> !name.flag)
        .map(name -> name.longName)
        .filter(Objects::nonNull)
        .forEach(name -> builder.addStatement("$N.add($S)", longNames, name));
    builder.addStatement("$N = $T.unmodifiableSet($N)", LONG_FLAGS, Collections.class, longFlags);
    builder.addStatement("$N = $T.unmodifiableSet($N)", SHORT_FLAGS, Collections.class, shortFlags);
    builder.addStatement("$N = $T.unmodifiableSet($N)", LONG_NAMES, Collections.class, longNames);
    builder.addStatement("$N = $T.unmodifiableSet($N)", SHORT_NAMES, Collections.class, shortNames);
    return builder.build();
  }

  private CodeBlock initMaps() {
    CodeBlock.Builder builder = CodeBlock.builder();

    ParameterSpec longOpts = ParameterSpec.builder(STRING_MAP, "longOpts").build();
    ParameterSpec shortOpts = ParameterSpec.builder(STRING_MAP, "shortOpts").build();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    builder.addStatement("$T $N = new $T<>()", longOpts.type, longOpts, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortOpts.type, shortOpts, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", trash.type, trash, ArrayList.class);

    // read args
    builder.addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);
    builder.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$N($N, $N, $N, $N)", addNext, longOpts, shortOpts, trash, it)
        .endControlFlow();
    builder.addStatement("return new $T($N, $N, $N)", generatedClass, longOpts, shortOpts, trash);
    return builder.build();
  }

  private static MethodSpec addNext() {
    ParameterSpec longOpts = ParameterSpec.builder(STRING_MAP, "longOpts").build();
    ParameterSpec shortOpts = ParameterSpec.builder(STRING_MAP, "shortOpts").build();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();

    ParameterSpec st = ParameterSpec.builder(STRING, "st").build();
    ParameterSpec set = ParameterSpec.builder(STRING_SET, "set").build();
    ParameterSpec m = ParameterSpec.builder(STRING_MAP, "m").build();

    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N.next()", STRING, s, it)
        .addStatement("$T $N", st.type, st)
        .addStatement("$T $N", set.type, set)
        .addStatement("$T $N", m.type, m)
        .beginControlFlow("if ($N.startsWith($S))", s, "--")
          .addStatement("$N = $N.substring(2)", st, s)
          .beginControlFlow("if ($N.contains($N))", LONG_FLAGS, st)
            .addStatement("$N.put($N, $S)", longOpts, st, Boolean.TRUE.toString())
            .addStatement("return")
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N = $N", set, LONG_NAMES)
            .addStatement("$N = $N", m, longOpts)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else if ($N.startsWith($S))", s, "-")
          .addStatement("$N = $N.substring(1)", st, s)
          .beginControlFlow("if ($N.contains($N))", SHORT_FLAGS, st)
            .addStatement("$N.put($N, $S)", shortOpts, st, Boolean.TRUE.toString())
            .addStatement("return")
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N = $N", set, SHORT_NAMES)
            .addStatement("$N = $N", m, shortOpts)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else")
          .addStatement("$N.add($N)", trash, s)
          .addStatement("return")
          .endControlFlow();

    return MethodSpec.methodBuilder("addNext")
        .addParameters(Arrays.asList(longOpts, shortOpts, trash, it))
        .addCode(builder
            .beginControlFlow("if (!$N.contains($N))", set, st)
              .addStatement("$N.add($N)", trash, s)
              .addStatement("return")
              .endControlFlow()
            .beginControlFlow("if ($N.containsKey($N))", m, st)
              .addStatement("throw new $T($S + $N)",
                  IllegalArgumentException.class, "Duplicate argument: ", st)
              .endControlFlow()
            .beginControlFlow("if (!$N.hasNext())", it)
              .addStatement("$N.add($N)", trash, s)
              .addStatement("return")
              .endControlFlow()
            .addStatement("$N.put($N, $N.next())", m, st, it)
            .build())
        .addModifiers(STATIC, PRIVATE)
        .build();
    //@formatter:on
  }

  private MethodSpec privateConstructor() {
    ParameterSpec ln = ParameterSpec.builder(STRING_MAP, longOptions.name).build();
    ParameterSpec sn = ParameterSpec.builder(STRING_MAP, shortOptions.name).build();
    ParameterSpec tr = ParameterSpec.builder(STRING_LIST, trash.name).build();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(ln, sn, tr))
        .addStatement("this.$N = $T.unmodifiableMap($N)", longOptions, Collections.class, ln)
        .addStatement("this.$N = $T.unmodifiableMap($N)", shortOptions, Collections.class, sn)
        .addStatement("this.$N = $T.unmodifiableList($N)", trash, Collections.class, tr)
        .addModifiers(PRIVATE)
        .build();
  }

  private CodeBlock bindingCall() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec options = ParameterSpec.builder(optionInfo, "options").build();
    builder.addStatement("$T $N = $N()",
        ParameterizedTypeName.get(ClassName.get(List.class), optionInfo),
        options, getOptions);
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      MethodSpec op = isFlag(variableElement) ? getBool : getParam;
      builder.add("$N($N.get($L))", op, options, j);
    }
    builder.add(");\n");
    return builder.build();
  }

  private CodeBlock arguments() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec entries = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo),
        "arguments").build();
    builder.addStatement("$T $N = new $T<>($L)", entries.type,
        entries, ArrayList.class, constructor.getParameters().size());
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    builder.beginControlFlow("for ($T $N : $N())", option.type, option, getOptions)
        .addStatement("$N.add(new $T($N, $N($N)))", entries,
            argumentInfo, option, getParam, option)
        .endControlFlow();
    builder.addStatement("return $N", entries);
    return builder.build();
  }

  private static CodeBlock options(ClassName optionInfo) {
    return CodeBlock.builder()
        .addStatement("return $T.asList($T.values())", Arrays.class, optionInfo)
        .build();
  }
}
