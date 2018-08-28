package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.util.*;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.*;
import static net.jbock.compiler.Util.optionalOf;
import static net.jbock.compiler.Util.optionalOfSubtype;

/**
 * Defines the private *_Parser.Helper inner class,
 * which accumulates the non-positional arguments in the input.
 *
 * @see Parser
 */
final class Helper {

  final ClassName type;

  final Context context;
  final Option option;

  final FieldSpec optMapField;
  final FieldSpec sMapField;
  final FieldSpec flagsField;

  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;

  private final Impl impl;

  final MethodSpec readMethod;
  final MethodSpec readRegularOptionMethod;

  private final MethodSpec addFlagMethod;
  private final MethodSpec addArgumentMethod;
  private final MethodSpec readNextMethod;
  private final MethodSpec readLongMethod;

  private final MethodSpec readArgumentMethod;

  final MethodSpec extractRequiredMethod;
  final MethodSpec extractRequiredIntMethod;
  final MethodSpec extractOptionalIntMethod;
  final MethodSpec extractPositionalRequiredMethod;
  final MethodSpec extractPositionalRequiredIntMethod;
  final MethodSpec extractPositionalOptionalMethod;
  final MethodSpec extractPositionalOptionalIntMethod;
  final MethodSpec extractPositionalListMethod;
  final MethodSpec extractPositionalList2Method;

  final ParameterSpec positionalParameter;
  final ParameterSpec ddIndexParameter;

  private Helper(
      ClassName type,
      Context context,
      Impl impl,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField,
      Option option,
      MethodSpec addFlagMethod,
      MethodSpec addArgumentMethod,
      MethodSpec readMethod,
      MethodSpec readNextMethod,
      MethodSpec readLongMethod,
      MethodSpec readRegularOptionMethod,
      MethodSpec readArgumentMethod,
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter,
      MethodSpec extractRequiredMethod,
      MethodSpec extractRequiredIntMethod,
      MethodSpec extractOptionalIntMethod,
      MethodSpec extractPositionalRequiredMethod,
      MethodSpec extractPositionalRequiredIntMethod,
      MethodSpec extractPositionalOptionalIntMethod,
      MethodSpec extractPositionalListMethod,
      MethodSpec extractPositionalOptionalMethod,
      MethodSpec extractPositionalList2Method) {
    this.type = type;
    this.context = context;
    this.impl = impl;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.optMapField = optMapField;
    this.sMapField = sMapField;
    this.flagsField = flagsField;
    this.option = option;
    this.addFlagMethod = addFlagMethod;
    this.addArgumentMethod = addArgumentMethod;
    this.readMethod = readMethod;
    this.readNextMethod = readNextMethod;
    this.readLongMethod = readLongMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
    this.readArgumentMethod = readArgumentMethod;
    this.positionalParameter = positionalParameter;
    this.ddIndexParameter = ddIndexParameter;
    this.extractRequiredMethod = extractRequiredMethod;
    this.extractRequiredIntMethod = extractRequiredIntMethod;
    this.extractOptionalIntMethod = extractOptionalIntMethod;
    this.extractPositionalRequiredMethod = extractPositionalRequiredMethod;
    this.extractPositionalRequiredIntMethod = extractPositionalRequiredIntMethod;
    this.extractPositionalOptionalIntMethod = extractPositionalOptionalIntMethod;
    this.extractPositionalListMethod = extractPositionalListMethod;
    this.extractPositionalOptionalMethod = extractPositionalOptionalMethod;
    this.extractPositionalList2Method = extractPositionalList2Method;
  }

