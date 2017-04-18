package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.STRING;

final class Argument {

  private static final FieldSpec ATOMIC = FieldSpec.builder(TypeName.BOOLEAN, "atomic")
      .addModifiers(PUBLIC, FINAL).build();

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
        .addFields(Arrays.asList(value, token, ATOMIC))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(reconstructMethod())
        .build();
  }

  private MethodSpec reconstructMethod() {
    CodeBlock.Builder builder = CodeBlock.builder();
    //@formatter:off
    builder.beginControlFlow("if ($N)", ATOMIC)
          .addStatement("return new $T[]{ this.$N }", STRING, token)
          .endControlFlow()
        .addStatement("return new $T[]{ this.$N, this.$N }", STRING, token, value);
    //@formatter:on
    return MethodSpec.methodBuilder("reconstruct")
        .addCode(builder.build())
        .returns(ArrayTypeName.of(STRING))
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec value = ParameterSpec.builder(this.value.type, this.value.name)
        .build();
    ParameterSpec token = ParameterSpec.builder(this.token.type, this.token.name)
        .build();
    ParameterSpec atomic = ParameterSpec.builder(ATOMIC.type, ATOMIC.name).build();
    builder.addStatement("this.$N = $N", this.value, value);
    builder.addStatement("this.$N = $N", this.token, token);
    builder.addStatement("this.$N = $N", ATOMIC, atomic);
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(value, token, atomic))
        .addCode(builder.build())
        .addModifiers(PRIVATE)
        .build();
  }
}
