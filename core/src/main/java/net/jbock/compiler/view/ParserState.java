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

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.listOf;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Defines the inner class ParserState
 */
final class ParserState {

  private final Context context;

  private final ParseMethod parseMethod;

  private final GeneratedTypes generatedTypes;

  private final FieldSpec optionNamesField;

  private final FieldSpec optionParsersField;

  private final FieldSpec paramParsersField;

  private final MethodSpec tryReadOptionMethod;

  @Inject
  ParserState(Context context, GeneratedTypes generatedTypes, OptionEnum optionEnum, ParseMethod parseMethod) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.parseMethod = parseMethod;

    ClassName optionType = generatedTypes.optionType();

    // read-only lookups
    FieldSpec optionNamesField = FieldSpec.builder(mapOf(STRING, optionType), "optionNames")
        .initializer("$T.$N()", optionType, optionEnum.optionNamesMethod())
        .build();

    // stateful parsers
    FieldSpec optionParsersField = FieldSpec.builder(mapOf(optionType, generatedTypes.optionParserType()), "optionParsers")
        .initializer("$T.$N()", optionType, optionEnum.optionParsersMethod())
        .build();

    FieldSpec paramParsersField = FieldSpec.builder(listOf(generatedTypes.paramParserType()), "paramParsers")
        .initializer("$T.$N()", optionType, optionEnum.paramParsersMethod())
        .build();

    MethodSpec tryReadOptionMethod = tryReadOptionMethod(generatedTypes, optionNamesField);
    this.optionNamesField = optionNamesField;
    this.optionParsersField = optionParsersField;
    this.paramParsersField = paramParsersField;
    this.tryReadOptionMethod = tryReadOptionMethod;
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(generatedTypes.parserStateType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(buildMethod())
        .addMethod(parseMethod.parseMethod())
        .addMethod(tryReadOptionMethod)
        .addFields(Arrays.asList(optionNamesField, optionParsersField, paramParsersField))
        .build();
  }

  private static MethodSpec tryReadOptionMethod(GeneratedTypes generatedTypes, FieldSpec optionNamesField) {
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

  /**
   * @return An expression that extracts the value of the given param from the parser state.
   * This expression will evaluate either to a {@link java.util.stream.Stream} or a {@link java.util.Optional}.
   */
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
