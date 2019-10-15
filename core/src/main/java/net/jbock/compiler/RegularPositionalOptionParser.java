package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Generates the RegularPositionalOptionParser class.
 */
final class RegularPositionalOptionParser {

  static TypeSpec define(Context context) {
    FieldSpec value = FieldSpec.builder(STRING, "value").build();
    return TypeSpec.classBuilder(context.regularPositionalOptionParserType())
        .superclass(context.positionalOptionParserType())
        .addMethod(readMethod(value))
        .addMethod(nextPositionMethod())
        .addMethod(MethodSpec.methodBuilder("value")
            .returns(optionalOf(STRING))
            .addStatement("return $T.ofNullable($N)", Optional.class, value)
            .addAnnotation(Override.class)
            .build())
        .addField(value)
        .addMethod(constructor(context))
        .addModifiers(PRIVATE, STATIC).build();
  }

  private static MethodSpec constructor(Context context) {
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    return MethodSpec.constructorBuilder()
        .addStatement("super($N)", optionParam)
        .addParameter(optionParam)
        .build();
  }

  private static MethodSpec readMethod(FieldSpec value) {
    ParameterSpec valueParam = ParameterSpec.builder(STRING, "value").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(valueParam)
        .addStatement("this.$N = $N", value, valueParam)
        .addAnnotation(Override.class)
        .build();
  }

  private static MethodSpec nextPositionMethod() {
    return MethodSpec.methodBuilder("positionIncrement")
        .addAnnotation(Override.class)
        .addStatement("return $L", 1)
        .returns(TypeName.INT)
        .build();
  }
}
