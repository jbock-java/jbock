package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;

import static com.squareup.javapoet.ParameterSpec.builder;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Generates the inner class OptionParser, which handles repeatable options.
 */
final class OptionParser {

  static TypeSpec define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .initializer("new $T<>()", ArrayList.class)
        .build();
    return TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethod(context))
        .addField(values)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec readMethod(Context context) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("read")
        .addParameters(asList(option, token, it))
        .addStatement("values.add(readOptionArgument($N, $N))", token, it)
        .build();
  }
}
