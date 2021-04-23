package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
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

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Defines the inner class StatefulParser
 */
final class StatefulParser {

  private final Context context;

  private final ParseMethod parseMethod;

  private final GeneratedTypes generatedTypes;

  private final FieldSpec optionParsersField;

  private final FieldSpec paramParsersField;

  @Inject
  StatefulParser(Context context, GeneratedTypes generatedTypes, OptionEnum optionEnum, ParseMethod parseMethod) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.parseMethod = parseMethod;
    ClassName optionType = generatedTypes.optionType();

    // stateful parsers
    this.optionParsersField = FieldSpec.builder(mapOf(optionType, generatedTypes.optionParserType()), "optionParsers")
        .initializer("$T.$N()", optionType, optionEnum.optionParsersMethod())
        .build();

    this.paramParsersField = FieldSpec.builder(ArrayTypeName.of(generatedTypes.paramParserType()), "paramParsers")
        .initializer("$T.$N()", optionType, optionEnum.paramParsersMethod())
        .build();
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.statefulParserType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(buildMethod())
        .addMethod(parseMethod.parseMethod());
    if (!context.options().isEmpty()) {
      spec.addMethod(tryParseOptionMethod())
          .addMethod(tryReadOptionMethod());
      spec.addField(optionParsersField);
    }
    if (!context.params().isEmpty()) {
      spec.addField(paramParsersField);
    }
    return spec.build();
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
        .addStatement("return OPTIONS_BY_NAME.get($N.substring(0, 2))", token).unindent();

    code.addStatement("$T $N = $N.indexOf('=')", INT, index, token)
        .addStatement("return OPTIONS_BY_NAME.get($N.substring(0, $N < 0 ? $N.length() : $N))",
            token, index, token, index);

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
          "$N[$L]", paramParsersField,
          param.positionalIndex().orElseThrow(AssertionError::new));
    }
    return CodeBlock.builder().add(
        "$N.get($T.$N)", optionParsersField,
        generatedTypes.optionType(), param.enumConstant());
  }
}
