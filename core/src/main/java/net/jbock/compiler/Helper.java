package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.squareup.javapoet.TypeName.CHAR;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.CHARACTER;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.OptionType.REPEATABLE;
import static net.jbock.compiler.Util.optionalOfSubtype;

/**
 * Defines the private *_Parser.Helper inner class,
 * which accumulates the non-positional arguments in the input.
 *
 * @see Parser
 */
final class Helper {

  final Context context;
  final Option option;

  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;
  final FieldSpec parsersField;

  final MethodSpec readMethod;
  final MethodSpec readRegularOptionMethod;

  private final MethodSpec readLongMethod;

  final ParameterSpec positionalParameter;

  private Helper(
      Context context,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec parsersField, Option option,
      MethodSpec readMethod,
      MethodSpec readLongMethod,
      MethodSpec readRegularOptionMethod,
      ParameterSpec positionalParameter) {
    this.context = context;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.parsersField = parsersField;
    this.option = option;
    this.readMethod = readMethod;
    this.readLongMethod = readLongMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
    this.positionalParameter = positionalParameter;
  }

  static Helper create(
      Context context,
      Option option) {
    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional")
        .build();

    // read-only lookups
    FieldSpec longNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, context.optionType()), "longNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.longNameMapMethod)
        .addModifiers(FINAL)
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        TypeName.get(Character.class), context.optionType()), "shortNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.shortNameMapMethod)
        .addModifiers(FINAL)
        .build();

    // stateful parsers
    FieldSpec parsersField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        context.optionType(), context.optionParserType()), "parsers")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.parsersMethod)
        .addModifiers(FINAL)
        .build();

    MethodSpec readLongMethod = readLongMethod(longNamesField, context);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        context,
        option,
        readLongMethod);

    MethodSpec readMethod = readMethod(
        parsersField,
        context);

    return new Helper(
        context,
        longNamesField,
        shortNamesField,
        parsersField,
        option,
        readMethod,
        readLongMethod,
        readRegularOptionMethod,
        positionalParameter);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.helperType())
        .addModifiers(PRIVATE, STATIC);
    spec.addMethod(readMethod)
        .addMethod(readRegularOptionMethod)
        .addMethod(buildMethod());
    if (!context.nonpositionalParamTypes.isEmpty()) {
      spec.addField(longNamesField)
          .addField(shortNamesField)
          .addField(parsersField);
      spec.addMethod(readLongMethod);
    }
    if (context.containsType(CHAR) || context.containsType(CHARACTER)) {
      spec.addMethod(parseCharacterMethod());
    }
    return spec.build();
  }

  private static MethodSpec parseCharacterMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parseCharacter")
        .addParameter(token);
    spec.beginControlFlow("if ($N.length() != 1)", token)
        .addStatement("throw new $T($S + $N + $S)", IllegalArgumentException.class,
            "Not a character: <", token, ">")
        .endControlFlow();
    spec.addStatement("return $N.charAt(0)", token);
    return spec.addModifiers(STATIC)
        .returns(ClassName.get(Character.class))
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      FieldSpec shortNamesField,
      Context context,
      Option option,
      MethodSpec readLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(context.optionType());

    if (option.context.nonpositionalParamTypes.isEmpty()) {
      return spec.addStatement("return null").build();
    }

    spec.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    spec.beginControlFlow("if ($N.charAt(1) == '-')", token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    if (!option.context.nonpositionalParamTypes.contains(OptionType.FLAG)) {
      return spec.addStatement("return $N.get($N.charAt(1))",
          shortNamesField, token).build();
    }

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

    if (!context.nonpositionalParamTypes.isEmpty()) {
      spec.addStatement("$N.get($N).read($N, $N)", parsersField, optionParam, token, it);
    }

    return spec.build();
  }

  private MethodSpec buildMethod() {

    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      args.add(extractExpression(param));
      if (j < context.parameters.size() - 1) {
        args.add(",\n");
      }
    }
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");

    ParameterSpec last = ParameterSpec.builder(INT, "size").build();
    ParameterSpec max = ParameterSpec.builder(INT, "max").build();

    if (context.hasPositional()) {
      context.maxPositional().ifPresent(maxPositional -> {
        spec.addStatement("$T $N = $L",
            INT, max, maxPositional);
        spec.addStatement("$T $N = $N.size()",
            INT, last, positionalParameter);

        spec.beginControlFlow("if ($N > $N)", last, max)
            .addStatement("throw new $T($S + $N.get($N))", IllegalArgumentException.class,
                "Invalid option: ", positionalParameter, max)
            .endControlFlow();
      });
    }

    for (Param param : context.parameters) {
      if (param.coercion().skipMapCollect()) {
        continue;
      }
      CodeBlock initMapper = param.coercion().initMapper();
      if (!initMapper.isEmpty()) {
        spec.addStatement(initMapper);
      }
      if (param.coercion().collectorParam().isPresent() &&
          param.coercion().initCollector().isPresent()) {
        ParameterSpec collectorParam = param.coercion().collectorParam().get();
        spec.addStatement("$T $N = $L", collectorParam.type, collectorParam, param.coercion().initCollector().get());
      }
    }

    if (context.hasPositional()) {
      spec.addParameter(positionalParameter);
    }

    spec.addStatement("return $T.of(new $T($L))", Optional.class, context.implType(), args.build());

    return spec.returns(optionalOfSubtype(context.implType())).build();
  }

  private CodeBlock extractExpression(Param param) {
    CodeBlock.Builder builder = param.paramType.extractExpression(this, param).toBuilder();
    boolean collected = param.paramType == REPEATABLE && !param.coercion().skipMapCollect();
    if (collected) {
      builder.add(".stream()");
    }
    if (!param.flag) {
      param.coercion().mapExpr().ifPresent(expr ->
          builder.add(".map($L)", expr));
    }
    if (collected) {
      param.coercion().collectExpr().ifPresent(expr ->
          builder.add(".collect($L)", expr));
    }
    if (param.required()) {
      builder.add("\n.orElseThrow(() -> new $T($L))", IllegalArgumentException.class,
          missingRequiredOptionMessage(param, context.optionType()));
    }
    return builder.build();
  }

  private CodeBlock missingRequiredOptionMessage(Param param, ClassName className) {
    if (param.isPositional()) {
      return CodeBlock.builder()
          .add("$T.format($S,$W$T.$L)",
              String.class,
              "Missing parameter: <%s>",
              className, param.enumConstant())
          .build();
    }
    return CodeBlock.builder()
        .add("$T.format($S,$W$T.$L,$W$T.$L.describeParam($S))",
            String.class,
            "Missing required option: %s (%s)",
            className, param.enumConstant(),
            className, param.enumConstant(),
            "")
        .build();
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

}
