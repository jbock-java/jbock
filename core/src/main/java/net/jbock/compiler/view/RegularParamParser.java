package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the RegularParamParser class, which handles non-repeatable params.
 */
final class RegularParamParser {

  static TypeSpec define(Context context) {
    return TypeSpec.classBuilder(context.regularParamParserType())
        .superclass(context.paramParserType())
        .addMethod(readMethod())
        .addModifiers(PRIVATE, STATIC).build();
  }

  private static MethodSpec readMethod() {
    ParameterSpec valueParam = ParameterSpec.builder(STRING, "value").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(valueParam)
        .returns(TypeName.INT)
        .addStatement("values.add($N)", valueParam)
        .addStatement("return $L", 1)
        .build();
  }
}
