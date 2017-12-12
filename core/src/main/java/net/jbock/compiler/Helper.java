package net.jbock.compiler;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the private *_Parser.Helper inner class.
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

  final MethodSpec readGroupMethod;
  final MethodSpec readMethod;
  final MethodSpec looksLikeGroupMethod;

  private final MethodSpec stripMethod;
  private final MethodSpec addFlagMethod;
  private final MethodSpec addMethod;
  private final MethodSpec readNextMethod;
  private final MethodSpec readLongMethod;
  private final MethodSpec looksLikeLongMethod;
  private final MethodSpec readRegularOptionMethod;
  private final MethodSpec readOptionFromGroupMethod;

  private final MethodSpec readArgumentMethod;
  private final MethodSpec chopMethod;

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
      MethodSpec stripMethod,
      MethodSpec addFlagMethod,
      MethodSpec addMethod,
      MethodSpec readMethod,
      MethodSpec readGroupMethod,
      MethodSpec readNextMethod,
      MethodSpec readLongMethod,
      MethodSpec looksLikeLongMethod,
      MethodSpec looksLikeGroupMethod,
      MethodSpec readRegularOptionMethod,
      MethodSpec readOptionFromGroupMethod,
      MethodSpec readArgumentMethod,
      MethodSpec chopMethod,
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
    this.stripMethod = stripMethod;
    this.addFlagMethod = addFlagMethod;
    this.addMethod = addMethod;
    this.readMethod = readMethod;
    this.readGroupMethod = readGroupMethod;
    this.readNextMethod = readNextMethod;
    this.readLongMethod = readLongMethod;
    this.looksLikeLongMethod = looksLikeLongMethod;
    this.looksLikeGroupMethod = looksLikeGroupMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
    this.readOptionFromGroupMethod = readOptionFromGroupMethod;
    this.readArgumentMethod = readArgumentMethod;
    this.chopMethod = chopMethod;
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
      OptionType optionType,
      Option option) {
    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional")
        .build();
    ParameterSpec ddIndexParameter = ParameterSpec.builder(INT, "ddIndex").build();
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    MethodSpec looksLikeLongMethod = looksLikeLongMethod();
    MethodSpec chopMethod = chopMethod();
    FieldSpec longNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.type), "longNames")
        .addModifiers(FINAL)
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.type), "shortNames")
        .addModifiers(FINAL)
        .build();
    MethodSpec looksLikeGroupMethod = looksLikeGroupMethod(
        looksLikeLongMethod,
        option,
        shortNamesField);
    ClassName helperClass = context.generatedClass.nestedClass("Helper");
    FieldSpec optMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, LIST_OF_STRING), "optMap", FINAL).build();
    FieldSpec sMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, STRING), "sMap", FINAL).build();
    FieldSpec flagsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        option.type), "flags", FINAL).build();
    MethodSpec addFlagMethod = addFlagMethod(option, flagsField);
    MethodSpec addMethod = addArgumentMethod(
        context,
        option.type,
        optionType.type,
        optMapField,
        sMapField);
    MethodSpec readLongMethod = readLongMethod(
        longNamesField, option.type);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        option.type,
        readLongMethod,
        looksLikeLongMethod);

    MethodSpec readOptionFromGroupMethod = readOptionFromGroupMethod(
        shortNamesField,
        option.type);

    MethodSpec readMethod = readMethod(
        context,
        readArgumentMethod,
        readRegularOptionMethod,
        option.type,
        option.typeField,
        optionType.type,
        addMethod,
        addFlagMethod);

    MethodSpec stripMethod = stripMethod();

    MethodSpec readGroupMethod = readGroupMethod(
        readOptionFromGroupMethod,
        stripMethod,
        option,
        chopMethod,
        addFlagMethod);

    MethodSpec extractOptionalIntMethod = extractOptionalIntMethod(option.type, sMapField);
    MethodSpec extractRequiredMethod = extractRequiredMethod(option.type, sMapField);
    MethodSpec extractRequiredIntMethod = extractRequiredIntMethod(option.type, sMapField);
    MethodSpec extractPositionalRequiredMethod = extractPositionalRequiredMethod(
        option.type, positionalParameter, ddIndexParameter);
    MethodSpec extractPositionalRequiredIntMethod = extractPositionalRequiredIntMethod(
        option.type, positionalParameter, ddIndexParameter);
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
        impl, longNamesField,
        shortNamesField,
        optMapField,
        sMapField,
        flagsField,
        option,
        stripMethod,
        addFlagMethod,
        addMethod,
        readMethod,
        readGroupMethod,
        readNextMethod,
        readLongMethod,
        looksLikeLongMethod,
        looksLikeGroupMethod,
        readRegularOptionMethod,
        readOptionFromGroupMethod,
        readArgumentMethod,
        chopMethod,
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
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(readMethod)
        .addMethod(buildMethod());
    if (!context.paramTypes.isEmpty()) {
      builder.addField(
          longNamesField.toBuilder()
              .initializer("$T.$N()", option.type, this.option.longNameMapMethod)
              .build());
      builder.addField(
          shortNamesField.toBuilder()
              .initializer("$T.$N()", option.type, this.option.shortNameMapMethod)
              .build());
      builder.addField(
          optMapField.toBuilder()
              .initializer("new $T<>($T.class)", EnumMap.class, option.type)
              .build());
      builder.addField(
          sMapField.toBuilder()
              .initializer("new $T<>($T.class)", EnumMap.class, option.type)
              .build());
      builder.addField(
          flagsField.toBuilder()
              .initializer("$T.noneOf($T.class)", EnumSet.class, option.type)
              .build());
      builder.addMethod(readRegularOptionMethod)
          .addMethod(addMethod)
          .addMethod(addFlagMethod)
          .addMethod(readArgumentMethod)
          .addMethod(readNextMethod)
          .addMethod(readLongMethod)
          .addMethod(looksLikeLongMethod);
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
    if (context.grouping) {
      builder.addMethod(looksLikeGroupMethod)
          .addMethod(chopMethod)
          .addMethod(stripMethod)
          .addMethod(readGroupMethod)
          .addMethod(readOptionFromGroupMethod);
    }
    return builder.build();
  }

  private static MethodSpec addFlagMethod(
      Option option,
      FieldSpec flags) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("addFlag");
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    builder.beginControlFlow("if (!$N.add($N))", flags, optionParam)
        .addStatement("throw new $T($S + $N.$N + $S)", IllegalArgumentException.class,
            "Option '-", optionParam, option.shortNameField, "' is not repeatable")
        .endControlFlow();

    return builder.addParameter(optionParam).build();
  }

  private static MethodSpec addArgumentMethod(
      Context context,
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec optMap,
      FieldSpec sMap) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec bucket = ParameterSpec.builder(LIST_OF_STRING, "bucket").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("addArgument");

    if (context.paramTypes.contains(Type.REPEATABLE)) {
      builder.beginControlFlow("if ($N.type == $T.$L)", option, optionTypeClass, Type.REPEATABLE);

      builder.addStatement("$T $N = $N.get($N)", bucket.type, bucket, optMap, option);

      builder.beginControlFlow("if ($N == null)", bucket)
          .addStatement("$N = new $T<>()", bucket, ArrayList.class)
          .addStatement("$N.put($N, $N)", optMap, option, bucket)
          .endControlFlow();

      builder.addStatement("$N.add($N)", bucket, argument);
      builder.addStatement("return $L", true);

      builder.endControlFlow();
    }

    builder.beginControlFlow("if ($N.containsKey($N))", sMap, option)
        .addStatement(repetitionError(option))
        .endControlFlow();

    builder.addStatement("$N.put($N, $N)", sMap, option, argument);

    builder.addStatement("return $L", true);

    return builder.addParameters(asList(option, token, argument))
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      FieldSpec shortNamesField,
      ClassName optionType,
      MethodSpec readLongMethod,
      MethodSpec looksLikeLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N($N))", looksLikeLongMethod, token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(1)))", option.type, option,
        shortNamesField, Character.class, token);
    builder.add("\n");

    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .endControlFlow();

    builder.addStatement("return $N", option);

    return MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(optionType)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readOptionFromGroupMethod(
      FieldSpec shortNamesField,
      ClassName optionClass) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(optionClass, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(0)))",
        optionParam.type, optionParam, shortNamesField, Character.class, token);

    builder.beginControlFlow("if ($N == null)", optionParam)
        .addStatement("throw new $T($S + $N.charAt(0))", IllegalArgumentException.class,
            "Invalid option: ", token)
        .endControlFlow();

    builder.addStatement("return $N", optionParam);

    return MethodSpec.methodBuilder("readOptionFromGroup")
        .addParameter(token)
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readLongMethod(
      FieldSpec longNamesField,
      ClassName optionClass) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec option = ParameterSpec.builder(INT, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);
    builder.addStatement("$T $N", optionClass, option);

    builder.beginControlFlow("if ($N < 0)", index)
        .addStatement("$N = $N.get($N.substring(2))", option, longNamesField, token)
        .endControlFlow();

    builder.beginControlFlow("else")
        .addStatement("$N = $N.get($N.substring(2, $N))", option, longNamesField, token, index)
        .endControlFlow();

    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .endControlFlow();

    builder.addStatement("return $N", option);

    return MethodSpec.methodBuilder("readLong")
        .addParameter(token)
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }

  /**
   * <p>The read method takes one token. If the token is
   * <em>known</em>, its argument is  also consumed (unless it's a flag),
   * and this information is added to one of
   * {@code sMap}, {@code optMap}, {@code flags}, and {@code true} is returned.
   * Otherwise, none of these collections are modified, and {@code false}
   * is returned.
   * </p>
   * <p>If the token is an <em>option group</em>, then all flags of this
   * group are read. If the group ends with a binding token, then its argument is
   * also consumed.
   * </p>
   * <p>After this method returns, the next token in the iterator will be
   * a free token, if any. </p>
   */
  private static MethodSpec readGroupMethod(
      MethodSpec readOptionFromGroupMethod,
      MethodSpec stripMethod,
      Option option,
      MethodSpec chopMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("readGroup");
    builder.addStatement("$N = $N($N)", token, stripMethod, token);
    builder.beginControlFlow("while(!$N.isEmpty())", token)
        .addStatement("$T $N = $N($N)",
            optionParam.type, optionParam, readOptionFromGroupMethod, token);
    builder.beginControlFlow("if ($N.type != $T.$L)", optionParam, option.optionType.type, Type.FLAG)
        .addStatement("throw new $T($S + $N.charAt(0))", IllegalArgumentException.class,
            "Invalid option: ", token)
        .endControlFlow();

    builder.addStatement("$N($N)", addFlagMethod, optionParam)
        .addStatement("$N = $N($N)", token, chopMethod, token)
        .endControlFlow();

    return builder.addParameter(token).build();
  }

  private static MethodSpec readMethod(
      Context context,
      MethodSpec readArgumentMethod,
      MethodSpec readRegularOptionMethod,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec addMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("read").addParameters(asList(token, it));

    if (context.paramTypes.isEmpty()) {
      return builder.addStatement("throw new $T($S + $N)",
          IllegalArgumentException.class, "Invalid option: ", token)
          .build();
    }

    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();

    builder.addStatement("$T $N = $N($N)",
        option.type, option, readRegularOptionMethod, token);

    if (context.paramTypes.contains(Type.FLAG)) {
      builder.addCode("\n");
      builder.beginControlFlow("if ($N.$N == $T.$L)",
          option, optionType, optionTypeClass, Type.FLAG)
          .addStatement("$N($N)", addFlagMethod, option)
          .addStatement("return")
          .endControlFlow();
      builder.addCode("\n");
    }

    builder.addStatement("$T $N = $N($N, $N)",
        argument.type, argument, readArgumentMethod, token, it);

    builder.addStatement("$N($N, $N, $N)", addMethod, option, token, argument);

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
        .add("// attached long\n")
        .addStatement("return $N.substring($N + 1)", token, index)
        .endControlFlow();

    builder.beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
        .add("// attached short\n")
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

  private static MethodSpec readNextMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N.hasNext())", it)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing value after token: ", token)
        .endControlFlow();

    builder.addStatement("return $N.next()", it);

    return MethodSpec.methodBuilder("readNext")
        .addParameters(asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec chopMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("chop");
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    builder.beginControlFlow("if ($N.isEmpty())", token)
        .addStatement("return $S", "")
        .endControlFlow();

    builder.addStatement("return $N.substring(1)", token);

    return builder.addParameter(token)
        .addModifiers(STATIC)
        .returns(STRING)
        .build();
  }

  private static MethodSpec stripMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("strip");
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    builder.beginControlFlow("if ($N.isEmpty())", token)
        .addStatement("return $S", "")
        .endControlFlow();

    builder.beginControlFlow("if ($N.charAt(0) == '-')", token)
        .addStatement("return $N.substring(1)", token)
        .endControlFlow();

    builder.addStatement("return $N", token);

    return builder.addParameter(token)
        .addModifiers(STATIC)
        .returns(STRING)
        .build();
  }

  private static MethodSpec looksLikeLongMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("looksLikeLong");

    builder.addStatement("return $N.charAt(1) == '-'", token);

    return builder.addParameter(token)
        .addModifiers(STATIC)
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec looksLikeGroupMethod(
      MethodSpec looksLikeLongMethod,
      Option option,
      FieldSpec shortNamesField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec originalToken = ParameterSpec.builder(STRING, "originalToken").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N($N))", looksLikeLongMethod, originalToken)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.addStatement("$T $N = $N", token.type, token, originalToken);

    builder.beginControlFlow("if ($N.length() < 3 )", token)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.beginControlFlow("if ($N.charAt(0) != '-')", token)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.add("\n");
    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(1)))",
        optionParam.type, optionParam, shortNamesField, Character.class, token);
    builder.add("\n");

    builder.beginControlFlow("if ($N == null)", optionParam)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != $T.$L)",
        optionParam, option.typeField, option.optionType.type, Type.FLAG)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("looksLikeGroup")
        .addParameter(originalToken)
        .returns(BOOLEAN)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec buildMethod() {

    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int j = 0; j < option.context.parameters.size(); j++) {
      Param param = option.context.parameters.get(j);
      args.add(param.extractExpression(this, j));
      if (j < option.context.parameters.size() - 1) {
        args.add(",\n");
      }
    }
    MethodSpec.Builder builder = MethodSpec.methodBuilder("build");

    ParameterSpec last = ParameterSpec.builder(INT, "size").build();
    ParameterSpec max = ParameterSpec.builder(INT, "max").build();

    if (!option.context.positionalParameters.isEmpty()) {
      int maxPositional = option.context.maxPositional();
      if (maxPositional >= 0) {
        builder.addStatement("$T $N = $L",
            INT, max, maxPositional);
        builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
            INT, last, ddIndexParameter, positionalParameter, ddIndexParameter);

        builder.beginControlFlow("if ($N > $N)", last, max)
            .addStatement("throw new $T($S + $N.get($N))", IllegalArgumentException.class,
                "Excess option: ", positionalParameter, max)
            .endControlFlow();
      }
      builder.addParameter(positionalParameter);
      builder.addParameter(ddIndexParameter);
    }

    builder.addStatement("return new $T($L)", impl.type, args.build());
    return builder.returns(impl.type).build();
  }

  private static MethodSpec extractRequiredMethod(
      ClassName type,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractRequired");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, option);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing required option: ", option)
        .endControlFlow();

    builder.addStatement("return $N", token);
    return builder.addParameter(option)
        .returns(STRING).build();
  }

  private static MethodSpec extractRequiredIntMethod(
      ClassName type,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractRequiredInt");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, option);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing required option: ", option)
        .endControlFlow();

    builder.addStatement("return $T.parseInt($N)", Integer.class, token);
    return builder.addParameter(option)
        .returns(INT).build();
  }

  private static MethodSpec extractOptionalIntMethod(
      ClassName type,
      FieldSpec sMapField) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractOptionalInt");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapField, option);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    builder.addStatement("return $T.of($T.parseInt($N))",
        OptionalInt.class, Integer.class, token);
    return builder.addParameter(option)
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalRequiredMethod(
      ClassName type,
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec option = ParameterSpec.builder(type, "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalRequired");

    builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
        INT, size, ddIndexParameter, positionalParameter, ddIndexParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing positional parameter: ", option)
        .endControlFlow();

    builder.addStatement("return $N.get($N)", positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter, option))
        .returns(STRING).build();
  }

  private static MethodSpec extractPositionalRequiredIntMethod(
      ClassName type,
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec option = ParameterSpec.builder(type, "option").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalRequiredInt");

    builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
        INT, size, ddIndexParameter, positionalParameter, ddIndexParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing positional parameter: ", option)
        .endControlFlow();

    builder.addStatement("return $T.parseInt($N.get($N))", Integer.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter, option))
        .returns(INT).build();
  }

  private static MethodSpec extractPositionalOptionalMethod(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalOptional");

    builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
        INT, size, ddIndexParameter, positionalParameter, ddIndexParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow();

    builder.addStatement("return $T.of($N.get($N))",
        Optional.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter))
        .returns(OPTIONAL_STRING).build();
  }

  private static MethodSpec extractPositionalOptionalIntMethod(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec size = ParameterSpec.builder(INT, "size").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalOptionalInt");

    builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
        INT, size, ddIndexParameter, positionalParameter, ddIndexParameter);

    builder.beginControlFlow("if ($N >= $N)", index, size)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    builder.addStatement("return $T.of($T.parseInt($N.get($N)))",
        OptionalInt.class, Integer.class, positionalParameter, index);

    return builder.addParameters(Arrays.asList(index, positionalParameter, ddIndexParameter))
        .returns(OptionalInt.class).build();
  }

  private static MethodSpec extractPositionalList2Method(
      ParameterSpec positionalParameter,
      ParameterSpec ddIndexParameter) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractPositionalList2");

    builder.beginControlFlow("if ($N < 0)", ddIndexParameter)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    builder.addStatement("return $N.subList($N, $N.size())",
        positionalParameter, ddIndexParameter, positionalParameter);

    return builder.addParameters(Arrays.asList(ddIndexParameter, positionalParameter))
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

    builder.addStatement("$T $N = $N < 0 ? $N.size() : $N",
        INT, end, ddIndexParameter, positionalParameter, ddIndexParameter);

    builder.beginControlFlow("if ($N >= $N)", start, end)
        .addStatement("return $T.emptyList()", Collections.class)
        .endControlFlow();

    builder.addStatement(
        "return $N.subList($N, $N)",
        positionalParameter,
        start,
        end);
    return builder.addParameters(Arrays.asList(start, positionalParameter, ddIndexParameter))
        .returns(LIST_OF_STRING).build();
  }


  private static CodeBlock repetitionError(
      ParameterSpec option) {
    return CodeBlock.builder()
        .add("throw new $T($S +$N + $S)",
            IllegalArgumentException.class,
            "Option ", option, " is not repeatable")
        .build();
  }
}
