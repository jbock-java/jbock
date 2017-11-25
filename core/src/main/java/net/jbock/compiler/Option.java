package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Type.EVERYTHING_AFTER;
import static net.jbock.compiler.Type.OTHER_TOKENS;
import static net.jbock.compiler.Util.snakeCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * Defines the *_Parser.Option inner class.
 *
 * @see Parser
 */
final class Option {

  final ClassName type;

  private final FieldSpec descriptionField;
  private final FieldSpec argumentNameField;

  private final Context context;
  private final OptionType optionType;

  private final MethodSpec describeNamesMethod;
  private final MethodSpec describeParamMethod;
  private final MethodSpec descriptionBlockMethod;

  final FieldSpec longNameField;
  final FieldSpec shortNameField;
  final FieldSpec typeField;

  final MethodSpec isSpecialMethod;
  final MethodSpec isBindingMethod;

  final TypeName optMapType;
  final TypeName sMapType;
  final TypeName flagsType;
  final TypeName stringOptionMapType;

  private Option(
      Context context,
      ClassName type,
      OptionType optionType,
      FieldSpec typeField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      MethodSpec isSpecialMethod,
      MethodSpec isBindingMethod) {
    this.descriptionField = descriptionField;
    this.argumentNameField = argumentNameField;
    this.longNameField = FieldSpec.builder(STRING, "longName", PRIVATE, FINAL).build();
    this.shortNameField = FieldSpec.builder(ClassName.get(Character.class),
        "shortName", PRIVATE, FINAL).build();
    this.context = context;
    this.type = type;
    this.optionType = optionType;
    this.typeField = typeField;
    this.isSpecialMethod = isSpecialMethod;
    this.isBindingMethod = isBindingMethod;
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
    this.optMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        type, LIST_OF_STRING);
    this.sMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        type, STRING);
    this.flagsType = ParameterizedTypeName.get(ClassName.get(Set.class),
        type);
    this.stringOptionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, type);
  }

  static Option create(Context context, OptionType optionType) {
    FieldSpec typeField = FieldSpec.builder(optionType.type, "type", PRIVATE, FINAL).build();
    MethodSpec isSpecialMethod = isSpecialMethod(optionType, typeField);
    MethodSpec isBindingMethod = isBindingMethod(optionType, typeField);
    FieldSpec descriptionField = FieldSpec.builder(
        LIST_OF_STRING, "description", PRIVATE, FINAL).build();
    FieldSpec argumentNameField = FieldSpec.builder(
        STRING, "descriptionArgumentName", PRIVATE, FINAL).build();
    return new Option(
        context,
        context.generatedClass.nestedClass("Option"),
        optionType,
        typeField,
        descriptionField,
        argumentNameField,
        isSpecialMethod,
        isBindingMethod);
  }

  String enumConstant(int i) {
    String result = snakeCase(context.parameters.get(i).parameterName());
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
              param.optionType(), argumentName, STRING);
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
        .beginControlFlow("if (!$N.$N())", optionType, this.optionType.isSpecialMethod)
        .addStatement("assert $N == null || $N.length() == 1", shortName, shortName)
        .addStatement("assert $N == null || !$N.isEmpty()", longName, longName)
        .addStatement("assert $N != null || $N != null", longName, shortName)
        .endControlFlow();

    builder
        .addStatement("this.$N = $N", longNameField, longName)
        .addStatement("this.$N = $N == null ? null : $N.charAt(0)", shortNameField, shortName, shortName)
        .addStatement("this.$N = $N", this.typeField, optionType)
        .addStatement("this.$N = $T.unmodifiableList($T.asList($N))", descriptionField,
            Collections.class, Arrays.class, description)
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
    return MethodSpec.methodBuilder(optionType.isSpecialMethod.name)
        .addStatement("return $N.$N()", optionTypeField, optionType.isSpecialMethod)
        .returns(optionType.isSpecialMethod.returnType)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec isBindingMethod(
      OptionType optionType,
      FieldSpec optionTypeField) {
    return MethodSpec.methodBuilder(optionType.isBindingMethod.name)
        .addStatement("return $N.$N()", optionTypeField, optionType.isBindingMethod)
        .returns(optionType.isBindingMethod.returnType)
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
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    ParameterSpec sIndent = ParameterSpec.builder(STRING, "indentString").build();
    ParameterSpec aIndent = ParameterSpec.builder(ArrayTypeName.of(TypeName.CHAR), "a").build();
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = new $T[$N]", aIndent.type, aIndent, TypeName.CHAR, indent)
        .addStatement("$T.fill($N, ' ')", Arrays.class, aIndent)
        .addStatement("$T $N = new $T($N)", STRING, sIndent, STRING, aIndent)
        .addStatement("return $N + $T.join('\\n' + $N, $N)", sIndent, STRING, sIndent, descriptionField);
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
    builder.beginControlFlow("if ($N.$N())", optionTypeField, optionType.isBindingMethod)
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

    builder.beginControlFlow("if ($N == $T.$L)", optionTypeField, optionType.type, OTHER_TOKENS)
        .addStatement("return $S", "Other tokens")
        .endControlFlow();

    builder.beginControlFlow("if ($N == $T.$L)", optionTypeField, optionType.type, EVERYTHING_AFTER)
        .addStatement("return $S + $S + $S", "Everything after '", context.stopword, "'")
        .endControlFlow();

    builder.addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class);

    builder.beginControlFlow("if ($N != null)", shortNameField)
        .addStatement("$N.append('-').append($N)", sb, shortNameField)
        .endControlFlow();

    builder.beginControlFlow("if ($N != null && $N != null)", longNameField, shortNameField)
        .addStatement("$N.append(',').append(' ')", sb)
        .endControlFlow();

    builder.beginControlFlow("if ($N != null)", longNameField)
        .addStatement("$N.append('-').append('-').append($N)", sb, longNameField)
        .endControlFlow();

    builder.addStatement("return $N.toString()", sb);

    return MethodSpec.methodBuilder("describeParam")
        .addModifiers(PRIVATE)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec toStringMethod() {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    return MethodSpec.methodBuilder("toString")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .returns(STRING)
        .addStatement("return name() + $S + $N() + $S", " (", describeParamMethod, ")")
        .build();
  }
}
