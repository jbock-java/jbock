package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.view.ParserState.throwRepetitionErrorStatement;

/**
 * Generates the FlagParser class.
 */
final class FlagParser {

  static TypeSpec define(Context context) {
    return TypeSpec.classBuilder(context.flagParserType())
        .superclass(context.optionParserType())
        .addMethod(readMethod(context))
        .addMethod(RegularOptionParser.constructor(context))
        .addModifiers(PRIVATE, STATIC).build();
  }

  private static MethodSpec readMethod(Context context) {
    FieldSpec option = FieldSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it));

    spec.beginControlFlow("if ($N.charAt(1) != '-' && $N.length() > 2 || $N.contains($S))", token, token, token, "=")
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Invalid token: ", token)
        .endControlFlow();
    spec.beginControlFlow("if (!values.isEmpty())")
        .addStatement(throwRepetitionErrorStatement(option))
        .endControlFlow();
    return spec.addStatement("values.add($S)", "").build();
  }
}
