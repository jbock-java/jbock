package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import java.util.Arrays;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.listOf;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Defines the inner class StatefulParser
 */
final class StatefulParser {

  private final Context context;

  private final ParseMethod parseMethod;

  private final GeneratedTypes generatedTypes;

  private final FieldSpec optionNamesField;

  private final FieldSpec optionParsersField;

  private final FieldSpec paramParsersField;

  @Inject
  StatefulParser(Context context, GeneratedTypes generatedTypes, OptionEnum optionEnum, ParseMethod parseMethod) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.parseMethod = parseMethod;
    ClassName optionType = generatedTypes.optionType();

    // read-only lookups
    this.optionNamesField = FieldSpec.builder(mapOf(STRING, optionType), "optionNames")
        .initializer("$T.$N()", optionType, optionEnum.optionNamesMethod())
        .build();

    // stateful parsers
    this.optionParsersField = FieldSpec.builder(mapOf(optionType, generatedTypes.optionParserType()), "optionParsers")
        .initializer("$T.$N()", optionType, optionEnum.optionParsersMethod())
        .build();

    this.paramParsersField = FieldSpec.builder(listOf(generatedTypes.paramParserType()), "paramParsers")
        .initializer("$T.$N()", optionType, optionEnum.paramParsersMethod())
        .build();
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(generatedTypes.statefulParserType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(buildMethod())
        .addMethod(parseMethod.parseMethod())
        .addMethod(tryParseOptionMethod())
        .addMethod(tryReadOptionMethod())
        .addFields(Arrays.asList(optionNamesField, optionParsersField, paramParsersField))
        .build();
  }

  private MethodSpec tryParseOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = ParameterSpec.builder(generatedTypes.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = tryReadOption($N)", generatedTypes.optionType(), option, token);
    code.beginControlFlow("if ($N != null)", option)
        .addStatement("$N.get($N).read($N, $N)", optionParsersField, option, token, it)
        .addStatement("return true")
        .endControlFlow();
    code.addStatement("return false");
    return MethodSpec.methodBuilder("tryParseOption")
        .addParameter(token)
        .addParameter(it)
        .addCode(code.build())
        .returns(BOOLEAN)
        .build();
  }

  private MethodSpec tryReadOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();

    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.length() <= 1 || $N.charAt(0) != '-')\n", token, token).indent()
        .addStatement("return null").unindent();

    code.add("if ($N.charAt(1) != '-')\n", token).indent()
        .addStatement("return $N.get($N.substring(0, 2))", optionNamesField, token).unindent();

    code.addStatement("$T $N = $N.indexOf('=')", INT, index, token)
        .addStatement("return $N.get($N.substring(0, $N < 0 ? $N.length() : $N))",
            optionNamesField, token, index, token, index);

    return MethodSpec.methodBuilder("tryReadOption")
        .addParameter(token)
        .addCode(code.build())
        .returns(generatedTypes.optionType()).build();
  }

  private MethodSpec buildMethod() {

    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int j = 0; j < context.parameters().size(); j++) {
      Parameter param = context.parameters().get(j);
      args.add(extractExpression(param));
      if (j < context.parameters().size() - 1) {
        args.add(",\n");
      }
    }
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($L)", generatedTypes.implType(), args.build())
        .returns(generatedTypes.sourceType())
        .build();
  }

  private CodeBlock extractExpression(Parameter param) {
    return getStreamExpression(param)
        .add(".stream()")
        .add(".map($L)", param.coercion().mapExpr())
        .add(param.coercion().tailExpr())
        .build();
  }

  private CodeBlock.Builder getStreamExpression(Parameter param) {
    if (param.isPositional()) {
      return CodeBlock.builder().add(
          "$N.get($L)", paramParsersField,
          param.positionalIndex().orElseThrow(AssertionError::new));
    }
    return CodeBlock.builder().add(
        "$N.get($T.$N)", optionParsersField,
        generatedTypes.optionType(), param.enumConstant());
  }
}
