package net.jbock.compiler;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
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

  private final Context context;
  private final Option option;

  private final FieldSpec optMapField;
  private final FieldSpec sMapField;
  private final FieldSpec flagsField;

  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;

  final MethodSpec readGroupMethod;
  final MethodSpec readMethod;
  final MethodSpec buildMethod;
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

  private Helper(
      ClassName type,
      Context context,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField,
      Option option,
      MethodSpec buildMethod,
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
      MethodSpec chopMethod) {
    this.type = type;
    this.context = context;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.optMapField = optMapField;
    this.sMapField = sMapField;
    this.flagsField = flagsField;
    this.option = option;
    this.buildMethod = buildMethod;
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
  }

  static Helper create(
      Context context,
      Impl impl,
      OptionType optionType,
      Option option) {
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
    MethodSpec buildMethod = buildMethod(option, impl, optMapField, sMapField, flagsField);
    MethodSpec addFlagMethod = addFlagMethod(context, option, optionType.type, flagsField);
    MethodSpec addMethod = addArgumentMethod(
        context,
        option.type,
        optionType.type,
        optMapField,
        sMapField);
    MethodSpec readLongMethod = readLongMethod(
        longNamesField, option.type);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        context,
        shortNamesField,
        option.typeField,
        optionType.type,
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

    return new Helper(
        helperClass,
        context,
        longNamesField,
        shortNamesField,
        optMapField,
        sMapField,
        flagsField,
        option,
        buildMethod,
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
        chopMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addFields(asList(
            longNamesField.toBuilder()
                .initializer("$T.$N()", option.type, this.option.longNameMapMethod)
                .build(),
            shortNamesField.toBuilder()
                .initializer("$T.$N()", option.type, this.option.shortNameMapMethod)
                .build(),
            optMapField.toBuilder()
                .initializer("new $T<>($T.class)", EnumMap.class, option.type)
                .build(),
            sMapField.toBuilder()
                .initializer("new $T<>($T.class)", EnumMap.class, option.type)
                .build(),
            flagsField.toBuilder()
                .initializer("$T.noneOf($T.class)", EnumSet.class, option.type)
                .build()))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(readMethod)
        .addMethod(readRegularOptionMethod)
        .addMethod(addMethod)
        .addMethod(addFlagMethod)
        .addMethod(readArgumentMethod)
        .addMethod(readNextMethod)
        .addMethod(readLongMethod)
        .addMethod(buildMethod)
        .addMethod(looksLikeLongMethod);
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
      Context context,
      Option option,
      ClassName optionTypeClass,
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
        .addStatement(repetitionError(option, token))
        .endControlFlow();

    builder.addStatement("$N.put($N, $N)", sMap, option, argument);

    builder.addStatement("return $L", true);

    return builder.addParameters(asList(option, token, argument))
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      Context context,
      FieldSpec shortNamesField,
      FieldSpec optionTypeField,
      ClassName optionTypeType,
      ClassName optionType,
      MethodSpec readLongMethod,
      MethodSpec looksLikeLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N($N))", looksLikeLongMethod, token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() < 2 || $N.charAt(0) != '-')", token, token)
        .addStatement("return null")
        .endControlFlow();

    builder.add("\n");
    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(1)))", option.type, option,
        shortNamesField, Character.class, token);
    builder.add("\n");

    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return null")
        .endControlFlow();

    if (context.paramTypes.contains(Type.FLAG)) {
      builder.beginControlFlow("if ($N.$N == $T.$L && $N.length() >= 3)", option, optionTypeField,
          optionTypeType, Type.FLAG, token)
          .add("// flags cannot have an argument attached\n")
          .addStatement("return null", option)
          .endControlFlow();
    }

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
    ParameterSpec firstToken = ParameterSpec.builder(STRING, "firstToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(optionClass, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.get($T.toString($N.charAt(0)))",
        optionParam.type, optionParam, shortNamesField, Character.class, token);

    builder.beginControlFlow("if ($N == null)", optionParam)
        .addStatement("throw new $T($S +\n$N + $S + $N.charAt(0) + $S)", IllegalArgumentException.class,
            "Invalid token in option group '", firstToken, "': '", token, "'")
        .endControlFlow();

    builder.addStatement("return $N", optionParam);

    return MethodSpec.methodBuilder("readOptionFromGroup")
        .addParameters(asList(token, firstToken))
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readLongMethod(
      FieldSpec longNamesField,
      ClassName optionClass) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N < 0)", index)
        .addStatement("return $N.get($N.substring(2))", longNamesField, token)
        .endControlFlow();

    builder.addStatement("return $N.get($N.substring(2, $N))", longNamesField, token, index);

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

    ParameterSpec firstToken = ParameterSpec.builder(STRING, "firstToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("readGroup");
    builder.addStatement("$T $N = $N($N)", STRING, token, stripMethod, firstToken);
    builder.beginControlFlow("while(!$N.isEmpty())", token)
        .addStatement("$T $N = $N($N, $N)",
            optionParam.type, optionParam, readOptionFromGroupMethod, token, firstToken);
    builder.beginControlFlow("if ($N.type != $T.$L)", optionParam, option.optionType.type, Type.FLAG)
        .addStatement("throw new $T($S +\n$N + $S + $N.$N + $S)", IllegalArgumentException.class,
            "Invalid token in option group '", firstToken, "': '",
            optionParam, option.shortNameField, "'")
        .endControlFlow();

    builder.addStatement("$N($N)", addFlagMethod, optionParam)
        .addStatement("$N = $N($N)", token, chopMethod, token)
        .endControlFlow();

    return builder.addParameter(firstToken).build();
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

    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N($N)", option.type, option, readRegularOptionMethod, token);

    // unknown token
    builder.beginControlFlow("if ($N == null)", option)
        .add("// unknown token\n")
        .addStatement("return $L", false)
        .endControlFlow();

    if (context.paramTypes.contains(Type.FLAG)) {
      builder.add("\n");
      builder.beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, Type.FLAG)
          .addStatement("$N($N)", addFlagMethod, option)
          .addStatement("return $L", true)
          .endControlFlow();
    }

    builder.add("\n");
    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readArgumentMethod, token, it);

    builder.addStatement("$N($N, $N, $N)", addMethod, option, token, argument);

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("read")
        .addParameters(asList(token, it))
        .addCode(builder.build())
        .returns(BOOLEAN)
        .build();
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

    builder.beginControlFlow("if ($N.length() < 2)", token)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.addStatement("return $N.charAt(0) == '-' && $N.charAt(1) == '-'",
        token, token);

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

  private static MethodSpec buildMethod(
      Option option,
      Impl impl,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField) {
    return MethodSpec.methodBuilder("build")
        .addStatement("return $T.$N($N, $N, $N, $N, $N)", impl.type, impl.createMethod,
            optMapField, sMapField, flagsField, option.positionalParameter, option.ddIndexParameter)
        .addParameter(option.positionalParameter)
        .addParameter(option.ddIndexParameter)
        .returns(impl.type)
        .build();
  }

  private static CodeBlock repetitionError(
      ParameterSpec option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S +\n$N + $S + $N + $S)",
            IllegalArgumentException.class,
            "Found token: ", token, ", but option ", option, " is not repeatable")
        .build();
  }

  private static CodeBlock throwRepetitionErrorInGroup(
      Option option,
      ParameterSpec optionParam,
      ParameterSpec originalToken) {
    return CodeBlock.builder()
        .add("throw new $T($S +\n$N + $S + $N.$N + $S)",
            IllegalArgumentException.class,
            "In option group '",
            originalToken, "': option '",
            optionParam, option.shortNameField, "' is not repeatable")
        .build();
  }
}
