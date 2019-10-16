package net.jbock.compiler.optionparser;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Generates the RepeatableOptionParser class.
 */
public final class RepeatableOptionParser {

  public static TypeSpec define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .initializer("new $T<>()", ArrayList.class)
        .build();
    return TypeSpec.classBuilder(context.repeatableOptionParserType())
        .superclass(context.optionParserType())
        .addMethod(readMethod(values))
        .addMethod(MethodSpec.methodBuilder("values")
            .addAnnotation(Override.class)
            .returns(STREAM_OF_STRING)
            .addStatement("return $N.stream()", values)
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
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it));

    spec.addStatement("$N.add(readArgument($N, $N))", values, token, it);

    return spec.addAnnotation(Override.class).build();
  }
}
