package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the inner class ParamParser and its subtypes.
 */
final class ParamParser {

  private final Context context;

  private final GeneratedTypes generatedTypes;

  private final OptionParser optionParser;

  @Inject
  ParamParser(Context context, GeneratedTypes generatedTypes, OptionParser optionParser) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
  }

  List<TypeSpec> define() {
    if (context.params().isEmpty()) {
      return Collections.emptyList();
    }
    FieldSpec value = FieldSpec.builder(STRING, "value")
        .build();
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(generatedTypes.paramParserType())
        .addMethod(readMethodAbstract())
        .addMethod(optionParser.streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    boolean anyRegular = !context.regularParams().isEmpty();
    if (anyRegular) {
      result.add(TypeSpec.classBuilder(generatedTypes.regularParamParserType())
          .addField(value)
          .superclass(generatedTypes.paramParserType())
          .addMethod(readMethodRegular(value))
          .addMethod(optionParser.streamMethodRegular(value))
          .addModifiers(PRIVATE, STATIC).build());
    }
    return result;
  }

  private MethodSpec readMethodAbstract() {
    ParameterSpec valueParam = ParameterSpec.builder(STRING, "token").build();
    return MethodSpec.methodBuilder("read")
        .addParameter(valueParam)
        .addModifiers(ABSTRACT)
        .build();
  }

  private MethodSpec readMethodRegular(FieldSpec value) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    CodeBlock.Builder code = CodeBlock.builder()
        .addStatement("$N = $N", value, token);
    return MethodSpec.methodBuilder("read")
        .addParameter(token)
        .addCode(code.build())
        .build();
  }
}
