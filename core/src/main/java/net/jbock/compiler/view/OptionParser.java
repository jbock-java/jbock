package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Generates the inner class OptionParser.
 */
final class OptionParser {

  static TypeSpec define(Context context) {
    FieldSpec option = FieldSpec.builder(context.optionType(), "option", FINAL).build();
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values", FINAL)
        .initializer("new $T<>()", ArrayList.class)
        .build();
    return TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethod())
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(STREAM_OF_STRING)
            .addStatement("return $N.stream()", values)
            .build())
        .addFields(Arrays.asList(option, values))
        .addMethod(constructor(option))
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec constructor(FieldSpec option) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, option.name).build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", option, optionParam)
        .addParameter(optionParam)
        .build();
  }

  private static MethodSpec readMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it))
        .addStatement("values.add(readValidArgument($N, $N))", token, it)
        .build();
  }
}
