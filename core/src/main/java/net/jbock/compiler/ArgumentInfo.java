package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class ArgumentInfo {

  private static final FieldSpec VALUE = FieldSpec.builder(Analyser.STRING, "value", PUBLIC, FINAL).build();

  private final ClassName optionInfo;
  private final ClassName argumentInfo;
  private final FieldSpec OPTION;

  TypeSpec define() {
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    ParameterSpec value = ParameterSpec.builder(VALUE.type, VALUE.name).build();
    return TypeSpec.classBuilder(argumentInfo)
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addFields(Arrays.asList(OPTION, VALUE))
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameters(Arrays.asList(option, value))
            .addStatement("this.$N = $N", OPTION, option)
            .addStatement("this.$N = $N", VALUE, value)
            .build())
        .build();
  }

  private ArgumentInfo(ClassName optionInfo, ClassName argumentInfo) {
    this.optionInfo = optionInfo;
    this.argumentInfo = argumentInfo;
    this.OPTION = FieldSpec.builder(optionInfo, "option", PUBLIC, FINAL).build();
  }

  static ArgumentInfo create(ClassName optionInfo, ClassName argumentInfo) {
    return new ArgumentInfo(optionInfo, argumentInfo);
  }
}
