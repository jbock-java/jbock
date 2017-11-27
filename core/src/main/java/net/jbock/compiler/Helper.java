package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

import java.util.ArrayList;
import java.util.Arrays;
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

  private final FieldSpec otherTokensField = FieldSpec.builder(LIST_OF_STRING, "otherTokens", FINAL)
      .build();

  private final Context context;
  private final Option option;
  private final OptionType optionType;

  private final FieldSpec optMapField;
  private final FieldSpec sMapField;
  private final FieldSpec flagsField;

  private final ClassName implType;

  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;

  final MethodSpec readGroupMethod;
  final MethodSpec readMethod;
  final MethodSpec buildMethod;

  private final MethodSpec addFlagMethod;
  private final MethodSpec addMethod;
  private final MethodSpec readNextMethod;
  private final MethodSpec readLongMethod;
  private final MethodSpec looksLikeLongMethod;
  private final MethodSpec looksLikeGroupMethod;
  private final MethodSpec readRegularOptionMethod;
  private final MethodSpec readOptionFromGroupMethod;

  private final MethodSpec readArgumentMethod;
  private final MethodSpec chopOffHeadMethod;

  private Helper(
      ClassName type,
      ClassName implType, Context context,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField,
      Option option,
      OptionType optionType,
      MethodSpec buildMethod,
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
      MethodSpec chopOffHeadMethod) {
    this.type = type;
    this.implType = implType;
    this.context = context;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.optMapField = optMapField;
    this.sMapField = sMapField;
    this.flagsField = flagsField;
    this.option = option;
    this.optionType = optionType;
    this.buildMethod = buildMethod;
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
    this.chopOffHeadMethod = chopOffHeadMethod;
  }

  static Helper create(
      Context context,
      ClassName implType,
      OptionType optionType,
      Option option) {
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    MethodSpec looksLikeLongMethod = looksLikeLongMethod();
    MethodSpec chopOffHeadMethod = chopOffHeadMethod();
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
        option.typeField,
        shortNamesField,
        option.type,
        optionType.type);
    ClassName helperClass = context.generatedClass.nestedClass("Helper");
    FieldSpec optMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, LIST_OF_STRING), "optMap", FINAL).build();
    FieldSpec sMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, STRING), "sMap", FINAL).build();
    FieldSpec flagsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        option.type), "flags", FINAL).build();
    MethodSpec buildMethod = buildMethod(implType, optMapField, sMapField, flagsField);
    MethodSpec addFlagMethod = addFlagMethod(option.type, optionType.type, flagsField);
    MethodSpec addMethod = addMethod(option.type, optionType.type,
        optMapField, sMapField, option.isBindingMethod);
    MethodSpec readLongMethod = readLongMethod(
        longNamesField, option.type);

    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField,
        option.typeField,
        optionType.type,
        option.type,
        readLongMethod,
        looksLikeLongMethod);

    MethodSpec readOptionFromGroupMethod = readOptionFromGroupMethod(
        shortNamesField,
        option.type,
        readLongMethod);

    MethodSpec readMethod = readMethod(
        readArgumentMethod,
        readRegularOptionMethod,
        option.type,
        option.typeField,
        optionType.type,
        addMethod,
        addFlagMethod);

    MethodSpec readGroupMethod = readGroupMethod(
        readMethod,
        looksLikeGroupMethod,
        readNextMethod,
        readOptionFromGroupMethod,
        option.type,
        option.typeField,
        optionType.type,
        chopOffHeadMethod,
        addMethod,
        addFlagMethod);

    return new Helper(
        helperClass,
        implType,
        context,
        longNamesField,
        shortNamesField,
        optMapField,
        sMapField,
        flagsField,
        option,
        optionType,
        buildMethod,
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
        chopOffHeadMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addFields(Arrays.asList(
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
          .addMethod(chopOffHeadMethod)
          .addMethod(readGroupMethod)
          .addMethod(readOptionFromGroupMethod);
    }
    return builder.build();
  }

  private static MethodSpec addFlagMethod(
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec flags) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    return MethodSpec.methodBuilder("add")
        .addStatement("assert $N.type == $T.$L", option, optionTypeClass, Type.FLAG)
        .addStatement("return $N.add($N)", flags, option)
        .addParameter(option)
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec addMethod(
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec optMap,
      FieldSpec sMap,
      MethodSpec isBindingMethod) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec bucket = ParameterSpec.builder(LIST_OF_STRING, "bucket").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("add");
    builder.addStatement("assert $N.$N()", option, isBindingMethod);

    // begin handle repeatable
    builder.beginControlFlow("if ($N.type == $T.$L)", option, optionTypeClass, Type.REPEATABLE);

    builder.addStatement("$T $N = $N.get($N)", bucket.type, bucket, optMap, option);
    builder.beginControlFlow("if ($N == null)", bucket)
        .addStatement("$N = new $T<>()", bucket, ArrayList.class)
        .addStatement("$N.put($N, $N)", optMap, option, bucket)
        .endControlFlow();
    builder.addStatement("$N.add($N)", bucket, argument);
    builder.addStatement("return $L", true);

    // done handling repeatable
    builder.endControlFlow();

    builder.beginControlFlow("if ($N.containsKey($N))", sMap, option)
        .addStatement("return $L", false)
        .endControlFlow()
        .addStatement("$N.put($N, $N)", sMap, option, argument);

    builder.addStatement("return $L", true);

    return builder.addParameters(Arrays.asList(option, argument))
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec readRegularOptionMethod(
      FieldSpec shortNamesField,
      FieldSpec optionTypeField,
      ClassName optionTypeType,
      ClassName optionType,
      MethodSpec readLongMethod,
      MethodSpec looksLikeLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N($N))", looksLikeLongMethod, token)
        .addStatement("return $N($N)", readLongMethod, token)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .add("// not an option\n")
        .addStatement("return null")
        .endControlFlow();

    builder.add("\n");
    builder.addStatement("$T $N = $N.get($N.substring(1, 2))", option.type, option, shortNamesField, token);
    builder.add("\n");

    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != $T.$L)", option, optionTypeField, optionTypeType, Type.FLAG)
        .add("// not a flag, possibly attached value\n")
        .addStatement("return $N", option)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() > 2)", token)
        .add("// flags cannot have an attached value\n")
        .addStatement("return null")
        .endControlFlow();
    builder.addStatement("return $N", option);

    return MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(optionType)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readOptionFromGroupMethod(
      FieldSpec shortNames,
      ClassName optionClass,
      MethodSpec readLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    ParameterSpec shortName = ParameterSpec.builder(STRING, "shortName").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N == null || $N.isEmpty())", token, token)
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() >= 2 && $N.charAt(0) == '-')", token, token)
        .addStatement("$N = $N.substring(1)", token, token)
        .endControlFlow();

    builder.addStatement("$T $N = $N.substring(0, 1)", shortName.type, shortName, token);

    builder.addStatement("return $N.get($N)", shortNames, shortName);

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
      MethodSpec readMethod,
      MethodSpec looksLikeGroupMethod,
      MethodSpec readNextMethod,
      MethodSpec readOptionFromGroupMethod,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec chopOffShortFlagMethod,
      MethodSpec addMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();

    ParameterSpec freeToken = ParameterSpec.builder(STRING, "freeToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N($N))", looksLikeGroupMethod, freeToken)
        .addStatement("return $N($N, $N)", readMethod, freeToken, it)
        .endControlFlow();

    builder.add("\n");
    builder.addStatement("$T $N = $N($N)", option.type, option, readOptionFromGroupMethod, freeToken);
    builder.addStatement("$T $N = $N", STRING, token, freeToken);
    builder.add("\n");

    builder.beginControlFlow("if ($N.length() >= 1 && $N.charAt(0) == '-')", token, token)
        .addStatement("$N = $N.substring(1)", token, token)
        .endControlFlow();
    builder.add("\n");

    // begin option group loop
    builder.beginControlFlow("do");

    builder.beginControlFlow("if (!$N($N))", addFlagMethod, option)
        .add(throwRepetitionErrorInGroup(option, freeToken))
        .endControlFlow();
    builder.addStatement("$N = $N($N)", token, chopOffShortFlagMethod, token);
    builder.addStatement("$N = $N($N)", option, readOptionFromGroupMethod, token);

    // end option group loop
    builder.endControlFlow("while ($N != null && $N.$N == $T.$L)",
        option, option, optionType, optionTypeClass, Type.FLAG);

    builder.add("\n");
    builder.beginControlFlow("if ($N == null)", option)
        .add("// the group ended in a flag\n")
        .beginControlFlow("if ($N != null)", token)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Unknown token in option group: ", freeToken)
        .endControlFlow()
        .addStatement("return $L", true)
        .endControlFlow();

    // if we got here, the option must be binding, so read next token
    builder.add("\n");
    builder.add("// the group ended in a binding token\n");
    builder.beginControlFlow("if ($N.length() != 1)", token)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option group:: ", freeToken)
        .endControlFlow();
    builder.add("\n");

    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readNextMethod, token, it);

    builder.beginControlFlow("if (!$N($N, $N))", addMethod, option, argument)
        .add(repetitionError(option, token))
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("readGroup")
        .addParameters(Arrays.asList(freeToken, it))
        .addCode(builder.build())
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec readMethod(
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
    ParameterSpec chopped = ParameterSpec.builder(STRING, "chopped").build();

    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N($N)", option.type, option, readRegularOptionMethod, token);

    // unknown token
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.add("\n");
    builder.add("// handle flag\n");

    // handle flag
    builder.beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, Type.FLAG);

    builder.beginControlFlow("if (!$N($N))", addFlagMethod, option)
        .add(throwRepetitionErrorInGroup(option, token))
        .endControlFlow();

    builder.addStatement("return $L", true);

    // end handle flag
    builder.endControlFlow();

    builder.add("\n");
    builder.add("// option is binding\n");
    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readArgumentMethod, token, it);

    builder.beginControlFlow("if (!$N($N, $N))", addMethod, option, argument)
        .add(repetitionError(option, token))
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(token, it))
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
        .addParameters(Arrays.asList(token, it))
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
        .addParameters(Arrays.asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec chopOffHeadMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("chopOffHead");

    builder.beginControlFlow("if ($N.length() <= 1)", token)
        .addStatement("return null")
        .endControlFlow();

    builder.addStatement("return $N.substring(1)", token);

    return builder.addParameter(token)
        .addModifiers(STATIC)
        .returns(STRING)
        .build();
  }

  private static MethodSpec looksLikeLongMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("looksLikeLong");

    builder.beginControlFlow("if ($N.length() <= 1)", token)
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
      FieldSpec optionTypeField,
      FieldSpec shortNamesField,
      ClassName optionType,
      ClassName optionTypeType) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec originalToken = ParameterSpec.builder(STRING, "originalToken").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N($N))", looksLikeLongMethod, originalToken)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.addStatement("$T $N = $N", token.type, token, originalToken);

    builder.beginControlFlow("if ($N.length() >= 1 && $N.charAt(0) == '-')", token, token)
        .addStatement("$N = $N.substring(1)", token, token)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() <= 1)", token)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.add("\n");
    builder.addStatement("$T $N = $N.get($N.substring(0, 1))", option.type, option, shortNamesField, token);
    builder.add("\n");

    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != $T.$L)", option, optionTypeField, optionTypeType, Type.FLAG)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.beginControlFlow("if ($N.indexOf('-') >= 0)", token)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Found hyphen in group: ", originalToken)
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("looksLikeGroup")
        .addParameter(originalToken)
        .returns(BOOLEAN)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec buildMethod(
      ClassName implType,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField) {
    ParameterSpec otherTokens = ParameterSpec.builder(LIST_OF_STRING, "otherTokens").build();
    ParameterSpec rest = ParameterSpec.builder(LIST_OF_STRING, "rest").build();
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($N, $N, $N, $N, $N)", implType,
            optMapField, sMapField, flagsField, otherTokens, rest)
        .addParameters(Arrays.asList(otherTokens, rest))
        .returns(implType)
        .build();
  }

  private static CodeBlock repetitionError(
      ParameterSpec option,
      ParameterSpec token) {
    return CodeBlock.builder()
        .addStatement("throw new $T($S + $N + $S + $N + $S)",
            IllegalArgumentException.class,
            "Found token: ", token, ", but option ", option, " is not repeatable")
        .build();
  }

  private static CodeBlock throwRepetitionErrorInGroup(
      ParameterSpec option,
      ParameterSpec originalToken) {
    return CodeBlock.builder()
        .addStatement("throw new $T($S + $N + $S + $N + $S)",
            IllegalArgumentException.class,
            "In option group ",
            originalToken, ": option ", option, " is not repeatable")
        .build();
  }
}
