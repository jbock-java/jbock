package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;

/**
 * Generates the RepeatableOptionParser class.
 */
final class RepeatableOptionParser {

  static TypeSpec define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values").build();
    return TypeSpec.classBuilder(context.repeatableOptionParserType())
        .superclass(context.optionParserType())
        .addMethod(readMethod(values))
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(LIST_OF_STRING)
            .beginControlFlow("if ($N == null)", values)
            .addStatement("return $T.emptyList()", Collections.class)
            .endControlFlow()
            .addStatement("return $N", values)
            .addAnnotation(Override.class)
            .build())
        .addField(values)
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

  private static MethodSpec readMethod(FieldSpec values) {
    ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it));

    spec.beginControlFlow("if ($N == null)", values)
        .addStatement("$N = new $T<>()", values, ArrayList.class)
        .endControlFlow();

    spec.addStatement("$N.add(readArgument($N, $N))", values, token, it);

    return spec.addAnnotation(Override.class).build();
  }
}
