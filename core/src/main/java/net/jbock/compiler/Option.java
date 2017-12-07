package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Type.POSITIONAL;
import static net.jbock.compiler.Util.snakeCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.StringJoiner;
import net.jbock.Description;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see Parser
 */
final class Option {

  final ClassName type;
  final OptionType optionType;

  private final FieldSpec descriptionField;
  private final FieldSpec argumentNameField;

  private final Context context;

  private final MethodSpec describeNamesMethod;
  private final MethodSpec describeParamMethod;
  private final MethodSpec descriptionBlockMethod;

  private final FieldSpec longNameField;

  final FieldSpec shortNameField;
  final FieldSpec typeField;

  final MethodSpec shortNameMapMethod;
  final MethodSpec longNameMapMethod;

  final MethodSpec extractOptionalIntMethod;

  final ParameterSpec optMapParameter;
  final ParameterSpec sMapParameter;
  final ParameterSpec flagsParameter;
  final ParameterSpec positionalParameter;

  private final MethodSpec isSpecialMethod;
  private final MethodSpec isBindingMethod;

  private Option(
      Context context,
      ClassName type,
      OptionType optionType,
      MethodSpec extractOptionalIntMethod,
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec typeField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      MethodSpec shortNameMapMethod,
      MethodSpec longNameMapMethod,
      MethodSpec isSpecialMethod,
      MethodSpec isBindingMethod,
      ParameterSpec optMapParameter,
      ParameterSpec sMapParameter,
      ParameterSpec flagsParameter,
      ParameterSpec positionalParameter) {
    this.extractOptionalIntMethod = extractOptionalIntMethod;
    this.longNameField = longNameField;
    this.shortNameField = shortNameField;
    this.descriptionField = descriptionField;
    this.argumentNameField = argumentNameField;
    this.shortNameMapMethod = shortNameMapMethod;
    this.context = context;
    this.type = type;
    this.optionType = optionType;
    this.typeField = typeField;
    this.longNameMapMethod = longNameMapMethod;
    this.isSpecialMethod = isSpecialMethod;
    this.isBindingMethod = isBindingMethod;
    this.optMapParameter = optMapParameter;
    this.sMapParameter = sMapParameter;
    this.flagsParameter = flagsParameter;
    this.positionalParameter = positionalParameter;
    this.describeParamMethod = describeParamMethod(
        context,
        longNameField,
        shortNameField,
        typeField,
        optionType);
    this.describeNamesMethod = describeNamesMethod(
        describeParamMethod,
        typeField,
        argumentNameField,
        optionType);
    this.descriptionBlockMethod = descriptionBlockMethod(descriptionField);
  }

