package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOf;
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
  private final MethodSpec readLongMethod;

  final MethodSpec extractRequiredMethod;
  final MethodSpec extractRequiredIntMethod;
  final MethodSpec extractOptionalIntMethod;
  final MethodSpec extractPositionalRequiredMethod;
  final MethodSpec extractPositionalRequiredIntMethod;
  final MethodSpec extractPositionalOptionalMethod;
  final MethodSpec extractPositionalOptionalIntMethod;
  final MethodSpec extractPositionalListMethod;

  final ParameterSpec positionalParameter;

  private Helper(
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
      MethodSpec readLongMethod,
      MethodSpec readRegularOptionMethod,
      ParameterSpec positionalParameter,
      MethodSpec extractRequiredMethod,
      MethodSpec extractRequiredIntMethod,
      MethodSpec extractOptionalIntMethod,
      MethodSpec extractPositionalRequiredMethod,
      MethodSpec extractPositionalRequiredIntMethod,
      MethodSpec extractPositionalOptionalIntMethod,
      MethodSpec extractPositionalListMethod,
      MethodSpec extractPositionalOptionalMethod) {
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
    this.readLongMethod = readLongMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
    this.positionalParameter = positionalParameter;
    this.extractRequiredMethod = extractRequiredMethod;
    this.extractRequiredIntMethod = extractRequiredIntMethod;
    this.extractOptionalIntMethod = extractOptionalIntMethod;
    this.extractPositionalRequiredMethod = extractPositionalRequiredMethod;
    this.extractPositionalRequiredIntMethod = extractPositionalRequiredIntMethod;
    this.extractPositionalOptionalIntMethod = extractPositionalOptionalIntMethod;
    this.extractPositionalListMethod = extractPositionalListMethod;
    this.extractPositionalOptionalMethod = extractPositionalOptionalMethod;
  }

  static Helper create(
      MethodSpec readArgumentMethod,
      MethodSpec readNextMethod,
      Context context,
      Impl impl,
      Option option) {
    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional")
        .build();

    // read-only lookups
    FieldSpec longNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, context.optionType()), "longNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.longNameMapMethod)
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        TypeName.get(Character.class), context.optionType()), "shortNames")
        .initializer("$T.unmodifiableMap($T.$N())", Collections.class, context.optionType(), option.shortNameMapMethod)
        .build();

    // empty collections
    FieldSpec optMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        context.optionType(), LIST_OF_STRING), "optMap")
        .initializer("new $T<>($T.class)", EnumMap.class, context.optionType())
        .build();
    FieldSpec sMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        context.optionType(), STRING), "sMap")
        .initializer("new $T<>($T.class)", EnumMap.class, context.optionType())
        .build();
    FieldSpec flagsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        context.optionType()), "flags")
        .initializer("$T.noneOf($T.class)", EnumSet.class, context.optionType())
        .build();

    MethodSpec addFlagMethod = addFlagMethod(
        option,
        context,
        flagsField);
    MethodSpec addArgumentMethod = addArgumentMethod(
        context,
        option,
        optMapField,
        sMapField);
    MethodSpec readLongMethod = readLongMethod(longNamesField, context);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        context,
        option,
        readLongMethod);

    MethodSpec readMethod = readMethod(
        readArgumentMethod,
        context,
        option,
        addArgumentMethod,
        addFlagMethod);

    MethodSpec extractOptionalIntMethod = extractOptionalIntMethod(context, option, sMapField);
    MethodSpec extractRequiredMethod = extractRequiredMethod(context, option, sMapField);
    MethodSpec extractRequiredIntMethod = extractRequiredIntMethod(context, option, sMapField);
    MethodSpec extractPositionalRequiredMethod = extractPositionalRequiredMethod(
        context, option, positionalParameter);
    MethodSpec extractPositionalRequiredIntMethod = extractPositionalRequiredIntMethod(
        context, option, positionalParameter);
    MethodSpec extractPositionalListMethod = extractPositionalListMethod(
        positionalParameter);
    MethodSpec extractPositionalOptionalMethod = extractPositionalOptionalMethod(
        positionalParameter);
    MethodSpec extractPositionalOptionalIntMethod = extractPositionalOptionalIntMethod(
        positionalParameter);

    return new Helper(
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
        readLongMethod,
        readRegularOptionMethod,
        positionalParameter,
        extractRequiredMethod,
        extractRequiredIntMethod,
        extractOptionalIntMethod,
        extractPositionalRequiredMethod,
        extractPositionalRequiredIntMethod,
        extractPositionalOptionalIntMethod,
        extractPositionalListMethod,
        extractPositionalOptionalMethod);
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
          .addField(sMapField);
      spec.addMethod(addArgumentMethod)
          .addMethod(readLongMethod);
    }
    if (context.nonpositionalParamTypes.contains(OptionType.REPEATABLE)) {
      spec.addField(optMapField);
    }
    if (context.nonpositionalParamTypes.contains(OptionType.FLAG)) {
      spec.addField(flagsField);
      spec.addMethod(addFlagMethod);
    }
    if (context.nonpositionalParamTypes.contains(OptionType.REQUIRED)) {
      spec.addMethod(extractRequiredMethod);
    }
    if (context.nonpositionalParamTypes.contains(OptionType.REQUIRED_INT)) {
      spec.addMethod(extractRequiredIntMethod);
    }
    if (context.nonpositionalParamTypes.contains(OptionType.OPTIONAL_INT)) {
      spec.addMethod(extractOptionalIntMethod);
    }
    if (context.positionalParamTypes.contains(OptionType.REPEATABLE)) {
      spec.addMethod(extractPositionalListMethod);
    }
    if (context.positionalParamTypes.contains(OptionType.OPTIONAL)) {
      spec.addMethod(extractPositionalOptionalMethod);
    }
    if (context.positionalParamTypes.contains(OptionType.OPTIONAL_INT)) {
      spec.addMethod(extractPositionalOptionalIntMethod);
    }
    if (context.positionalParamTypes.contains(OptionType.REQUIRED)) {
      spec.addMethod(extractPositionalRequiredMethod);
    }
    if (context.positionalParamTypes.contains(OptionType.REQUIRED_INT)) {
      spec.addMethod(extractPositionalRequiredIntMethod);
    }
    return spec.build();
  }

  private static MethodSpec addFlagMethod(
      Option option,
      Context context,
      FieldSpec flags) {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("addFlag");
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    spec.beginControlFlow("if (!$N.add($N))", flags, optionParam)
        .addStatement(throwRepetitionErrorStatement(optionParam))
        .endControlFlow();

    return spec.addParameter(optionParam).build();
  }

  private static MethodSpec addArgumentMethod(
      Context context,
      Option option,
      FieldSpec optMap,
      FieldSpec sMap) {
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec bucket = ParameterSpec.builder(LIST_OF_STRING, "bucket").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("addArgument");

    if (context.nonpositionalParamTypes.contains(OptionType.REPEATABLE)) {
      spec.beginControlFlow("if ($N.type == $T.$L)", optionParam, context.optionTypeType(), OptionType.REPEATABLE);

      spec.addStatement("$T $N = $N.get($N)", bucket.type, bucket, optMap, optionParam);

      spec.beginControlFlow("if ($N == null)", bucket)
          .addStatement("$N = new $T<>()", bucket, ArrayList.class)
          .addStatement("$N.put($N, $N)", optMap, optionParam, bucket)
          .endControlFlow();

      spec.addStatement("$N.add($N)", bucket, argument);
      spec.addStatement("return");

      spec.endControlFlow();
    }

    spec.beginControlFlow("if ($N.containsKey($N))", sMap, optionParam)
        .addStatement(throwRepetitionErrorStatement(optionParam))
        .endControlFlow();

    spec.addStatement("$N.put($N, $N)", sMap, optionParam, argument);

    return spec.addParameters(asList(optionParam, argument))
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

    spec.beginControlFlow("if ($N.$N != $T.$L)",
        optionParam, option.typeField, option.context.optionTypeType(), OptionType.FLAG)
        .addStatement("return $N", optionParam)
        .endControlFlow();

    spec.beginControlFlow("if ($N.length() >= 3)", token)
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
      MethodSpec readArgumentMethod,
      Context context,
      Option option,
      MethodSpec addArgumentMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("read")
        .addParameters(asList(optionParam, token, it));

    if (option.context.nonpositionalParamTypes.isEmpty()) {
      return spec.addStatement(throwInvalidOptionStatement(token))
          .build();
    }

    if (option.context.nonpositionalParamTypes.contains(OptionType.FLAG)) {
      spec.beginControlFlow("if ($N.$N == $T.$L)",
          optionParam, option.typeField, option.context.optionTypeType(), OptionType.FLAG)
          .addStatement("$N($N)", addFlagMethod, optionParam)
          .addStatement("return")
          .endControlFlow();
    }

    spec.addStatement("$T $N = $N($N, $N)",
        argument.type, argument, readArgumentMethod, token, it);

    spec.addStatement("$N($N, $N)", addArgumentMethod, optionParam, argument);

    return spec.build();
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
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");

    ParameterSpec last = ParameterSpec.builder(INT, "size").build();
    ParameterSpec max = ParameterSpec.builder(INT, "max").build();

    option.context.maxPositional().ifPresent(maxPositional -> {
      spec.addStatement("$T $N = $L",
          INT, max, maxPositional);
      spec.addStatement("$T $N = $N.size()",
          INT, last, positionalParameter);

      spec.beginControlFlow("if ($N > $N)", last, max)
          .addStatement("throw new $T($S + $N.get($N))", IllegalArgumentException.class,
              "Invalid option: ", positionalParameter, max)
          .endControlFlow();
    });

    if (context.hasPositional()) {
      spec.addParameter(positionalParameter);
    }

    spec.addStatement("return $T.of(new $T($L))", Optional.class, impl.type, args.build());

    return spec.returns(optionalOfSubtype(impl.type)).build();
  }

  private static MethodSpec extractRequiredMethod(
      Context context,
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractRequired");

    spec.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    spec.beginControlFlow("if ($N == null)", token)
        .addStatement(throwMissingRequiredErrorStatement(optionParam))
        .endControlFlow();

    spec.addStatement("return $N", token);
    return spec.addParameter(optionParam)
        .returns(STRING).build();
  }

  private static MethodSpec extractRequiredIntMethod(
      Context context,
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractRequiredInt");

    spec.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    spec.beginControlFlow("if ($N == null)", token)
        .addStatement(throwMissingRequiredErrorStatement(optionParam))
        .endControlFlow();

    spec.addStatement("return $T.parseInt($N)", Integer.class, token);
    return spec.addParameter(optionParam)
        .returns(INT).build();
  }

  private static MethodSpec extractOptionalIntMethod(
      Context context,
      Option option,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractOptionalInt");

    spec.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, optionParam);

    spec.beginControlFlow("if ($N == null)", token)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    spec.addStatement("return $T.of($T.parseInt($N))",
        OptionalInt.class, Integer.class, token);
    return spec.addParameter(optionParam)
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalRequiredMethod(
      Context context,
      Option option,
      ParameterSpec positionalParameter) {
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractPositionalRequired");

    spec.addStatement("$T $N = $N.size()",
        INT, size, positionalParameter);

    spec.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement(throwMissingParameterStatement(optionParam))
        .endControlFlow();

    spec.addStatement("return $N.get($N)", positionalParameter, index);

    return spec.addParameters(Arrays.asList(index, positionalParameter, optionParam))
        .addModifiers(STATIC)
        .returns(STRING).build();
  }

  private static MethodSpec extractPositionalRequiredIntMethod(
      Context context,
      Option option,
      ParameterSpec positionalParameter) {
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractPositionalRequiredInt");

    spec.addStatement("$T $N = $N.size()",
        INT, size, positionalParameter);

    spec.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement(throwMissingParameterStatement(optionParam))
        .endControlFlow();

    spec.addStatement("return $T.parseInt($N.get($N))", Integer.class, positionalParameter, index);

    return spec.addParameters(Arrays.asList(index, positionalParameter, optionParam))
        .addModifiers(STATIC)
        .returns(INT).build();
  }

  private static CodeBlock throwMissingParameterStatement(ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N + $S)", IllegalArgumentException.class,
            "Missing parameter: <", optionParam, ">")
        .build();
  }

  private static MethodSpec extractPositionalOptionalMethod(
      ParameterSpec positionalParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractPositionalOptional");

    spec.addStatement("$T $N = $N.size()",
        INT, size, positionalParameter);

    spec.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow();

    spec.addStatement("return $T.of($N.get($N))",
        Optional.class, positionalParameter, index);

    return spec.addParameters(Arrays.asList(index, positionalParameter))
        .addModifiers(STATIC)
        .returns(optionalOf(STRING)).build();
  }

  private static MethodSpec extractPositionalOptionalIntMethod(
      ParameterSpec positionalParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractPositionalOptionalInt");

    spec.addStatement("$T $N = $N.size()",
        INT, size, positionalParameter);

    spec.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    spec.addStatement("return $T.of($T.parseInt($N.get($N)))",
        OptionalInt.class, Integer.class, positionalParameter, index);

    return spec.addParameters(Arrays.asList(index, positionalParameter))
        .addModifiers(STATIC)
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalListMethod(
      ParameterSpec positionalParameter) {
    ParameterSpec start = ParameterSpec.builder(INT, "start").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("extractPositionalList");

    spec.beginControlFlow("if ($N >= $N.size())", start, positionalParameter)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    spec.addStatement("return $N.subList($N, $N.size())", positionalParameter, start, positionalParameter);
    return spec.addParameters(Arrays.asList(start, positionalParameter))
        .addModifiers(STATIC)
        .returns(LIST_OF_STRING).build();
  }

  private static CodeBlock optionSummaryCode(
      Object optionParam) {
    return CodeBlock.builder()
        .add("$N + $S + $N.describeParam($S) + $S",
            optionParam, " (", optionParam, "", ")")
        .build();
  }

  private static CodeBlock throwInvalidOptionStatement(
      ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private static CodeBlock throwMissingRequiredErrorStatement(
      ParameterSpec optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $L)", IllegalArgumentException.class,
            "Missing required option: ", optionSummaryCode(optionParam))
        .build();
  }

  // TODO param should be FieldSpec
  static CodeBlock throwRepetitionErrorStatement(
      Object optionParam) {
    return CodeBlock.builder()
        .add("throw new $T($S + $L + $S)",
            IllegalArgumentException.class,
            "Option ", optionSummaryCode(optionParam), " is not repeatable")
        .build();
  }

}
