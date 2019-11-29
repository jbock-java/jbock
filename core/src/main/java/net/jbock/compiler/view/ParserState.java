package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.ParameterType;
import net.jbock.compiler.Context;
import net.jbock.compiler.Param;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.view.Tokenizer.throwInvalidOptionStatement;

/**
 * Defines the inner class ParserState
 */
final class ParserState {

  private final Context context;

  private final FieldSpec optionNamesField;
  private final FieldSpec parsersField;

  private final FieldSpec positionalParsersField;

  private final MethodSpec readPositionalMethod;
  private final MethodSpec readRegularOptionMethod;

  private ParserState(
      Context context,
      FieldSpec optionNamesField,
      FieldSpec parsersField,
      FieldSpec positionalParsersField,
      MethodSpec readPositionalMethod,
      MethodSpec readRegularOptionMethod) {
    this.context = context;
    this.optionNamesField = optionNamesField;
    this.parsersField = parsersField;
    this.positionalParsersField = positionalParsersField;
    this.readPositionalMethod = readPositionalMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
  }

  static ParserState create(Context context, Option option) {

    // read-only lookups
    FieldSpec optionNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, context.optionType()), "optionNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.optionNamesMethod())
        .addModifiers(FINAL)
        .build();

    // stateful parsers
    FieldSpec parsersField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        context.optionType(), context.optionParserType()), "parsers")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.parsersMethod())
        .addModifiers(FINAL)
        .build();

    FieldSpec positionalParsersField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class),
        context.positionalOptionParserType()), "positionalParsers")
        .initializer("$T.unmodifiableList($T.$N())", Collections.class, context.optionType(), option.positionalParsersMethod())
        .addModifiers(FINAL)
        .build();

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(context, optionNamesField);

    MethodSpec readPositionalMethod = readPositionalMethod(positionalParsersField, context);

    return new ParserState(
        context,
        optionNamesField,
        parsersField,
        positionalParsersField,
        readPositionalMethod,
        readRegularOptionMethod);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(context.parserStateType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(buildMethod())
        .addMethod(readPositionalMethod)
        .addMethod(readRegularOptionMethod)
        .addFields(Arrays.asList(optionNamesField, parsersField, positionalParsersField))
        .build();
  }

  private static MethodSpec readRegularOptionMethod(Context context, FieldSpec optionNamesField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(context.optionType());

    spec.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    spec.beginControlFlow("if ($N.charAt(1) == '-')", token)
        .addStatement("$T $N = $N.indexOf('=')", INT, index, token)
        .addStatement("return $N.get($N.substring(0, $N < 0 ? $N.length() : $N))",
            optionNamesField, token, index, token, index)
        .endControlFlow();

    spec.addStatement("return $N.get($N.substring(0, 2))", optionNamesField, token);

    return spec.build();
  }

  private static MethodSpec readPositionalMethod(FieldSpec positionalParsersField, Context context) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec positionParam = ParameterSpec.builder(INT, "position").build();

    return MethodSpec.methodBuilder("readPositional")
        .addParameters(Arrays.asList(positionParam, token))
        .returns(INT)
        .beginControlFlow("if ($N >= $N.size())", positionParam, positionalParsersField)
        .addStatement(throwInvalidOptionStatement(token))
        .endControlFlow()
        .addStatement("return $N.get($N).read($N)", positionalParsersField, positionParam, token)
        .build();
  }

  private MethodSpec buildMethod() {

    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int j = 0; j < context.parameters().size(); j++) {
      Param param = context.parameters().get(j);
      args.add(extractExpression(param));
      if (j < context.parameters().size() - 1) {
        args.add(",\n");
      }
    }
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($L)", context.implType(), args.build())
        .returns(context.sourceElement())
        .build();
  }

  private CodeBlock extractExpression(Param param) {
    CodeBlock.Builder builder = getStreamExpression(param).toBuilder();
    if (!param.isFlag()) {
      builder.add(".map($L)", param.coercion().mapExpr());
    }
    param.coercion().collectExpr().map(collectExpr ->
        CodeBlock.of(".collect($L)", collectExpr))
        .ifPresent(builder::add);
    if (param.isRequired()) {
      builder.add(".orElseThrow($T.$L.missingRequired())", context.optionType(),
          param.enumConstant());
    }
    return builder.build();
  }

  static CodeBlock throwRepetitionErrorStatement(
      FieldSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($T.format($S, $N, $N.describeParam($S).trim()))",
            IllegalArgumentException.class,
            String.class,
            "Option %s (%s) is not repeatable",
            optionParam, optionParam, "")
        .build();
  }

  /**
   * @return An expression that extracts the value of the given param from the parser state.
   * This expression will evaluate either to a {@link java.util.stream.Stream} or a {@link java.util.Optional}.
   */
  private CodeBlock getStreamExpression(Param param) {
    ParameterType parameterType = param.coercion().parameterType();
    if (param.isPositional()) {
      if (parameterType == ParameterType.REPEATABLE) {
        return repeatablePositionalStream(param);
      } else {
        return regularPositionalStream(param);
      }
    }
    switch (parameterType) {
      case REPEATABLE:
        return repeatableStream(param);
      case FLAG:
        return flagStream(param);
      default:
        return regularStream(param);
    }
  }


  private CodeBlock flagStream(Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$N).flag()",
        parsersField,
        context.optionType(),
        param.enumConstant())
        .build();
  }

  private CodeBlock regularStream(Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$L).value()",
        parsersField,
        context.optionType(),
        param.enumConstant())
        .build();
  }

  private CodeBlock regularPositionalStream(Param param) {
    return CodeBlock.builder().add(
        "$N.get($L).value()",
        positionalParsersField,
        param.positionalIndex().orElseThrow(AssertionError::new))
        .build();
  }

  private CodeBlock repeatableStream(Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$L).values()",
        parsersField,
        context.optionType(),
        param.enumConstant())
        .build();
  }

  private CodeBlock repeatablePositionalStream(Param param) {
    return CodeBlock.builder().add(
        "$N.get($L).values()",
        positionalParsersField,
        param.positionalIndex().orElseThrow(AssertionError::new))
        .build();
  }

  MethodSpec readPositionalMethod() {
    return readPositionalMethod;
  }

  MethodSpec readRegularOptionMethod() {
    return readRegularOptionMethod;
  }

  FieldSpec parsersField() {
    return parsersField;
  }
}
