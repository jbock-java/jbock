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
import net.jbock.LongName;
import net.jbock.ShortName;

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private final ExecutableElement constructor;
  private final ClassName generatedClass;
  private final MethodSpec getParam;

  private static final FieldSpec longNames = FieldSpec.builder(STRING_MAP, "longNames", PRIVATE, FINAL)
      .build();

  private static final FieldSpec shortNames = FieldSpec.builder(STRING_MAP, "shortNames", PRIVATE, FINAL)
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
    this.getParam = MethodSpec.methodBuilder("param")
        .addParameter(ln)
        .addParameter(sn)
        .addCode(builder.build())
        .returns(STRING)
        .addModifiers(PRIVATE)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(generatedClass)
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
        .addMethod(MethodSpec.methodBuilder("help")
            .addCode(help())
            .returns(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo))
            .addModifiers(PUBLIC)
            .build())
        .addModifiers(PUBLIC, FINAL)
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

  private CodeBlock initMaps() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec ln = ParameterSpec.builder(STRING_MAP, longNames.name)
        .build();
    ParameterSpec sn = ParameterSpec.builder(STRING_MAP, shortNames.name)
        .build();
    ParameterSpec sf = ParameterSpec.builder(STRING_SET, "shortFlags")
        .build();
    ParameterSpec lf = ParameterSpec.builder(STRING_SET, "longFlags")
        .build();

    // init maps
    builder.addStatement("$T $N = new $T<>()", ln.type, ln, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", sn.type, sn, HashMap.class);

    // init flag sets
    builder.addStatement("$T $N = new $T<>()", lf.type, lf, HashSet.class);
    builder.addStatement("$T $N = new $T<>()", sf.type, sf, HashSet.class);
    for (VariableElement variableElement : constructor.getParameters()) {
      if (variableElement.getAnnotation(Flag.class) != null) {
        LongName longName = variableElement.getAnnotation(LongName.class);
        ShortName shortName = variableElement.getAnnotation(ShortName.class);
        if (longName != null) {
          builder.addStatement("$N.add($S)", lf, longName.value());
        } else {
          builder.addStatement("$N.add($S)", lf, variableElement.getSimpleName().toString());
        }
        if (shortName != null) {
          builder.addStatement("$N.add($S)", sf, shortName.value());
        } else {
          builder.addStatement("$N.add($S)", sf, variableElement.getSimpleName().toString());
        }
      }
    }

    // read args
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it")
        .build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s")
        .build();
    builder.addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);
    //@formatter:off
    builder.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, s, it)
        .beginControlFlow("if ($N.startsWith($S))", s, "---")
          .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
              "Argument may not start with '---': ", s)
          .endControlFlow()
        .beginControlFlow("else if ($N.startsWith($S))", s, "--")
          .beginControlFlow("if ($N.contains($N.substring(2)))", lf, s)
            .addStatement("$N.put($N.substring(2), $S)", ln, s, Boolean.TRUE.toString())
            .endControlFlow()
          .beginControlFlow("else")
            .add(nextCheck(s, it))
            .addStatement("$N.put($N.substring(2), $N.next())", ln, s, it)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else if ($N.startsWith($S))", s, "-")
          .beginControlFlow("if ($N.contains($N.substring(1)))", sf, s)
            .addStatement("$N.put($N.substring(1), $S)", sn, s, Boolean.TRUE.toString())
            .endControlFlow()
          .beginControlFlow("else")
            .add(nextCheck(s, it))
            .addStatement("$N.put($N.substring(1), $N.next())", sn, s, it)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else")
          .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
              "Expecting an argument that starts with '-' or '--': ", s)
          .endControlFlow()
     .endControlFlow();
    //@formatter:on
    builder.addStatement("return new $T($N, $N)", generatedClass, ln, sn);
    return builder.build();
  }

  private CodeBlock nextCheck(ParameterSpec s, ParameterSpec it) {
    return CodeBlock.builder()
        .beginControlFlow("if (!$N.hasNext())", it)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Expecting argument value: ", s)
        .endControlFlow()
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec ln = ParameterSpec.builder(STRING_MAP, longNames.name).build();
    ParameterSpec sn = ParameterSpec.builder(STRING_MAP, shortNames.name).build();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(ln, sn))
        .addStatement("this.$N = $N", longNames, ln)
        .addStatement("this.$N = $N", shortNames, sn)
        .addModifiers(PRIVATE)
        .build();
  }

  private CodeBlock bindingCall() {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      LongName longName = variableElement.getAnnotation(LongName.class);
      ShortName shortName = variableElement.getAnnotation(ShortName.class);
      String ln = longName == null ? variableElement.getSimpleName().toString() : longName.value();
      String sn = shortName == null ? variableElement.getSimpleName().toString() : shortName.value();
      if (j > 0) {
        builder.add(",\n    ");
      }
      builder.add("$N($S, $S)", getParam, ln, sn);
    }
    builder.add(");\n");
    return builder.build();
  }

  private CodeBlock help() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec entries = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo),
        "arguments").build();
    builder.addStatement("$T $N = new $T<>($L)", entries.type,
        entries, ArrayList.class, constructor.getParameters().size());
    for (VariableElement variableElement : constructor.getParameters()) {
      LongName longName = variableElement.getAnnotation(LongName.class);
      ShortName shortName = variableElement.getAnnotation(ShortName.class);
      boolean flag = variableElement.getAnnotation(Flag.class) != null;
      Description description = variableElement.getAnnotation(Description.class);
      String ln = longName == null ? variableElement.getSimpleName().toString() : longName.value();
      String sn = shortName == null ? variableElement.getSimpleName().toString() : shortName.value();
      String desc = description == null ? "" : description.value();
      builder.addStatement("$N.add(new $T($S, $S, $L, $S, $N($S, $S)))", entries,
          argumentInfo, ln, sn, flag, desc, getParam, ln, sn);
    }
    builder.addStatement("return $N", entries);
    return builder.build();
  }
}
