package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
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

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;

final class Binder {

  private final ClassName binderClass;
  private final ClassName optionClass;
  private final MethodSpec getParam;
  private final FieldSpec optMap;
  private final FieldSpec trash;
  private final ExecutableElement constructor;

  private Binder(ClassName binderClass,
                 ClassName optionClass,
                 MethodSpec getParam,
                 FieldSpec optMap,
                 FieldSpec trash,
                 ExecutableElement constructor) {
    this.binderClass = binderClass;
    this.optionClass = optionClass;
    this.getParam = getParam;
    this.optMap = optMap;
    this.trash = trash;
    this.constructor = constructor;
  }

  static Binder create(ClassName binderClass,
                       ClassName optionClass,
                       FieldSpec optMap,
                       FieldSpec trash,
                       FieldSpec value,
                       ExecutableElement constructor) {
    return new Binder(binderClass, optionClass,
        getParamMethod(optMap, optionClass, value),
        optMap, trash, constructor);
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
    ParameterSpec options = ParameterSpec.builder(ArrayTypeName.of(optionClass), "options").build();
    builder.addStatement("$T $N = $T.values()",
        options.type, options, optionClass);
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      OptionType optionType = Names.getOptionType(variableElement);
      if (optionType == OptionType.FLAG) {
        builder.add("$N.containsKey($N[$L])", optMap, options, j);
      } else if (optionType == OptionType.STRING) {
        builder.add("$N($N[$L])", getParam, options, j);
      } else {
        builder.add("$N.getOrDefault($N[$L], $T.emptyList())", optMap, options, j, Collections.class);
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
