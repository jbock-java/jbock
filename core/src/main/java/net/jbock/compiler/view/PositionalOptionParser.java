package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Generates the PositionalOptionParser class.
 */
final class PositionalOptionParser {

  public static TypeSpec define(Context context) {
    CodeBlock defaultImpl = CodeBlock.builder()
        .addStatement("throw new $T()", AssertionError.class)
        .build();
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
        .addModifiers(PRIVATE, ABSTRACT, STATIC)
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
