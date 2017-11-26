package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Parser.repetitionError;
import static net.jbock.compiler.Parser.throwRepetitionErrorInGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the private *_Parser.Helper inner class.
 *
 * @see Parser
 */
final class Helper {

  final ClassName type;

  final FieldSpec otherTokensField = FieldSpec.builder(LIST_OF_STRING, "otherTokens", FINAL)
      .build();

  private final Option option;
  private final OptionType optionType;

  final FieldSpec optMapField;
  final FieldSpec sMapField;
  final FieldSpec flagsField;
  final FieldSpec longNamesField;
  final FieldSpec shortNamesField;

  final MethodSpec addFlagMethod;
  final MethodSpec addMethod;
  final MethodSpec readMethod;
  final MethodSpec readNextMethod;
  final MethodSpec readLongMethod;
  final MethodSpec readRegularOptionMethod;
  final MethodSpec readOptionFromGroupMethod;

  private final MethodSpec readArgumentMethod;
  private final MethodSpec chopOffShortFlagMethod;

  private Helper(
      ClassName type,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField,
      Option option,
      OptionType optionType,
      MethodSpec addFlagMethod,
      MethodSpec addMethod,
      MethodSpec readMethod,
      MethodSpec readNextMethod,
      MethodSpec readLongMethod,
      MethodSpec readRegularOptionMethod,
      MethodSpec readOptionFromGroupMethod,
      MethodSpec readArgumentMethod,
      MethodSpec chopOffShortFlagMethod) {
    this.type = type;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.optMapField = optMapField;
    this.sMapField = sMapField;
    this.flagsField = flagsField;
    this.option = option;
    this.optionType = optionType;
    this.addFlagMethod = addFlagMethod;
    this.addMethod = addMethod;
    this.readMethod = readMethod;
    this.readNextMethod = readNextMethod;
    this.readLongMethod = readLongMethod;
    this.readRegularOptionMethod = readRegularOptionMethod;
    this.readOptionFromGroupMethod = readOptionFromGroupMethod;
    this.readArgumentMethod = readArgumentMethod;
    this.chopOffShortFlagMethod = chopOffShortFlagMethod;
  }

  static Helper create(
      Context context,
      OptionType optionType,
      Option option) {
    MethodSpec readNextMethod = readNextMethod();
    MethodSpec readArgumentMethod = readArgumentMethod(readNextMethod);
    MethodSpec chopOffShortFlagMethod = chopOffShortFlagMethod();
    FieldSpec longNamesField = FieldSpec.builder(option.stringOptionMapType, "longNames")
        .addModifiers(FINAL)
        .build();
    FieldSpec shortNamesField = FieldSpec.builder(option.stringOptionMapType, "shortNames")
        .addModifiers(FINAL)
        .build();
    ClassName helperClass = context.generatedClass.nestedClass("Helper");
    FieldSpec optMapField = FieldSpec.builder(option.optMapType, "optMap", FINAL).build();
    FieldSpec sMapField = FieldSpec.builder(option.sMapType, "sMap", FINAL).build();
    FieldSpec flagsField = FieldSpec.builder(option.flagsType, "flags", FINAL).build();
    MethodSpec addFlagMethod = addFlagMethod(option.type, optionType.type, flagsField);
    MethodSpec addMethod = addMethod(option.type, optionType.type,
        optMapField, sMapField, option.isBindingMethod);
    MethodSpec readLongMethod = readLongMethod(
        longNamesField, option.type);
    MethodSpec readRegularOptionMethod = readRegularOptionMethod(
        shortNamesField, option.type, readLongMethod);
    MethodSpec readOptionFromGroupMethod = readOptionFromGroupMethod(
        shortNamesField, option.type, readLongMethod);

    MethodSpec readMethod = readMethod(
        context,
        helperClass,
        readArgumentMethod,
        readRegularOptionMethod,
        readOptionFromGroupMethod,
        option.optMapType,
        option.sMapType,
        option.flagsType,
        option.type,
        option.typeField,
        optionType.type,
        chopOffShortFlagMethod,
        addMethod,
        addFlagMethod);

    return new Helper(
        helperClass,
        longNamesField,
        shortNamesField,
        optMapField,
        sMapField,
        flagsField,
        option,
        optionType,
        addFlagMethod,
        addMethod,
        readMethod,
        readNextMethod,
        readLongMethod,
        readRegularOptionMethod,
        readOptionFromGroupMethod,
        readArgumentMethod,
        chopOffShortFlagMethod);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(type)
        .addFields(Arrays.asList(
            longNamesField,
            shortNamesField,
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
        .addMethod(privateConstructor())
        .addMethod(readMethod)
        .addMethod(readRegularOptionMethod)
        .addMethod(readOptionFromGroupMethod)
        .addMethod(addMethod)
        .addMethod(addFlagMethod)
        .addMethod(readArgumentMethod)
        .addMethod(readNextMethod)
        .addMethod(readLongMethod)
        .addMethod(chopOffShortFlagMethod)
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec longNames = ParameterSpec.builder(this.longNamesField.type, this.longNamesField.name)
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(this.shortNamesField.type, this.shortNamesField.name)
        .build();
    ParameterSpec option = ParameterSpec.builder(this.option.type, "option")
        .build();

    builder.add("\n");
    builder.addStatement("$T $N = new $T<>()",
        longNames.type, longNames, HashMap.class)
        .addStatement("$T $N = new $T<>()",
            shortNames.type, shortNames, HashMap.class);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", this.option.type, option, this.option.type);

    builder.beginControlFlow("if ($N.$N != null)", option, this.option.shortNameField)
        .addStatement("$N.put($N.$N.toString(), $N)", shortNames, option, this.option.shortNameField, option)
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != null)", option, this.option.longNameField)
        .addStatement("$N.put($N.$N, $N)", longNames, option, this.option.longNameField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();

    builder.addStatement("this.$N = $T.unmodifiableMap($N)",
        longNames, Collections.class, longNames);

    builder.addStatement("this.$N = $T.unmodifiableMap($N)",
        shortNames, Collections.class, shortNames);

    return MethodSpec.constructorBuilder()
        .addCode(builder.build())
        .build();
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
      FieldSpec shortNames,
      ClassName optionClass,
      MethodSpec readLongMethod) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token)
        .add("// not an option\n")
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.charAt(1) != '-')", token)
        .add("// short option\n")
        .addStatement("return $N.get($N.substring(1, 2))", shortNames, token)
        .endControlFlow();

