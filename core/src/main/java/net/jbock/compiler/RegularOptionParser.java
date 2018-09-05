package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Helper.throwRepetitionErrorStatement;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Generates the RegularOptionParser class.
 */
final class RegularOptionParser {

  static TypeSpec define(Context context) {
    FieldSpec value = FieldSpec.builder(STRING, "value").build();
    return TypeSpec.classBuilder(context.generatedClass.nestedClass("RegularOptionParser"))
        .superclass(context.optionParserType())
        .addMethod(readMethod(context, value))
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

  private static MethodSpec readMethod(Context context, FieldSpec value) {
    FieldSpec option = FieldSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it));

    spec.beginControlFlow("if ($N != null)", value)
        .addStatement(throwRepetitionErrorStatement(option))
        .endControlFlow();

    spec.addStatement("$N = readArgument($N, $N)", value, token, it);

    return spec.addAnnotation(Override.class).build();
  }
}
