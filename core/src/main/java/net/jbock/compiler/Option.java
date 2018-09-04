package net.jbock.compiler;

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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
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
  final MethodSpec printUsageMethod;
  final MethodSpec synopsisMethod;

  private final MethodSpec exampleMethod;
  private final FieldSpec descriptionField;

  private final MethodSpec spacesMethod;

  private final FieldSpec argumentNameField;

  private final MethodSpec describeNamesMethod;
  private final MethodSpec descriptionBlockMethod;

  private final FieldSpec longNameField;

  private final FieldSpec shortNameField;

  final FieldSpec typeField;

  final MethodSpec shortNameMapMethod;
  final MethodSpec longNameMapMethod;

  private Option(
      Context context,
      ClassName type,
      OptionType optionType,
      MethodSpec printUsageMethod,
      MethodSpec synopsisMethod,
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec typeField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      MethodSpec spacesMethod,
      MethodSpec exampleMethod,
      MethodSpec shortNameMapMethod,
      MethodSpec longNameMapMethod,
      MethodSpec describeParamMethod,
      MethodSpec describeNamesMethod,
      MethodSpec descriptionBlockMethod) {
    this.printUsageMethod = printUsageMethod;
    this.synopsisMethod = synopsisMethod;
    this.spacesMethod = spacesMethod;
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
    this.describeNamesMethod = describeNamesMethod;
    this.descriptionBlockMethod = descriptionBlockMethod;
  }

  static Option create(Context context, OptionType optionType) {
    FieldSpec typeField = FieldSpec.builder(optionType.type, "type").build();
    FieldSpec longNameField = FieldSpec.builder(STRING, "longName").build();
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
    MethodSpec spacesMethod = spacesMethod();
    MethodSpec synopsisMethod = synopsisMethod(context, exampleMethod);
    MethodSpec printUsageMethod = printUsageMethod(
        context,
        type,
        spacesMethod,
        synopsisMethod,
        optionType,
        typeField);

    MethodSpec describeParamMethod = describeParamMethod(
        longNameField,
        shortNameField);
    MethodSpec describeNamesMethod = describeNamesMethod(
        context,
        describeParamMethod,
        typeField,
        argumentNameField,
        optionType);
    MethodSpec descriptionBlockMethod = descriptionBlockMethod(spacesMethod, descriptionField);

    return new Option(
        context,
        type,
        optionType,
        printUsageMethod,
        synopsisMethod,
        longNameField,
        shortNameField,
        typeField,
        descriptionField,
        argumentNameField,
        spacesMethod,
        exampleMethod,
        shortNameMapMethod,
        longNameMapMethod,
        describeParamMethod,
        describeNamesMethod,
        descriptionBlockMethod);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Param param : context.parameters) {
      String[] desc = getText(param.description());
      String argumentName = param.descriptionArgumentName();
      String enumConstant = param.enumConstant();
      String format = String.format("$S, $S, $T.$L, $S, new $T[]{$Z%s}",
          String.join(",$Z", Collections.nCopies(desc.length, "$S")));
      List<Comparable<? extends Comparable<?>>> fixArgs =
          Arrays.asList(param.longName(), param.shortName(), optionType.type,
              param.positionalType != null ? param.positionalType : param.paramType,
              argumentName, STRING);
      List<Object> args = new ArrayList<>(fixArgs.size() + desc.length);
      args.addAll(fixArgs);
      args.addAll(Arrays.asList(desc));
      builder.addEnumConstant(enumConstant, anonymousClassBuilder(format,
          args.toArray()).build());
    }
    builder.addModifiers(PRIVATE)
        .addFields(Arrays.asList(longNameField, shortNameField, typeField, argumentNameField, descriptionField))
        .addMethod(describeMethod())
        .addMethod(spacesMethod)
        .addMethod(describeNamesMethod)
        .addMethod(describeParamMethod)
        .addMethod(descriptionBlockMethod)
        .addMethod(exampleMethod)
        .addMethod(printUsageMethod)
        .addMethod(synopsisMethod)
        .addMethod(privateConstructor());
    if (!context.nonpositionalParamTypes.isEmpty()) {
      builder.addMethod(shortNameMapMethod)
          .addMethod(longNameMapMethod);
    }
    return builder.build();
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
    builder.beginControlFlow("for ($T $N : $T.values())", optionType, option, optionType);

    builder.beginControlFlow("if ($N.$N != null)", option, shortNameField)
        .addStatement("$N.put($N.$N.toString(), $N)", shortNames, option, shortNameField, option)
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

  private static MethodSpec descriptionBlockMethod(
      MethodSpec spacesMethod,
      FieldSpec descriptionField) {
    ParameterSpec line = ParameterSpec.builder(STRING, "line").build();
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    ParameterSpec spaces = ParameterSpec.builder(STRING, "spaces").build();
    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N($N)", STRING, spaces, spacesMethod, indent);
    builder.addStatement("$T $N = new $T($T.lineSeparator() + $N, $N, $S)", joiner.type, joiner, joiner.type,
        System.class, spaces, spaces, "");
    builder.beginControlFlow("for ($T $N : $N)", STRING, line, descriptionField)
        .addStatement("$N.add($N)", joiner, line)
        .endControlFlow();
    builder.addStatement("return $N.toString()", joiner);
    return MethodSpec.methodBuilder("descriptionBlock")
        .addParameter(indent)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }

  private MethodSpec describeMethod() {
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("describe");
    ParameterSpec spaces = ParameterSpec.builder(STRING, "spaces").build();
    builder.addStatement("$T $N = spaces($N)", STRING, spaces, indent);

    builder.beginControlFlow("if ($N.isEmpty())", descriptionField)
        .addStatement("return $N + $N()", spaces, describeNamesMethod)
        .endControlFlow();

    builder.addStatement("return $N + $N() + $T.lineSeparator() + $N($L * $N)",
        spaces, describeNamesMethod, System.class, descriptionBlockMethod, 2, indent);
    return builder
        .returns(STRING)
        .addParameter(indent)
        .build();
  }

  private static MethodSpec describeNamesMethod(
      Context context,
      MethodSpec describeParamMethod,
      FieldSpec optionTypeField,
      FieldSpec argumentNameField,
      OptionType optionType) {
    CodeBlock.Builder builder = CodeBlock.builder();
    if (context.nonpositionalParamTypes.contains(Type.FLAG)) {
      builder.beginControlFlow("if ($N == $T.$L)",
          optionTypeField, optionType.type, Type.FLAG)
          .addStatement("return $N($S)", describeParamMethod, "")
          .endControlFlow();
    }
    builder.beginControlFlow("if ($N.$N)",
        optionTypeField, optionType.isPositionalField)
        .addStatement("return $N", argumentNameField)
        .endControlFlow();

    builder.addStatement("return $N($S + $N + $S)",
        describeParamMethod, " <", argumentNameField, ">");
    return MethodSpec.methodBuilder("describeNames")
        .returns(STRING)
        .addCode(builder.build())
        .build();
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

  private static MethodSpec printUsageMethod(
      Context context,
      ClassName type,
      MethodSpec spacesMethod,
      MethodSpec synopsisMethod,
      OptionType optionType,
      FieldSpec typeField) {
    ParameterSpec optionParam = ParameterSpec.builder(type, "option").build();
    ParameterSpec out = ParameterSpec.builder(ClassName.get(PrintStream.class), "out").build();
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("printUsage");

    builder.addStatement("$N.println($S)", out, "NAME");
    builder.addStatement("$N.print($N($N))", out, spacesMethod, indent);
    builder.addStatement("$N.print($S)", out, optionType.context.programName);
    if (!optionType.context.missionStatement.isEmpty()) {
      builder.addStatement("$N.print($S)", out, " - ");
      builder.addStatement("$N.println($S)", out, optionType.context.missionStatement);
      builder.addStatement("$N.println()", out);
    } else {
      builder.addStatement("$N.println()", out);
      builder.addStatement("$N.println()", out);
    }

    builder.addStatement("$N.println($S)", out, "SYNOPSIS");
    builder.addStatement("$N.print($N($N))", out, spacesMethod, indent);
    builder.addStatement("$N.println($N())", out, synopsisMethod);
    builder.addStatement("$N.println()", out);

    builder.addStatement("$N.println($S)", out, "DESCRIPTION");

    for (String line : optionType.context.overview) {
      if (line.isEmpty()) {
        builder.addStatement("$N.println()", out);
      } else {
        builder.addStatement("$N.println($N($N) + $S)", out, spacesMethod, indent, line);
      }
    }

    builder.addStatement("$N.println()", out);
    if (!context.positionalParamTypes.isEmpty()) {
      builder.beginControlFlow("for ($T $N: $T.values())",
          optionParam.type, optionParam, optionParam.type)
          .addCode(printUsagePositionalLoopCode(optionParam, out, indent, optionType, typeField))
          .endControlFlow();
    }

    if (!context.nonpositionalParamTypes.isEmpty()) {
      builder.addStatement("$N.println($S)", out, "OPTIONS");
      builder.beginControlFlow("for ($T $N: $T.values())",
          optionParam.type, optionParam, optionParam.type)
          .addCode(printUsageNonpositionalLoopCode(optionParam, out, indent, optionType, typeField))
          .endControlFlow();
    }


    return builder
        .addModifiers(STATIC)
        .addParameters(Arrays.asList(out, indent))
        .build();
  }

  private static MethodSpec synopsisMethod(
      Context context,
      MethodSpec exampleMethod) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("synopsis")
        .returns(STRING)
        .addModifiers(STATIC);

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    builder.addStatement("$T $N = new $T($S)",
        StringJoiner.class, joiner, StringJoiner.class, " ");

    Map<Boolean, List<Param>> partitionedNonpos = context.parameters.stream()
        .filter(p -> p.positionalType == null)
        .collect(partitioningBy(p -> p.paramType.required));

    List<Param> requiredNonpos = partitionedNonpos.get(true);
    List<Param> optionalNonpos = partitionedNonpos.get(false);

    List<Param> positional = context.parameters.stream()
        .filter(p -> p.positionalType != null)
        .collect(toList());

    builder.addStatement("$N.add($S)", joiner, context.programName);

    if (!optionalNonpos.isEmpty()) {
      builder.addStatement("$N.add($S)", joiner, "[<options>]");
    }

    for (Param param : requiredNonpos) {
      builder.addStatement("$N.add($L.$N())", joiner,
          param.enumConstant(), exampleMethod);
    }

    for (Param param : positional) {
      switch (param.positionalType.order) {
        case REQUIRED:
          builder.addStatement("$N.add($S + $S + $S)", joiner, "<",
              param.descriptionArgumentName(), ">");
          break;
        case OPTIONAL:
          builder.addStatement("$N.add($S + $S + $S)", joiner, "[<",
              param.descriptionArgumentName(), ">]");
          break;
        case LIST:
          builder.addStatement("$N.add($S + $S + $S)", joiner, context.allowEscape() ? "[[--] <" : "[<",
              param.descriptionArgumentName(), ">]");
          break;
        default:
          throw new AssertionError();
      }
    }

    builder.addStatement("return $N.toString()", joiner);

    return builder.build();
  }

  private static CodeBlock printUsagePositionalLoopCode(
      ParameterSpec optionParam,
      ParameterSpec out,
      ParameterSpec indent,
      OptionType optionType,
      FieldSpec typeField) {
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N.$N.$N)",
        optionParam, typeField, optionType.isPositionalField)
        .addStatement("$N.println($N)", out, optionParam)
        .addStatement("$N.println($N.descriptionBlock($N))", out, optionParam, indent)
        .addStatement("$N.println()", out)
        .endControlFlow();

    return builder.build();
  }

  private static CodeBlock printUsageNonpositionalLoopCode(
      ParameterSpec optionParam,
      ParameterSpec out,
      ParameterSpec indent,
      OptionType optionType,
      FieldSpec typeField) {
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if (!$N.$N.$N)",
        optionParam, typeField, optionType.isPositionalField)
        .addStatement("$N.println($N.describe($N))", out, optionParam, indent)
        .addStatement("$N.println()", out)
        .endControlFlow();

    return builder.build();
  }

  private static MethodSpec spacesMethod() {
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    ParameterSpec sp = ParameterSpec.builder(ArrayTypeName.of(TypeName.CHAR), "sp").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T[$N]", sp.type, sp, TypeName.CHAR, indent);
    builder.addStatement("$T.fill($N, ' ')", Arrays.class, sp);
    builder.addStatement("return new $T($N)", STRING, sp);
    return MethodSpec.methodBuilder("spaces")
        .addParameter(indent)
        .addModifiers(STATIC)
        .returns(STRING)
        .addCode(builder.build())
        .build();
  }
}