    builder.addStatement("return $N($N)", readLongMethod, token);

    return MethodSpec.methodBuilder("readRegularOption")
        .addParameter(token)
        .returns(optionClass)
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

    builder.beginControlFlow("if ($N.length() >= 2 && $N.charAt(0) == '-' && $N.charAt(1) == '-')",
        token, token, token)
        .addStatement("return $N($N)", readLongMethod, token)
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
      FieldSpec longNames,
      ClassName optionClass) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("$T $N = $N.indexOf('=')", INT, index, token);

    builder.beginControlFlow("if ($N < 0)", index)
        .addStatement("return $N.get($N.substring(2))", longNames, token)
        .endControlFlow();

    builder.addStatement("return $N.get($N.substring(2, $N))", longNames, token, index);

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
  private static MethodSpec readMethod(
      Context context,
      ClassName keysClass,
      MethodSpec readArgumentMethod,
      MethodSpec readRegularOptionMethod,
      MethodSpec readOptionFromGroupMethod,
      TypeName optMapType,
      TypeName sMapType,
      TypeName flagsType,
      ClassName optionClass,
      FieldSpec optionType,
      ClassName optionTypeClass,
      MethodSpec chopOffShortFlagMethod,
      MethodSpec addMethod,
      MethodSpec addFlagMethod) {

    ParameterSpec optMap = ParameterSpec.builder(optMapType, "optMap").build();
    ParameterSpec sMap = ParameterSpec.builder(sMapType, "sMap").build();
    ParameterSpec flags = ParameterSpec.builder(flagsType, "flags").build();
    ParameterSpec otherTokens = ParameterSpec.builder(LIST_OF_STRING, "otherTokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec bucket = ParameterSpec.builder(LIST_OF_STRING, "bucket").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();

    ParameterSpec freeToken = ParameterSpec.builder(STRING, "freeToken").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N($N)", option.type, option, readRegularOptionMethod, freeToken);

    // unknown token
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("return $L", false)
        .endControlFlow();

    builder.add("\n");
    builder.add("// handle flags\n");
    builder.addStatement("$T $N = $N", STRING, token, freeToken);

    // begin option group loop
    builder.beginControlFlow("while ($N.$N == $T.$L)", option, optionType, optionTypeClass, Type.FLAG);

    builder.beginControlFlow("if (!$N($N))", addFlagMethod, option)
        .add(throwRepetitionErrorInGroup(option, freeToken))
        .endControlFlow();
    builder.addStatement("$N = $N($N)", token, chopOffShortFlagMethod, token);
    builder.beginControlFlow("if ($N == null)", token)
        .add("// done reading flags\n")
        .addStatement("return $L", true)
        .endControlFlow();
    builder.addStatement("$N = $N($N)", option, readOptionFromGroupMethod, token);
    builder.beginControlFlow("if ($N == null)", option)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Unknown token in option group: ", freeToken)
        .endControlFlow();

    // end option group loop
    builder.endControlFlow();

    // if we got here, the option must be binding, so read next token
    builder.add("\n");
    builder.add("// option is now binding\n");
    builder.addStatement("$T $N = $N($N, $N)", argument.type, argument, readArgumentMethod, token, it);

    builder.beginControlFlow("if (!$N($N, $N))", addMethod, option, argument)
        .add(repetitionError(option, token))
        .endControlFlow();

    builder.addStatement("return $L", true);

    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(freeToken, it))
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

    // handle bsd
    builder.beginControlFlow("if ($N.charAt(0) != '-')", token);

    builder.beginControlFlow("if ($N.length() >= 2)", token)
        .add("// attached short\n")
        .addStatement("return $N.substring(1)", token)
        .endControlFlow();

    builder.addStatement("return $N($N, $N)", readNextMethod, token, it);

    // end handle bsd
    builder.endControlFlow();

    builder.add("\n");
    builder.addStatement("assert $N.length() >= 2", token);
    builder.add("\n");

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
        .addModifiers(PRIVATE, STATIC)
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
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec chopOffShortFlagMethod() {

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec rest = ParameterSpec.builder(STRING, "rest").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("chopOffShortFlag");

    builder.beginControlFlow("if ($N.length() >= 2 && $N.charAt(0) == '-' && $N.charAt(1) == '-')",
        token, token, token)
        .addCode("// long flag\n")
        .addStatement("return null")
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() >= 1 && $N.charAt(0) == '-')", token, token)
        .addStatement("$N = $N.substring(1)", token, token)
        .endControlFlow();

    builder.beginControlFlow("if ($N.length() <= 1)", token)
        .addStatement("return null")
        .endControlFlow();

    builder.addStatement("$T $N = $N.substring(1)", rest.type, rest, token);

    builder.beginControlFlow("if ($N.charAt(0) == '-')", rest)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Found hyphen in option group: ", token)
        .endControlFlow();

    builder.addStatement("return $N", rest);

    return builder.addParameter(token)
        .addModifiers(PRIVATE, STATIC)
        .returns(STRING)
        .build();
  }
}
