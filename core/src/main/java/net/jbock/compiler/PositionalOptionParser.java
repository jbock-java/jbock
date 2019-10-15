package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Generates the PositionalOptionParser class.
 */
final class PositionalOptionParser {

  static TypeSpec define(Context context) {
    CodeBlock defaultImpl = CodeBlock.builder()
        .addStatement("throw new $T()", AssertionError.class)
        .build();
    FieldSpec option = FieldSpec.builder(context.optionType(), "option", FINAL).build();
    FieldSpec positionalIndex = FieldSpec.builder(Integer.TYPE, "positionalIndex", FINAL).build();
    return TypeSpec.classBuilder(context.positionalOptionParserType())
        .addMethod(readMethod())
        .addMethod(nextPositionMethod())
        .addMethod(MethodSpec.methodBuilder("value")
            .returns(optionalOf(STRING))
            .addCode(defaultImpl)
            .build())
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(STREAM_OF_STRING)
            .addCode(defaultImpl)
            .build())
        .addField(option)
        .addField(positionalIndex)
        .addMethod(constructor(option, positionalIndex))
        .addModifiers(PRIVATE, ABSTRACT, STATIC)
        .build();
  }

  private static MethodSpec constructor(FieldSpec option, FieldSpec positionalIndex) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, option.name).build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", option, optionParam)
        .addStatement("this.$N = $N.positionalIndex.orElseThrow($T::new)", positionalIndex, optionParam, AssertionError.class)
        .addParameter(optionParam)
        .build();
  }

  private static MethodSpec readMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("read")
        .addModifiers(ABSTRACT)
        .addParameter(token)
        .build();
  }

  private static MethodSpec nextPositionMethod() {
    return MethodSpec.methodBuilder("positionIncrement")
        .addModifiers(ABSTRACT)
        .returns(TypeName.INT)
        .build();
  }
}
