package net.jbock.compiler;

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

final class ArgumentInfo {

  private static final FieldSpec LONG_NAME = FieldSpec.builder(Analyser.STRING, "longName", PUBLIC, FINAL).build();
  private static final FieldSpec SHORT_NAME = FieldSpec.builder(Analyser.STRING, "shortName", PUBLIC, FINAL).build();
  private static final FieldSpec IS_FLAG = FieldSpec.builder(TypeName.BOOLEAN, "flag", PUBLIC, FINAL).build();
  private static final FieldSpec DESCRIPTION = FieldSpec.builder(Analyser.STRING, "description", PUBLIC, FINAL).build();
  private static final FieldSpec VALUE = FieldSpec.builder(Analyser.STRING, "value", PUBLIC, FINAL).build();

  static TypeSpec define(ClassName argumentInfo) {
    ParameterSpec longName = ParameterSpec.builder(LONG_NAME.type, LONG_NAME.name).build();
    ParameterSpec shortName = ParameterSpec.builder(SHORT_NAME.type, SHORT_NAME.name).build();
    ParameterSpec isFlag = ParameterSpec.builder(IS_FLAG.type, IS_FLAG.name).build();
    ParameterSpec description = ParameterSpec.builder(DESCRIPTION.type, DESCRIPTION.name).build();
    ParameterSpec value = ParameterSpec.builder(VALUE.type, VALUE.name).build();
    return TypeSpec.classBuilder(argumentInfo)
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addFields(Arrays.asList(LONG_NAME, SHORT_NAME, IS_FLAG, DESCRIPTION, VALUE))
        .addMethod(MethodSpec.methodBuilder("describe")
            .addModifiers(PUBLIC)
            .returns(Analyser.STRING)
            .addCode(describeMethod())
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameters(Arrays.asList(longName, shortName, isFlag, description, value))
            .addStatement("this.$N = $N", LONG_NAME, longName)
            .addStatement("this.$N = $N", SHORT_NAME, shortName)
            .addStatement("this.$N = $N", IS_FLAG, isFlag)
            .addStatement("this.$N = $N", DESCRIPTION, description)
            .addStatement("this.$N = $N", VALUE, value)
            .build())
        .build();
  }

  private static CodeBlock describeMethod() {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    //@formatter:off
    return CodeBlock.builder()
        .addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class)
        .beginControlFlow("if ($N)", IS_FLAG)
          .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .addStatement("$N.append(',').append(' ').append('-').append('-').append($N)", sb, LONG_NAME)
            .endControlFlow()
          .beginControlFlow("else if ($N != null)", LONG_NAME)
            .addStatement("$N.append('-').append('-').append($N)", sb, LONG_NAME)
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .endControlFlow()
        .endControlFlow()
        .beginControlFlow("else")
          .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .addStatement("$N.append(',').append(' ').append('-').append('-').append($N).append(' ').append($S)", sb, LONG_NAME, "VAL")
            .endControlFlow()
          .beginControlFlow("else if ($N != null)", LONG_NAME)
            .addStatement("$N.append('-').append('-').append($N).append(' ').append($S)", sb, LONG_NAME, "VAL")
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N.append('-').append($N).append(' ').append($S)", sb, SHORT_NAME, "VAL")
            .endControlFlow()
        .endControlFlow()
        .addStatement("$N.append('\\n').append($S)", sb, "  ")
        .addStatement("$N.append($N)", sb, DESCRIPTION)
        .addStatement("return $N.toString()", sb)
        .build();
    //@formatter:on
  }

  private ArgumentInfo() {
    throw new UnsupportedOperationException();
  }
}
