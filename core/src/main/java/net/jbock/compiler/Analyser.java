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
import net.jbock.Argument;

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;

final class Analyser {

  private static final ClassName STRING = ClassName.get(String.class);
  private static final ParameterizedTypeName STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);
  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName LIST_OF_ENTRIES = ParameterizedTypeName.get(ClassName.get(List.class),
      ParameterizedTypeName.get(ClassName.get(Map.Entry.class), STRING, STRING));
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();

  private final ExecutableElement constructor;
  private final ClassName generatedClass;
  private static final FieldSpec m = FieldSpec.builder(STRING_MAP, "m", PRIVATE, FINAL)
      .build();


  Analyser(ExecutableElement constructor, ClassName generatedClass) {
    this.constructor = constructor;
    this.generatedClass = generatedClass;
  }

  TypeSpec analyse() {
    check();
    return TypeSpec.classBuilder(generatedClass)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .build())
        .addMethod(MethodSpec.methodBuilder("parse")
            .addCode(bindingCall())
            .addModifiers(PUBLIC)
            .returns(ClassName.get(asType(constructor.getEnclosingElement())))
            .build())
        .addMethod(MethodSpec.methodBuilder("help")
            .addCode(help())
            .returns(LIST_OF_ENTRIES)
            .addModifiers(PUBLIC)
            .build())
        .addModifiers(PUBLIC, FINAL)
        .addField(m)
        .addMethod(privateConstructor())
        .addMethod(MethodSpec.methodBuilder("init")
            .addParameter(ARGS)
            .addCode(initMap())
            .returns(generatedClass)
            .addModifiers(PUBLIC, STATIC)
            .build())
        .build();
  }

  private CodeBlock help() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec entries = ParameterSpec.builder(LIST_OF_ENTRIES, "entries").build();
    builder.addStatement("$T $N = new $T<>($L)", entries.type,
        entries, ArrayList.class, constructor.getParameters().size());
    for (VariableElement variableElement : constructor.getParameters()) {
      builder.addStatement("$N.add(new $T<>($S, $S))", entries, AbstractMap.SimpleImmutableEntry.class,
          name(variableElement), "Description goes here");
    }
    builder.addStatement("return $N", entries);
    return builder.build();
  }

  private CodeBlock initMap() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec map = ParameterSpec.builder(STRING_MAP, m.name)
        .build();
    ParameterSpec i = ParameterSpec.builder(TypeName.INT, "i").build();
    builder.addStatement("$T $N = new $T<>()", map.type, map.name, HashMap.class);
    builder.beginControlFlow("if ($N.length % 2 != 0)", ARGS)
        .addStatement("throw new $T($S)", IllegalArgumentException.class, "Even number of arguments expected")
        .endControlFlow();
    builder.beginControlFlow("for (int $N = 0; $N < $N.length; $N += 2)", i, i, ARGS, i)
        .beginControlFlow("if (!$N[$N].startsWith($S))", ARGS, i, "--")
        .addStatement("throw new $T($S + $N[$N])", IllegalArgumentException.class,
            "Expecting an argument name that starts with '--': ", ARGS, i)
        .endControlFlow()
        .addStatement("$N.put($N[$N].substring(2), $N[$N + 1])", map, ARGS, i, ARGS, i)
        .endControlFlow();
    builder.addStatement("return new $T($N)", generatedClass, map);
    return builder.build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec parameterM = ParameterSpec.builder(STRING_MAP, m.name).build();
    return MethodSpec.constructorBuilder()
        .addParameter(parameterM)
        .addStatement("this.$N = $N", m, parameterM)
        .addModifiers(PRIVATE)
        .build();
  }

  private CodeBlock bindingCall() {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      builder.add("$N.get($S)", m, name(variableElement));
    }
    builder.add(");\n");
    return builder.build();
  }

  private void check() {
    List<? extends VariableElement> parameters = constructor.getParameters();
    Set<String> check = new HashSet<>();
    parameters.forEach(p -> {
      String name = name(p);
      if (!TypeName.get(p.asType()).equals(STRING)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Argument must be String: " + name, p);
      }
      if (!check.add(name)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate name: " + name, p);
      }
    });
  }

  private static String name(VariableElement parameter) {
    Argument annotation = parameter.getAnnotation(Argument.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return parameter.getSimpleName().toString();
    }
  }
}
