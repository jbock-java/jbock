package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class Argument {

  private final ClassName valueClass;
  private final FieldSpec value;
  private final FieldSpec token;

  private Argument(ClassName valueClass, FieldSpec value, FieldSpec token) {
    this.valueClass = valueClass;
    this.value = value;
    this.token = token;
  }

  static Argument create(ClassName valueClass, FieldSpec value, FieldSpec token) {
    return new Argument(valueClass, value, token);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(valueClass)
        .addFields(Arrays.asList(value, token))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec value = ParameterSpec.builder(this.value.type, this.value.name)
        .build();
    ParameterSpec token = ParameterSpec.builder(this.token.type, this.token.name)
        .build();
    builder.addStatement("this.$N = $N", this.value, value);
    builder.addStatement("this.$N = $N", this.token, token);
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(value, token))
        .addCode(builder.build())
        .addModifiers(PRIVATE)
        .build();
  }
}
