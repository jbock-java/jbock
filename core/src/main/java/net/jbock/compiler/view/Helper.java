package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.ParameterType;
import net.jbock.compiler.Context;
import net.jbock.compiler.Param;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOfSubtype;

/**
 * Defines the private *_Parser.Helper inner class,
 * which accumulates the non-positional arguments in the input.
 *
 * @see Parser
 */
final class Helper {

  private final Context context;

  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;
  private final FieldSpec parsersField;

  private final FieldSpec positionalParsersField;

  private final MethodSpec readMethod;
  private final MethodSpec readRegularOptionMethod;

  private final MethodSpec readLongMethod;

  private Helper(
      Context context,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec parsersField,
      FieldSpec positionalParsersField,
      MethodSpec readMethod,
      MethodSpec readLongMethod,
      MethodSpec readRegularOptionMethod) {
    this.context = context;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.parsersField = parsersField;
    this.positionalParsersField = positionalParsersField;
    this.readMethod = readMethod;
    this.readLongMethod = readLongMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
  }

  public static Helper create(Context context, Option option) {

    // read-only lookups
    FieldSpec longNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, context.optionType()), "longNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.longNameMapMethod())
        .addModifiers(FINAL)
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        TypeName.get(Character.class), context.optionType()), "shortNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.shortNameMapMethod())
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

    MethodSpec readLongMethod = readLongMethod(longNamesField, context);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        context,
        readLongMethod);

    MethodSpec readMethod = readMethod(parsersField, context);

    return new Helper(
        context,
        longNamesField,
        shortNamesField,
        parsersField,
        positionalParsersField,
        readMethod,
        readLongMethod,
        readRegularOptionMethod);
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.helperType())
        .addModifiers(PRIVATE, STATIC);
    spec.addMethod(readMethod)
        .addMethod(readRegularOptionMethod)
        .addMethod(buildMethod());
    spec.addField(longNamesField)
        .addField(shortNamesField)
        .addField(parsersField);
    spec.addMethod(readLongMethod);
    spec.addField(positionalParsersField);
    return spec.build();
  }

  private static MethodSpec readRegularOptionMethod(
      FieldSpec shortNamesField,
      Context context,
      MethodSpec readLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(context.optionType());

    spec.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    spec.beginControlFlow("if ($N.charAt(1) == '-')", token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    spec.addStatement("$T $N = $N.get($N.charAt(1))",
        context.optionType(), optionParam,
        shortNamesField, token);

    spec.beginControlFlow("if ($N == null)", optionParam)
        .addStatement("return null")
        .endControlFlow();

    spec.beginControlFlow("if (!$N.validShortToken($N))",
        optionParam, token)
        .addStatement("return null")
        .endControlFlow();

    spec.addStatement("return $N", optionParam);
    return spec.build();
  }

  private static MethodSpec readLongMethod(
      FieldSpec longNamesField,
      Context context) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder spec = CodeBlock.builder();

    spec.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    spec.beginControlFlow("if ($N < 0)", index)
        .addStatement("return $N.get($N.substring(2))", longNamesField, token)
        .endControlFlow();

    spec.beginControlFlow("else")
        .addStatement("return $N.get($N.substring(2, $N))", longNamesField, token, index)
        .endControlFlow();

    return MethodSpec.methodBuilder("readLong")
        .addParameter(token)
        .returns(context.optionType())
        .addCode(spec.build())
        .build();
  }

  private static MethodSpec readMethod(
      FieldSpec parsersField,
      Context context) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(optionParam, token, it));

    spec.addStatement("$N.get($N).read($N, $N)", parsersField, optionParam, token, it);

    return spec.build();
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
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");

    spec.addStatement("return $T.of(new $T($L))", Optional.class, context.implType(), args.build());

    return spec.returns(optionalOfSubtype(context.implType())).build();
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
        .add("throw new $T($T.format($S, $N, $N.describeParam($S)))",
            IllegalArgumentException.class,
            String.class,
            "Option %s (%s) is not repeatable",
            optionParam, optionParam, "")
        .build();
  }

  /**
   * @return An expression that extracts the value of the given param from the helper state.
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

  public FieldSpec positionalParsersField() {
    return positionalParsersField;
  }

  public MethodSpec readMethod() {
    return readMethod;
  }

  public MethodSpec readRegularOptionMethod() {
    return readRegularOptionMethod;
  }
}