  static Helper create(
      Context context,
      Impl impl,
      Option option) {
    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional")
        .build();
    ParameterSpec ddIndexParameter = ParameterSpec.builder(OptionalInt.class, "ddIndex").build();
    MethodSpec readNextMethod = readNextMethod(option);
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    FieldSpec longNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.type), "longNames")
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.type), "shortNames")
        .build();
    ClassName helperClass = context.generatedClass.nestedClass("Helper");
    FieldSpec optMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, LIST_OF_STRING), "optMap").build();
    FieldSpec sMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, STRING), "sMap").build();
    FieldSpec flagsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        option.type), "flags").build();
    MethodSpec addFlagMethod = addFlagMethod(option, flagsField);
    MethodSpec addArgumentMethod = addArgumentMethod(
        context,
        option,
        optMapField,
        sMapField);
    MethodSpec readLongMethod = readLongMethod(longNamesField, option);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        option,
        readLongMethod);

    MethodSpec readMethod = readMethod(
        readArgumentMethod,
        option,
        addArgumentMethod,
        addFlagMethod);

    MethodSpec extractOptionalIntMethod = extractOptionalIntMethod(option, sMapField);
    MethodSpec extractRequiredMethod = extractRequiredMethod(option, sMapField);
    MethodSpec extractRequiredIntMethod = extractRequiredIntMethod(option, sMapField);
    MethodSpec extractPositionalRequiredMethod = extractPositionalRequiredMethod(
        option, positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalRequiredIntMethod = extractPositionalRequiredIntMethod(
        option, positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalListMethod = extractPositionalListMethod(
        positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalOptionalMethod = extractPositionalOptionalMethod(
        positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalOptionalIntMethod = extractPositionalOptionalIntMethod(
        positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalList2Method = extractPositionalList2Method(
        positionalParameter, ddIndexParameter);

    return new Helper(
        helperClass,
        context,
        impl,
        longNamesField,
        shortNamesField,
        optMapField,
        sMapField,
        flagsField,
        option,
        addFlagMethod,
        addArgumentMethod,
        readMethod,
        readNextMethod,
        readLongMethod,
        readRegularOptionMethod,
        readArgumentMethod,
        positionalParameter,
        ddIndexParameter,
        extractRequiredMethod,
        extractRequiredIntMethod,
        extractOptionalIntMethod,
        extractPositionalRequiredMethod,
        extractPositionalRequiredIntMethod,
        extractPositionalOptionalIntMethod,
        extractPositionalListMethod,
        extractPositionalOptionalMethod,
        extractPositionalList2Method);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addModifiers(PRIVATE, STATIC);
    builder.addMethod(readMethod);
    builder.addMethod(readRegularOptionMethod);
    builder.addMethod(buildMethod());
    if (!context.paramTypes.isEmpty()) {
      builder.addField(
          longNamesField.toBuilder()
              .initializer("$T.unmodifiableMap($T.$N())", Collections.class, option.type, this.option.longNameMapMethod)
              .build());
      builder.addField(
          shortNamesField.toBuilder()
              .initializer("$T.unmodifiableMap($T.$N())", Collections.class, option.type, this.option.shortNameMapMethod)
              .build());
      builder.addField(
          sMapField.toBuilder()
              .initializer("new $T<>($T.class)", EnumMap.class, option.type)
              .build());
      builder.addMethod(addArgumentMethod)
          .addMethod(readArgumentMethod)
          .addMethod(readNextMethod)
          .addMethod(readLongMethod);
    }
    if (context.paramTypes.contains(Type.REPEATABLE)) {
      builder.addField(
          optMapField.toBuilder()
              .initializer("new $T<>($T.class)", EnumMap.class, option.type)
              .build());
    }
    if (context.paramTypes.contains(Type.FLAG)) {
      builder.addField(
          flagsField.toBuilder()
              .initializer("$T.noneOf($T.class)", EnumSet.class, option.type)
              .build());
      builder.addMethod(addFlagMethod);
    }
    if (context.paramTypes.contains(Type.REQUIRED)) {
      builder.addMethod(extractRequiredMethod);
    }
    if (context.paramTypes.contains(Type.REQUIRED_INT)) {
      builder.addMethod(extractRequiredIntMethod);
    }
    if (context.paramTypes.contains(Type.OPTIONAL_INT)) {
      builder.addMethod(extractOptionalIntMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_LIST)) {
      builder.addMethod(extractPositionalListMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_OPTIONAL)) {
      builder.addMethod(extractPositionalOptionalMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_OPTIONAL_INT)) {
      builder.addMethod(extractPositionalOptionalIntMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_REQUIRED)) {
      builder.addMethod(extractPositionalRequiredMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_REQUIRED_INT)) {
      builder.addMethod(extractPositionalRequiredIntMethod);
    }
    if (context.positionalParamTypes.contains(PositionalType.POSITIONAL_LIST_2)) {
      builder.addMethod(extractPositionalList2Method);
    }
    return builder.build();
  }

  private static MethodSpec addFlagMethod(
      Option option,
      FieldSpec flags) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("addFlag");
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    builder.beginControlFlow("if (!$N.add($N))", flags, optionParam)
        .addStatement(throwRepetitionErrorStatement(option, optionParam))
        .endControlFlow();

    return builder.addParameter(optionParam).build();
  }

  private static MethodSpec addArgumentMethod(
      Context context,
      Option option,
      FieldSpec optMap,
      FieldSpec sMap) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec bucket = ParameterSpec.builder(LIST_OF_STRING, "bucket").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("addArgument");

    if (context.paramTypes.contains(Type.REPEATABLE)) {
      builder.beginControlFlow("if ($N.type == $T.$L)", optionParam, option.optionType.type, Type.REPEATABLE);

      builder.addStatement("$T $N = $N.get($N)", bucket.type, bucket, optMap, optionParam);

      builder.beginControlFlow("if ($N == null)", bucket)
          .addStatement("$N = new $T<>()", bucket, ArrayList.class)
          .addStatement("$N.put($N, $N)", optMap, optionParam, bucket)
          .endControlFlow();

      builder.addStatement("$N.add($N)", bucket, argument);
      builder.addStatement("return");

      builder.endControlFlow();
    }

    builder.beginControlFlow("if ($N.containsKey($N))", sMap, optionParam)
        .addStatement(throwRepetitionErrorStatement(option, optionParam))
        .endControlFlow();

    builder.addStatement("$N.put($N, $N)", sMap, optionParam, argument);

    return builder.addParameters(asList(optionParam, argument))
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      FieldSpec shortNamesField,
      Option option,
      MethodSpec readLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(option.type);

    if (option.context.paramTypes.isEmpty()) {
      return builder.addStatement("return null").build();
    }

    builder.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.charAt(1) == '-')", token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    if (!option.context.paramTypes.contains(Type.FLAG)) {
      return builder.addStatement("return $N.get($T.toString($N.charAt(1)))",
          shortNamesField, Character.class, token).build();
    }

    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(1)))",
        option.type, optionParam,
        shortNamesField, Character.class, token);

    builder.beginControlFlow("if ($N == null)", optionParam)
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != $T.$L)",
        optionParam, option.typeField, option.optionType.type, Type.FLAG)
        .addStatement("return $N", optionParam)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() >= 3)", token)
        .addStatement("return null")
        .endControlFlow();

    builder.addStatement("return $N", optionParam);
    return builder.build();
  }

  private static MethodSpec readLongMethod(
      FieldSpec longNamesField,
      Option option) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N < 0)", index)
        .addStatement("return $N.get($N.substring(2))", longNamesField, token)
        .endControlFlow();

    builder.beginControlFlow("else")
        .addStatement("return $N.get($N.substring(2, $N))", longNamesField, token, index)
        .endControlFlow();

    return MethodSpec.methodBuilder("readLong")
        .addParameter(token)
        .returns(option.type)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readMethod(
      MethodSpec readArgumentMethod,
      Option option,
      MethodSpec addArgumentMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
        .addParameters(asList(optionParam, token, it));

    if (option.context.paramTypes.isEmpty()) {
      return builder.addStatement(throwInvalidOptionStatement(option, token))
          .build();
    }

    if (option.context.paramTypes.contains(Type.FLAG)) {
      builder.beginControlFlow("if ($N.$N == $T.$L)",
          optionParam, option.typeField, option.optionType.type, Type.FLAG)
          .addStatement("$N($N)", addFlagMethod, optionParam)
          .addStatement("return")
          .endControlFlow();
    }

    builder.addStatement("$T $N = $N($N, $N)",
        argument.type, argument, readArgumentMethod, token, it);

    builder.addStatement("$N($N, $N)", addArgumentMethod, optionParam, argument);

    return builder.build();
  }

  private static MethodSpec readArgumentMethod(
      MethodSpec readNextMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec isLong = ParameterSpec.builder(BOOLEAN, "isLong").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.charAt(1) == '-'", BOOLEAN, isLong, token);
    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N && $N >= 0)", isLong, index)
        .addStatement("return $N.substring($N + 1)", token, index)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
        .addStatement("return $N.substring(2)", token)
        .endControlFlow();

    builder.addStatement("return $N($N, $N)", readNextMethod, token, it);

    return MethodSpec.methodBuilder("readArgument")
        .addParameters(asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec readNextMethod(Option option) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement(throwMissingValueAfterTokenStatement(option, token))
        .endControlFlow();

    builder.addStatement("return $N.next()", it);

    return MethodSpec.methodBuilder("readNext")
        .addParameters(asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC)
        .build();
  }

  private MethodSpec buildMethod() {

    CodeBlock.Builder args = CodeBlock.builder();
    for (int j = 0; j < option.context.parameters.size(); j++) {
      Param param = option.context.parameters.get(j);
      args.add(param.extractExpression(this));
      if (j < option.context.parameters.size() - 1) {
        args.add(",$W");
      }
    }
    MethodSpec.Builder builder = MethodSpec.methodBuilder("build");

    ParameterSpec last = ParameterSpec.builder(INT, "size").build();
    ParameterSpec max = ParameterSpec.builder(INT, "max").build();

    option.context.maxPositional().ifPresent(maxPositional -> {
      builder.addStatement("$T $N = $L",
          INT, max, maxPositional);
      builder.addStatement("$T $N = $N.orElse($N.size())",
          INT, last, ddIndexParameter, positionalParameter);

      builder.beginControlFlow("if ($N > $N)", last, max)
          .addStatement("throw new $T($S + $N.get($N))", IllegalArgumentException.class,
              "Invalid option: ", positionalParameter, max)
          .endControlFlow();
    });

    if (!context.positionalParameters.isEmpty()) {
      builder.addParameter(positionalParameter);
      builder.addParameter(ddIndexParameter);
    }

    builder.addStatement("return $T.of(new $T($L))", Optional.class, impl.type, args.build());

    return builder.returns(optionalOfSubtype(impl.type)).build();
  }

  private static MethodSpec extractRequiredMethod(
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractRequired");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement(throwMissingRequiredErrorStatement(option, optionParam))
        .endControlFlow();

    builder.addStatement("return $N", token);
    return builder.addParameter(optionParam)
        .returns(STRING).build();
  }

  private static MethodSpec extractRequiredIntMethod(
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractRequiredInt");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement(throwMissingRequiredErrorStatement(option, optionParam))
        .endControlFlow();

    builder.addStatement("return $T.parseInt($N)", Integer.class, token);
    return builder.addParameter(optionParam)
        .returns(INT).build();
  }

  private static MethodSpec extractOptionalIntMethod(
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractOptionalInt");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    builder.addStatement("return $T.of($T.parseInt($N))",
        OptionalInt.class, Integer.class, token);
    return builder.addParameter(optionParam)
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalRequiredMethod(
      Option option,
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalRequired");

    builder.addStatement("$T $N = $N.orElse($N.size())",
        INT, size, ddIndexParameter, positionalParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement(throwMissingParameterStatement(option, optionParam))
        .endControlFlow();

    builder.addStatement("return $N.get($N)", positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter, optionParam))
        .addModifiers(STATIC)
        .returns(STRING).build();
  }

  private static MethodSpec extractPositionalRequiredIntMethod(
      Option option,
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalRequiredInt");

    builder.addStatement("$T $N = $N.orElse($N.size())",
        INT, size, ddIndexParameter, positionalParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement(throwMissingParameterStatement(option, optionParam))
        .endControlFlow();

    builder.addStatement("return $T.parseInt($N.get($N))", Integer.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter, optionParam))
        .addModifiers(STATIC)
        .returns(INT).build();
  }

  private static CodeBlock throwMissingParameterStatement(
      Option option, ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing parameter: ", optionParam)
        .build();
  }

  private static MethodSpec extractPositionalOptionalMethod(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalOptional");

    builder.addStatement("$T $N = $N.orElse($N.size())",
        INT, size, ddIndexParameter, positionalParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow();

    builder.addStatement("return $T.of($N.get($N))",
        Optional.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter))
        .addModifiers(STATIC)
        .returns(optionalOf(STRING)).build();
  }

  private static MethodSpec extractPositionalOptionalIntMethod(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalOptionalInt");

    builder.addStatement("$T $N = $N.orElse($N.size())",
        INT, size, ddIndexParameter, positionalParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    builder.addStatement("return $T.of($T.parseInt($N.get($N)))",
        OptionalInt.class, Integer.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter))
        .addModifiers(STATIC)
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalList2Method(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalList2");

    builder.beginControlFlow("if (!$N.isPresent())", ddIndexParameter)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    builder.addStatement("return $N.subList($N.getAsInt(), $N.size())",
        positionalParameter, ddIndexParameter, positionalParameter);

    return builder.addParameters(Arrays.asList(ddIndexParameter, positionalParameter))
        .addModifiers(STATIC)
        .returns(LIST_OF_STRING).build();
  }

  private static MethodSpec extractPositionalListMethod(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec start = ParameterSpec.builder(INT, "start").build();
    ParameterSpec end = ParameterSpec.builder(INT, "end").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalList");

    builder.beginControlFlow("if ($N >= $N.size())", start, positionalParameter)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    builder.addStatement("$T $N = $N.orElse($N.size())",
        INT, end, ddIndexParameter, positionalParameter);

    builder.beginControlFlow("if ($N >= $N)", start, end)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    builder.addStatement(
        "return $N.subList($N, $N)",
        positionalParameter,
        start,
        end);
    return builder.addParameters(Arrays.asList(start, positionalParameter, ddIndexParameter))
        .addModifiers(STATIC)
        .returns(LIST_OF_STRING).build();
  }

  private static CodeBlock optionSummaryCode(
      Option option,
      ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("$N.name() + $S + $N.$N() + $S",
            optionParam, " (", optionParam, option.describeParamMethod, ")")
        .build();
  }

  private static CodeBlock throwInvalidOptionStatement(
      Option option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private static CodeBlock throwMissingRequiredErrorStatement(
      Option option,
      ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $L)", IllegalArgumentException.class,
            "Missing required option: ", optionSummaryCode(option, optionParam))
        .build();
  }

  private static CodeBlock throwRepetitionErrorStatement(
      Option option,
      ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $L + $S)",
            IllegalArgumentException.class,
            "Option ", optionSummaryCode(option, optionParam), " is not repeatable")
        .build();
  }

  private static CodeBlock throwMissingValueAfterTokenStatement(
      Option option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing value after token: ", token)
        .build();
  }
}
