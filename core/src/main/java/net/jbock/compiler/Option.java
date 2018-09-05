package net.jbock.compiler;

import net.jbock.Description;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see Parser
 */
final class Option {

  final ClassName type;
  final OptionType optionType;
  final Context context;

  final MethodSpec describeParamMethod;

  private final MethodSpec exampleMethod;
  private final FieldSpec descriptionField;

  final FieldSpec argumentNameField;

  private final FieldSpec longNameField;

  private final FieldSpec shortNameField;

  private final FieldSpec positionalField;

  final FieldSpec typeField;

  final MethodSpec shortNameMapMethod;
  final MethodSpec longNameMapMethod;

  private Option(
      Context context,
      ClassName type,
      OptionType optionType,
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec positionalField,
      FieldSpec typeField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      MethodSpec exampleMethod,
      MethodSpec shortNameMapMethod,
      MethodSpec longNameMapMethod,
      MethodSpec describeParamMethod) {
    this.positionalField = positionalField;
    this.exampleMethod = exampleMethod;
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
    this.describeParamMethod = describeParamMethod;
  }

  static Option create(Context context, OptionType optionType) {
    FieldSpec typeField = FieldSpec.builder(optionType.type, "type").build();
    FieldSpec longNameField = FieldSpec.builder(STRING, "longName").build();
    FieldSpec positionalField = FieldSpec.builder(BOOLEAN, "positional").build();
    FieldSpec shortNameField = FieldSpec.builder(ClassName.get(Character.class),
        "shortName").build();
    ClassName type = context.generatedClass.nestedClass("Option");
    MethodSpec shortNameMapMethod = shortNameMapMethod(type, shortNameField);
    MethodSpec longNameMapMethod = longNameMapMethod(type, longNameField);
    FieldSpec argumentNameField = FieldSpec.builder(
        STRING, "descriptionArgumentName").build();
    MethodSpec exampleMethod = exampleMethod(longNameField, shortNameField, argumentNameField);
    FieldSpec descriptionField = FieldSpec.builder(
        LIST_OF_STRING, "description").build();

    MethodSpec describeParamMethod = describeParamMethod(
        longNameField,
        shortNameField);

    return new Option(
        context,
        type,
        optionType,
        longNameField,
        shortNameField,
        positionalField,
        typeField,
        descriptionField,
        argumentNameField,
        exampleMethod,
        shortNameMapMethod,
        longNameMapMethod,
        describeParamMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Param param : context.parameters) {
      String[] desc = getText(param.description());
      String argumentName = param.descriptionArgumentName();
      String enumConstant = param.enumConstant();
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("longName", param.longName());
      map.put("shortName", param.shortName() == null ? "null" : "'" + param.shortName() + "'");
      map.put("type", param.isPositional() ? param.positionalType() : param.paramType);
      map.put("positional", param.isPositional());
      map.put("optionType", optionType.type);
      map.put("argumentName", argumentName);
      map.put("descExpression", descExpression(desc));
      String format = String.join(", ",
          "$longName:S",
          "$shortName:L",
          "$positional:L",
          "$optionType:T.$type:L",
          "$argumentName:S",
          "$descExpression:L");
      builder.addEnumConstant(enumConstant,
          anonymousClassBuilder(CodeBlock.builder().addNamed(format, map).build())
              .build());
    }
    builder.addModifiers(PRIVATE)
        .addFields(asList(longNameField, shortNameField, positionalField, typeField, argumentNameField, descriptionField))
        .addMethod(describeParamMethod)
        .addMethod(exampleMethod)
        .addMethod(privateConstructor());
    if (!context.nonpositionalParamTypes.isEmpty()) {
      builder.addMethod(shortNameMapMethod)
          .addMethod(longNameMapMethod);
    }
    return builder.build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec longName = ParameterSpec.builder(longNameField.type, longNameField.name).build();
    ParameterSpec shortName = ParameterSpec.builder(shortNameField.type, shortNameField.name).build();
    ParameterSpec positional = ParameterSpec.builder(positionalField.type, positionalField.name).build();
    ParameterSpec optionType = ParameterSpec.builder(typeField.type, typeField.name).build();
    ParameterSpec description = ParameterSpec.builder(descriptionField.type, descriptionField.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(argumentNameField.type, argumentNameField.name).build();
    MethodSpec.Builder spec = MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", longNameField, longName)
        .addStatement("this.$N = $N", shortNameField, shortName)
        .addStatement("this.$N = $N", positionalField, positional)
        .addStatement("this.$N = $N", typeField, optionType)
        .addStatement("this.$N = $N", descriptionField, description)
        .addStatement("this.$N = $N", argumentNameField, argumentName);

    spec.addParameters(asList(
        longName, shortName, positional, optionType, argumentName, description));
    return spec.build();
  }


  private CodeBlock descExpression(String[] desc) {
    if (desc.length == 0) {
      return CodeBlock.builder().add("$T.emptyList()", Collections.class).build();
    } else if (desc.length == 1) {
      return CodeBlock.builder().add("$T.singletonList($S)", Collections.class, desc[0]).build();
    }
    Object[] args = new Object[1 + desc.length];
    args[0] = Arrays.class;
    System.arraycopy(desc, 0, args, 1, desc.length);
    return CodeBlock.builder()
        .add(String.format("$T.asList($Z%s)",
            String.join(",$Z", nCopies(desc.length, "$S"))), args)
        .build();
  }

  private static MethodSpec shortNameMapMethod(
      ClassName optionType,
      FieldSpec shortNameField) {
    ParameterSpec shortNames = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        TypeName.get(Character.class), optionType), "shortNames")
        .build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>($T.values().length)",
        shortNames.type, shortNames, HashMap.class, optionType);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", optionType, option, optionType);

    builder.beginControlFlow("if ($N.$N != null)", option, shortNameField)
        .addStatement("$N.put($N.$N, $N)", shortNames, option, shortNameField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $N", shortNames);

    return MethodSpec.methodBuilder("shortNameMap")
        .addCode(builder.build())
        .returns(shortNames.type)
        .addModifiers(STATIC)
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
    builder.beginControlFlow("for ($T $N : $T.values())", optionType, option, optionType);

    builder.beginControlFlow("if ($N.$N != null)", option, longNameField)
        .addStatement("$N.put($N.$N, $N)", longNames, option, longNameField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $N", longNames);

    return MethodSpec.methodBuilder("longNameMap")
        .addCode(builder.build())
        .returns(longNames.type)
        .addModifiers(STATIC)
        .build();
  }

  private static String[] getText(Description description) {
    if (description == null) {
      return new String[0];
    }
    return description.value();
  }

  private static MethodSpec describeParamMethod(
      FieldSpec longNameField,
      FieldSpec shortNameField) {

    ParameterSpec argname = ParameterSpec.builder(STRING, "argname").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N == null)", shortNameField)
        .addStatement("return $S + $N + $N", "--", longNameField, argname)
        .endControlFlow();

    builder.beginControlFlow("if ($N == null)", longNameField)
        .addStatement("return $S + $N + $N", "-", shortNameField, argname)
        .endControlFlow();

    builder.addStatement("return $S + $N + $N + $S + $N + $N", "-",
        shortNameField, argname, ", --", longNameField, argname);

    return MethodSpec.methodBuilder("describeParam")
        .addParameter(argname)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec exampleMethod(
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec argumentNameField) {
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N == null)", shortNameField)
        .addStatement("return $T.format($S, $N, $N)",
            String.class, "--%s=<%s>", longNameField, argumentNameField)
        .endControlFlow();

    builder.addStatement("return $T.format($S, $N, $N)",
        String.class, "-%s <%s>", shortNameField, argumentNameField);

    return MethodSpec.methodBuilder("example")
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }
}
