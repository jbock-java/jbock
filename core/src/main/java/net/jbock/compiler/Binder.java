package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;

final class Binder {

  private final ClassName binderClass;
  private final Option option;
  private final ClassName argumentClass;
  private final MethodSpec getParam;
  private final FieldSpec optMap;
  private final FieldSpec trash;
  private final FieldSpec value;
  private final ExecutableElement constructor;

  private Binder(ClassName binderClass,
                 Option option,
                 ClassName argumentClass, MethodSpec getParam,
                 FieldSpec optMap,
                 FieldSpec trash,
                 FieldSpec value, ExecutableElement constructor) {
    this.binderClass = binderClass;
    this.option = option;
    this.argumentClass = argumentClass;
    this.getParam = getParam;
    this.optMap = optMap;
    this.trash = trash;
    this.value = value;
    this.constructor = constructor;
  }

  static Binder create(ClassName binderClass,
                       Option optionClass,
                       ClassName argumentClass,
                       FieldSpec optMap,
                       FieldSpec trash,
                       FieldSpec value,
                       ExecutableElement constructor) {
    return new Binder(binderClass, optionClass,
        argumentClass, getParamMethod(optMap, optionClass.optionClass, value),
        optMap, trash, value, constructor);
  }

  private static MethodSpec getParamMethod(FieldSpec optMap, ClassName optionClass, FieldSpec value) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    //@formatter:off
    CodeBlock block = CodeBlock.builder()
        .beginControlFlow("if (!$N.containsKey($N))", optMap, option)
          .addStatement("return null")
          .endControlFlow()
        .addStatement("return $N.get($N).get(0).$N", optMap, option, value)
        .build();
    //@formatter:on
    return MethodSpec.methodBuilder("param")
        .addParameter(option)
        .addCode(block)
        .returns(Analyser.STRING)
        .addModifiers(PRIVATE)
        .build();
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(binderClass)
        .addFields(Arrays.asList(optMap, trash))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(getParam)
        .addMethod(bindMethod())
        .addMethod(argumentsMethod())
        .addMethod(trashMethod())
        .build();
  }

  private MethodSpec bindMethod() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec a = ParameterSpec.builder(argumentClass, "a").build();
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      OptionType optionType = Names.getOptionType(variableElement);
      if (optionType == OptionType.FLAG) {
        builder.add("$N.containsKey($T.$N)", optMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.STRING) {
        builder.add("$N.getOrDefault($T.$L, $T.emptyList()).stream()\n",
            optMap, option.optionClass, option.enumConstant(j), Collections.class)
            .add("        .map($N -> $N.$N)\n", a, a, value)
            .add("        .findFirst().orElse(null)");
      } else {
        builder.add("$N.getOrDefault($T.$L, $T.emptyList()).stream()\n",
            optMap, option.optionClass, option.enumConstant(j), Collections.class)
            .add("        .map($N -> $N.$N)\n", a, a, value)
            .add("        .collect($T.toList())", Collectors.class);
      }
    }
    builder.add(");\n");
    return MethodSpec.methodBuilder("bind")
        .addCode(builder.build())
        .addModifiers(PUBLIC)
        .returns(ClassName.get(asType(constructor.getEnclosingElement())))
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec optMap = ParameterSpec.builder(this.optMap.type, this.optMap.name)
        .build();
    ParameterSpec trash = ParameterSpec.builder(this.trash.type, this.trash.name)
        .build();
    builder.addStatement("this.$N = $T.unmodifiableList($N)", this.trash, Collections.class, trash);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", this.optMap, Collections.class, optMap);
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(optMap, trash))
        .addCode(builder.build())
        .addModifiers(PRIVATE)
        .build();
  }

  private MethodSpec argumentsMethod() {
    return MethodSpec.methodBuilder("arguments")
        .addStatement("return $N", optMap)
        .returns(optMap.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec trashMethod() {
    return MethodSpec.methodBuilder("trash")
        .addStatement("return $N", trash)
        .returns(trash.type)
        .addModifiers(PUBLIC)
        .build();
  }
}
