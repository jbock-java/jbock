package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.parameter.PositionalParameter;

import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the inner class ParamParser and its subtypes.
 */
final class ParamParser {

  static List<TypeSpec> define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .build();
    FieldSpec value = FieldSpec.builder(STRING, "value")
        .build();
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(context.paramParserType())
        .addMethod(readMethodAbstract())
        .addMethod(OptionParser.streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    boolean anyRepeatable = context.params().stream().anyMatch(PositionalParameter::isRepeatable);
    boolean anyRegular = context.params().stream().anyMatch(param -> !param.isRepeatable());
    if (anyRegular) {
      result.add(TypeSpec.classBuilder(context.regularParamParserType())
          .addField(value)
          .superclass(context.paramParserType())
          .addMethod(readMethodRegular(value))
          .addMethod(OptionParser.streamMethodRegular(value))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (anyRepeatable) {
      result.add(TypeSpec.classBuilder(context.repeatableParamParserType())
          .addField(values)
          .superclass(context.paramParserType())
          .addMethod(readMethodRepeatable(values))
          .addMethod(OptionParser.streamMethodRepeatable(values))
          .addModifiers(PRIVATE, STATIC).build());
    }
    return result;
  }

  private static MethodSpec readMethodAbstract() {
    ParameterSpec valueParam = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(valueParam)
        .returns(TypeName.INT)
        .addModifiers(ABSTRACT)
        .build();
  }

  private static MethodSpec readMethodRepeatable(FieldSpec values) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(token)
        .returns(TypeName.INT)
        .addStatement("if ($N == null) $N = new $T<>()", values, values, ArrayList.class)
        .addStatement("$N.add($N)", values, token)
        .addStatement("return $L", 0)
        .build();
  }

  private static MethodSpec readMethodRegular(FieldSpec value) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(token)
        .returns(TypeName.INT)
        .addStatement("$N = $N", value, token)
        .addStatement("return $L", 1)
        .build();
  }
}
