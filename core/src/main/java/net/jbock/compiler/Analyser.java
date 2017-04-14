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
import net.jbock.Description;
import net.jbock.Flag;

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

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);
  private static final ParameterizedTypeName STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);
  private static final ParameterizedTypeName STRING_SET = ParameterizedTypeName.get(
      ClassName.get(Set.class), STRING);
  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  private static final MethodSpec ADD_NEXT = addNext();

  private final ExecutableElement constructor;
  private final ClassName generatedClass;
  private final MethodSpec getParam = getParam();

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


  private static final FieldSpec longNames = FieldSpec.builder(STRING_MAP, "longOptions", PUBLIC, FINAL)
      .build();

  private static final FieldSpec shortNames = FieldSpec.builder(STRING_MAP, "shortOptions", PUBLIC, FINAL)
      .build();

  private final ClassName argumentInfo;

  Analyser(ExecutableElement constructor, ClassName generatedClass) {
    this.constructor = constructor;
    this.generatedClass = generatedClass;
    this.argumentInfo = generatedClass.nestedClass("Argument");
    ParameterSpec ln = ParameterSpec.builder(STRING, "ln").build();
    ParameterSpec sn = ParameterSpec.builder(STRING, "sn").build();
    ParameterSpec lv = ParameterSpec.builder(STRING, "lv").build();
    ParameterSpec sv = ParameterSpec.builder(STRING, "sv").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = this.$N.get($N)", STRING, lv, longNames, ln);
    builder.addStatement("$T $N = this.$N.get($N)", STRING, sv, shortNames, sn);
    builder.beginControlFlow("if ($N == null)", lv)
        .addStatement("return $N", sv)
        .endControlFlow();
    builder.beginControlFlow("else if ($N == null)", sv)
        .addStatement("return $N", lv)
        .endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("throw new $T($S + $N + $S + $N)", IllegalArgumentException.class,
            "Competing arguments: --", ln, " versus -", sn)
        .endControlFlow();
  }

  private static MethodSpec getParam() {
    ParameterSpec longName = ParameterSpec.builder(STRING, "longName").build();
    ParameterSpec shortName = ParameterSpec.builder(STRING, "shortName").build();
    ParameterSpec longValue = ParameterSpec.builder(STRING, "longValue").build();
    ParameterSpec shortValue = ParameterSpec.builder(STRING, "shortValue").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = null, $N = null", STRING, longValue, shortValue);
    builder.beginControlFlow("if ($N != null)", longName)
        .addStatement("$N = this.$N.get($N)", longValue, longNames, longName)
        .endControlFlow();
    builder.beginControlFlow("if ($N != null)", shortName)
        .addStatement("$N = this.$N.get($N)", shortValue, shortNames, shortName)
        .endControlFlow();
    builder.beginControlFlow("if ($N == null)", longValue)
        .addStatement("return $N", shortValue)
        .endControlFlow();
    builder.beginControlFlow("else if ($N == null)", shortValue)
        .addStatement("return $N", longValue)
        .endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("throw new $T($S + $N + $S + $N)", IllegalArgumentException.class,
            "Competing arguments: --", longName, " versus -", shortName)
        .endControlFlow();
    return MethodSpec.methodBuilder("param")
        .addParameters(Arrays.asList(longName, shortName))
        .addCode(builder.build())
        .returns(STRING)
        .addModifiers(PRIVATE)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(generatedClass)
        .addStaticBlock(initSets(constructor))
        .addType(ArgumentInfo.define(argumentInfo))
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .build())
        .addMethod(MethodSpec.methodBuilder("parse")
            .addCode(bindingCall())
            .addModifiers(PUBLIC)
            .returns(ClassName.get(asType(constructor.getEnclosingElement())))
            .build())
        .addMethod(getParam)
        .addMethod(ADD_NEXT)
        .addMethod(MethodSpec.methodBuilder("summary")
            .addCode(summary())
            .returns(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo))
            .addModifiers(PUBLIC)
            .build())
        .addModifiers(PUBLIC, FINAL)
        .addFields(Arrays.asList(LONG_FLAGS, SHORT_FLAGS, LONG_NAMES, SHORT_NAMES))
        .addField(longNames)
        .addField(shortNames)
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

    builder.addStatement("$T $N = new $T<>()", longOpts.type, longOpts, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortOpts.type, shortOpts, HashMap.class);

    // read args
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it")
        .build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s")
        .build();
    builder.addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);
    //@formatter:off
    builder.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, s, it)
        .beginControlFlow("if ($N.startsWith($S))", s, "--")
          .beginControlFlow("if ($N.contains($N.substring(2)))", LONG_FLAGS, s)
            .addStatement("$N.put($N.substring(2), $S)", longOpts, s, Boolean.TRUE.toString())
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N($N, $N, $N.substring(2), $N)", ADD_NEXT, LONG_NAMES, longOpts, s, it)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else if ($N.startsWith($S))", s, "-")
          .beginControlFlow("if ($N.contains($N.substring(1)))", SHORT_FLAGS, s)
            .addStatement("$N.put($N.substring(1), $S)", shortOpts, s, Boolean.TRUE.toString())
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N($N, $N, $N.substring(1), $N)", ADD_NEXT, SHORT_NAMES, shortOpts, s, it)
            .endControlFlow()
          .endControlFlow()
     .endControlFlow();
    //@formatter:on
    builder.addStatement("return new $T($N, $N)", generatedClass, longOpts, shortOpts);
    return builder.build();
  }

  private static MethodSpec addNext() {
    ParameterSpec set = ParameterSpec.builder(STRING_SET, "set").build();
    ParameterSpec m = ParameterSpec.builder(STRING_MAP, "m").build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    //@formatter:off
    return MethodSpec.methodBuilder("addNext")
        .addParameters(Arrays.asList(set, m, s, it))
        .addCode(CodeBlock.builder()
            .beginControlFlow("if (!$N.contains($N))", set, s)
              .addStatement("return")
              .endControlFlow()
            .beginControlFlow("if ($N.containsKey($N))", m, s)
              .addStatement("throw new $T($S + $N)",
                  IllegalArgumentException.class, "Duplicate argument: ", s)
              .endControlFlow()
            .beginControlFlow("if (!$N.hasNext())", it)
              .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Expecting argument value: ", s)
              .endControlFlow()
            .addStatement("$N.put($N, $N.next())", m, s, it)
            .build())
        .addModifiers(STATIC, PRIVATE)
        .build();
    //@formatter:on
  }

  private MethodSpec privateConstructor() {
    ParameterSpec ln = ParameterSpec.builder(STRING_MAP, longNames.name).build();
    ParameterSpec sn = ParameterSpec.builder(STRING_MAP, shortNames.name).build();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(ln, sn))
        .addStatement("this.$N = $T.unmodifiableMap($N)", longNames, Collections.class, ln)
        .addStatement("this.$N = $T.unmodifiableMap($N)", shortNames, Collections.class, sn)
        .addModifiers(PRIVATE)
        .build();
  }

  private CodeBlock bindingCall() {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      Names names = Names.create(variableElement);
      if (j > 0) {
        builder.add(",\n    ");
      }
      builder.add("$N($S, $S)", getParam, names.longName, names.shortName);
    }
    builder.add(");\n");
    return builder.build();
  }

  private CodeBlock summary() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec entries = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo),
        "arguments").build();
    builder.addStatement("$T $N = new $T<>($L)", entries.type,
        entries, ArrayList.class, constructor.getParameters().size());
    for (VariableElement variableElement : constructor.getParameters()) {
      Names names = Names.create(variableElement);
      boolean flag = variableElement.getAnnotation(Flag.class) != null;
      Description description = variableElement.getAnnotation(Description.class);
      String desc = description == null ? "" : description.value();
      builder.addStatement("$N.add(new $T($S, $S, $L, $S, $N($S, $S)))", entries,
          argumentInfo,
          names.longName, names.shortName,
          flag, desc, getParam,
          names.longName, names.shortName);
    }
    builder.addStatement("return $N", entries);
    return builder.build();
  }
}
