package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.view.ParserState.throwRepetitionErrorStatement;

/**
 * Generates the inner class OptionParser and its subtypes.
 */
final class OptionParser {

  static List<TypeSpec> define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .initializer("new $T<>()", ArrayList.class)
        .build();
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethodRepeatable(context))
        .addField(values)
        .addModifiers(PRIVATE, STATIC)
        .build());
    result.add(TypeSpec.classBuilder(context.flagParserType())
        .superclass(context.optionParserType())
        .addMethod(readMethodFlag(context))
        .addModifiers(PRIVATE, STATIC).build());
    result.add(TypeSpec.classBuilder(context.regularOptionParserType())
        .superclass(context.optionParserType())
        .addMethod(readMethodRegular(context))
        .addModifiers(PRIVATE, STATIC).build());
    return result;
  }

  private static MethodSpec readMethodRepeatable(Context context) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("read")
        .addParameters(asList(option, token, it))
        .addStatement("values.add(readOptionArgument($N, $N))", token, it)
        .build();
  }

  private static MethodSpec readMethodRegular(Context context) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if (!values.isEmpty())\n").indent()
        .addStatement(throwRepetitionErrorStatement(option))
        .unindent();

    code.addStatement("super.read($N, $N, $N)", option, token, it);

    return MethodSpec.methodBuilder("read")
        .addCode(code.build())
        .addParameters(asList(option, token, it)).build();
  }


  private static MethodSpec readMethodFlag(Context context) {
    ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.add("if ($N.charAt(1) != '-' && $N.length() > 2 || $N.contains($S))\n", token, token, token, "=").indent()
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Invalid token: ", token)
        .unindent();
    code.add("if (!values.isEmpty())\n").indent()
        .addStatement(throwRepetitionErrorStatement(option))
        .unindent();
    code.addStatement("values.add($S)", "");
    return MethodSpec.methodBuilder("read")
        .addCode(code.build())
        .addParameters(asList(option, token, it)).build();
  }

}