  static Option create(Context context, OptionType optionType) {
    FieldSpec typeField = FieldSpec.builder(optionType.type, "type", PRIVATE, FINAL).build();
    FieldSpec longNameField = FieldSpec.builder(STRING, "longName", PRIVATE, FINAL).build();
    FieldSpec shortNameField = FieldSpec.builder(ClassName.get(Character.class),
        "shortName", PRIVATE, FINAL).build();
    MethodSpec isSpecialMethod = isSpecialMethod(optionType, typeField);
    ClassName type = context.generatedClass.nestedClass("Option");
    MethodSpec isBindingMethod = isBindingMethod(optionType, typeField);
    MethodSpec shortNameMapMethod = shortNameMapMethod(type, shortNameField);
    MethodSpec longNameMapMethod = longNameMapMethod(type, longNameField);
    FieldSpec descriptionField = FieldSpec.builder(
        LIST_OF_STRING, "description", PRIVATE, FINAL).build();
    FieldSpec argumentNameField = FieldSpec.builder(
        STRING, "descriptionArgumentName", PRIVATE, FINAL).build();
    ParameterSpec optMapParameter = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        type, LIST_OF_STRING), "optMap").build();
    ParameterSpec sMapParameter = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        type, STRING), "sMap").build();
    ParameterSpec flagsParameter = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        type), "flags").build();
    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional")
        .build();
    MethodSpec extractOptionalIntMethod = extractOptionalIntMethod(type, sMapParameter);

    return new Option(
        context,
        type,
        optionType,
        extractOptionalIntMethod,
        longNameField,
        shortNameField,
        typeField,
        descriptionField,
        argumentNameField,
        shortNameMapMethod,
        longNameMapMethod,
        isSpecialMethod,
        isBindingMethod,
        optMapParameter,
        sMapParameter,
        flagsParameter,
        positionalParameter);
  }

  String enumConstant(int i) {
    String result = snakeCase(context.parameters.get(i).methodName());
    if (!context.problematicOptionNames) {
      return result;
    }
    return result + '_' + i;
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (int i = 0; i < context.parameters.size(); i++) {
      Param param = context.parameters.get(i);
      String[] desc = getText(param.description());
      String argumentName = param.descriptionArgumentName();
      String enumConstant = enumConstant(i);
      String format = String.format("$S, $S, $T.$L, $S, new $T[] {\n    %s}",
          String.join(",\n    ", Collections.nCopies(desc.length, "$S")));
      List<Comparable<? extends Comparable<?>>> fixArgs =
          Arrays.asList(param.longName(), param.shortName(), optionType.type,
              param.paramType, argumentName, STRING);
      List<Object> args = new ArrayList<>(fixArgs.size() + desc.length);
      args.addAll(fixArgs);
      args.addAll(Arrays.asList(desc));
      builder.addEnumConstant(enumConstant, anonymousClassBuilder(format,
          args.toArray()).build());
    }
    return builder.addModifiers(PUBLIC)
        .addFields(Arrays.asList(longNameField, shortNameField, typeField, argumentNameField, descriptionField))
        .addMethod(describeMethod())
        .addMethod(toStringMethod())
        .addMethod(describeNamesMethod)
        .addMethod(describeParamMethod)
        .addMethod(descriptionBlockMethod)
        .addMethod(shortNameMethod(shortNameField))
        .addMethod(longNameMethod(longNameField))
        .addMethod(descriptionMethod())
        .addMethod(descriptionArgumentNameMethod(argumentNameField))
        .addMethod(typeMethod())
        .addMethod(isSpecialMethod)
        .addMethod(isBindingMethod)
        .addMethod(privateConstructor())
        .addMethod(shortNameMapMethod)
        .addMethod(longNameMapMethod)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec longName = ParameterSpec.builder(longNameField.type, longNameField.name).build();
    ParameterSpec shortName = ParameterSpec.builder(STRING, shortNameField.name).build();
    ParameterSpec optionType = ParameterSpec.builder(this.typeField.type, this.typeField.name).build();
    ParameterSpec description = ParameterSpec.builder(ArrayTypeName.of(STRING), descriptionField.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(argumentNameField.type, argumentNameField.name).build();
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    builder
        .addStatement("this.$N = $N", longNameField, longName)
        .addStatement("this.$N = $N == null ? null : $N.charAt(0)", shortNameField, shortName, shortName)
        .addStatement("this.$N = $N", this.typeField, optionType)
        .addStatement("this.$N = $T.asList($N)", descriptionField, Arrays.class, description)
        .addStatement("this.$N = $N", argumentNameField, argumentName);

    builder.addParameters(Arrays.asList(
        longName, shortName, optionType, argumentName, description));
    return builder.build();
  }

  private static MethodSpec shortNameMethod(FieldSpec shortNameField) {
    return MethodSpec.methodBuilder(shortNameField.name)
        .addStatement("return $T.ofNullable($N)", Optional.class, shortNameField)
        .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(Character.class)))
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec shortNameMapMethod(
      ClassName optionType,
      FieldSpec shortNameField) {
    ParameterSpec shortNames = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, optionType), "shortNames")
        .build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>($T.values().length)",
        shortNames.type, shortNames, HashMap.class, optionType);

    // begin iteration over options
    builder.add("\n");
    builder.beginControlFlow("for ($T $N : $T.values())", optionType, option, optionType);

    builder.beginControlFlow("if ($N.$N != null)", option, shortNameField)
        .addStatement("$N.put($N.$N.toString(), $N)", shortNames, option, shortNameField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $T.unmodifiableMap($N)", Collections.class, shortNames);

    return MethodSpec.methodBuilder("shortNameMap")
        .addCode(builder.build())
        .returns(shortNames.type)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec longNameMapMethod(
      ClassName optionType,
      FieldSpec longNameField) {
    ParameterSpec longNames = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, optionType), "longNames")
        .build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>($T.values().length)",
        longNames.type, longNames, HashMap.class, optionType);

    // begin iteration over options
    builder.add("\n");
    builder.beginControlFlow("for ($T $N : $T.values())", optionType, option, optionType);

    builder.beginControlFlow("if ($N.$N != null)", option, longNameField)
        .addStatement("$N.put($N.$N, $N)", longNames, option, longNameField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $T.unmodifiableMap($N)", Collections.class, longNames);

    return MethodSpec.methodBuilder("longNameMap")
        .addCode(builder.build())
        .returns(longNames.type)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec longNameMethod(FieldSpec longNameField) {
    return MethodSpec.methodBuilder(longNameField.name)
        .addStatement("return $T.ofNullable($N)", Optional.class, longNameField)
        .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), STRING))
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec descriptionMethod() {
    return MethodSpec.methodBuilder(descriptionField.name)
        .addStatement("return $N", descriptionField)
        .returns(descriptionField.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec descriptionArgumentNameMethod(FieldSpec argumentNameField) {
    return MethodSpec.methodBuilder(argumentNameField.name)
        .addStatement("return $T.ofNullable($N)", Optional.class, argumentNameField)
        .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), STRING))
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec typeMethod() {
    return MethodSpec.methodBuilder(typeField.name)
        .addStatement("return $N", typeField)
        .returns(typeField.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec isSpecialMethod(
      OptionType optionType,
      FieldSpec optionTypeField) {
    return MethodSpec.methodBuilder("isSpecial")
        .addStatement("return $N.$N", optionTypeField, optionType.isSpecialField)
        .returns(BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec isBindingMethod(
      OptionType optionType,
      FieldSpec optionTypeField) {
    return MethodSpec.methodBuilder("isBinding")
        .addStatement("return $N.$N", optionTypeField, optionType.isBindingField)
        .returns(BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }

  private static String[] getText(Description description) {
    if (description == null) {
      return new String[]{"--- description goes here ---"};
    }
    return description.value();
  }

  private static MethodSpec descriptionBlockMethod(FieldSpec descriptionField) {
    ParameterSpec line = ParameterSpec.builder(STRING, "line").build();
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    ParameterSpec spaces = ParameterSpec.builder(STRING, "spaces").build();
    ParameterSpec sp = ParameterSpec.builder(ArrayTypeName.of(TypeName.CHAR), "sp").build();
    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T[$N]", sp.type, sp, TypeName.CHAR, indent);
    builder.addStatement("$T.fill($N, ' ')", Arrays.class, sp);
    builder.addStatement("$T $N = new $T($N)", STRING, spaces, STRING, sp);
    builder.addStatement("$T $N = new $T($S + $N, $N, $S)", joiner.type, joiner, joiner.type,
        "\n", spaces, spaces, "");
    builder.beginControlFlow("for ($T $N : $N)", STRING, line, descriptionField)
        .addStatement("$N.add($N)", joiner, line)
        .endControlFlow();
    builder.addStatement("return $N.toString()", joiner);
    return MethodSpec.methodBuilder("descriptionBlock")
        .addModifiers(PUBLIC)
        .addParameter(indent)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec describeMethod() {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    CodeBlock codeBlock = CodeBlock.builder()
        .addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class)
        .addStatement("$N.append($N())", sb, describeNamesMethod)
        .addStatement("$N.append($S)", sb, "\n")
        .addStatement("$N.append($N($N))", sb, descriptionBlockMethod, indent)
        .addStatement("return $N.toString()", sb)
        .build();
    return MethodSpec.methodBuilder("describe")
        .addModifiers(PUBLIC)
        .returns(STRING)
        .addParameter(indent)
        .addCode(codeBlock)
        .build();
  }

  private static MethodSpec describeNamesMethod(
      MethodSpec describeParamMethod,
      FieldSpec optionTypeField,
      FieldSpec argumentNameField,
      OptionType optionType) {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.beginControlFlow("if ($N.$N)", optionTypeField, optionType.isBindingField)
        .addStatement("return $N() + ' ' + $N", describeParamMethod, argumentNameField)
        .endControlFlow();
    builder.addStatement("return $N()", describeParamMethod);
    return MethodSpec.methodBuilder("describeNames")
        .addModifiers(PUBLIC)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec describeParamMethod(
      Context context,
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec optionTypeField,
      OptionType optionType) {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    if (context.otherTokens) {
      builder.beginControlFlow("if ($N == $T.$L)", optionTypeField, optionType.type, POSITIONAL)
          .addStatement("return $S", "(positional arguments)")
          .endControlFlow();
    }

    builder.beginControlFlow("if ($N == null)", shortNameField)
        .addStatement("return $S + $N", "--", longNameField)
        .endControlFlow();

    builder.beginControlFlow("if ($N == null)", longNameField)
        .addStatement("return $S + $N", "-", shortNameField)
        .endControlFlow();

    builder.addStatement("$T $N = new $T($N.length() + 6)",
        StringBuilder.class, sb, StringBuilder.class, longNameField);
    builder.addStatement("$N.append('-').append($N)", sb, shortNameField);
    builder.addStatement("$N.append(',').append(' ')", sb);
    builder.addStatement("$N.append('-').append('-').append($N)", sb, longNameField);

    builder.addStatement("return $N.toString()", sb);

    return MethodSpec.methodBuilder("describeParam")
        .addModifiers(PRIVATE)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec toStringMethod() {
    return MethodSpec.methodBuilder("toString")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .returns(STRING)
        .addStatement("return name() + $S + $N() + $S", " (", describeParamMethod, ")")
        .build();
  }

  private static MethodSpec extractOptionalIntMethod(
      ClassName optionType,
      ParameterSpec sMapParameter) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("extractOptionalInt");

    builder.addStatement("$T $N = $N.get($N)", STRING, token, sMapParameter, option);

    builder.beginControlFlow("if ($N == null)", token)
        .addStatement("return $T.empty()", OptionalInt.class)
        .endControlFlow();

    builder.addStatement("return $T.of($T.parseInt($N))",
        OptionalInt.class, Integer.class, token);
    return builder.addParameters(Arrays.asList(sMapParameter, option))
        .addModifiers(STATIC)
        .returns(OptionalInt.class).build();
  }
}
