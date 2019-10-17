package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.Param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see Parser
 */
final class Option {

  private final Context context;

  private final MethodSpec describeParamMethod;

  private final MethodSpec exampleMethod;

  private final FieldSpec descriptionField;

  private final FieldSpec argumentNameField;

  private final FieldSpec longNameField;

  private final FieldSpec shortNameField;

  private final FieldSpec bundleKeyField;

  private final FieldSpec positionalIndexField;

  private final MethodSpec shortNameMapMethod;

  private final MethodSpec longNameMapMethod;

  private final MethodSpec parsersMethod;

  private final MethodSpec positionalParsersMethod;

  private Option(
      Context context,
      FieldSpec longNameField,
      FieldSpec shortNameField,
      FieldSpec bundleKeyField,
      FieldSpec positionalIndexField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      MethodSpec exampleMethod,
      MethodSpec shortNameMapMethod,
      MethodSpec longNameMapMethod,
      MethodSpec describeParamMethod,
      MethodSpec parsersMethod,
      MethodSpec positionalParsersMethod) {
    this.positionalIndexField = positionalIndexField;
    this.exampleMethod = exampleMethod;
    this.longNameField = longNameField;
    this.shortNameField = shortNameField;
    this.descriptionField = descriptionField;
    this.argumentNameField = argumentNameField;
    this.bundleKeyField = bundleKeyField;
    this.shortNameMapMethod = shortNameMapMethod;
    this.context = context;
    this.longNameMapMethod = longNameMapMethod;
    this.describeParamMethod = describeParamMethod;
    this.parsersMethod = parsersMethod;
    this.positionalParsersMethod = positionalParsersMethod;
  }

  public static Option create(Context context) {
    FieldSpec longNameField = FieldSpec.builder(STRING, "longName").addModifiers(FINAL).build();
    FieldSpec positionalIndexField = FieldSpec.builder(OptionalInt.class, "positionalIndex").addModifiers(FINAL).build();
    FieldSpec shortNameField = FieldSpec.builder(ClassName.get(Character.class),
        "shortName").addModifiers(FINAL).build();
    FieldSpec bundleKeyField = FieldSpec.builder(STRING, "bundleKey").addModifiers(FINAL).build();
    TypeName parsersType = ParameterizedTypeName.get(ClassName.get(Map.class),
        context.optionType(), context.optionParserType());
    TypeName positionalParsersType = ParameterizedTypeName.get(ClassName.get(List.class),
        context.positionalOptionParserType());
    MethodSpec shortNameMapMethod = shortNameMapMethod(context.optionType(), shortNameField);
    MethodSpec longNameMapMethod = longNameMapMethod(context.optionType(), longNameField);
    MethodSpec parsersMethod = parsersMethod(parsersType, context);
    MethodSpec positionalParsersMethod = positionalParsersMethod(positionalParsersType, context);
    FieldSpec argumentNameField = FieldSpec.builder(
        STRING, "descriptionArgumentName").addModifiers(FINAL).build();
    MethodSpec exampleMethod = exampleMethod(longNameField, shortNameField, argumentNameField);
    FieldSpec descriptionField = FieldSpec.builder(
        LIST_OF_STRING, "description").addModifiers(FINAL).build();

    MethodSpec describeParamMethod = describeParamMethod(
        longNameField,
        shortNameField);

    return new Option(
        context,
        longNameField,
        shortNameField,
        bundleKeyField,
        positionalIndexField,
        descriptionField,
        argumentNameField,
        exampleMethod,
        shortNameMapMethod,
        longNameMapMethod,
        describeParamMethod,
        parsersMethod,
        positionalParsersMethod);
  }

  public TypeSpec define() {
    List<Param> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(context.optionType());
    for (Param param : parameters) {
      String enumConstant = param.enumConstant();
      spec.addEnumConstant(enumConstant, optionEnumConstant(param));
    }
    spec.addModifiers(PRIVATE)
        .addField(longNameField)
        .addField(shortNameField)
        .addField(bundleKeyField)
        .addField(positionalIndexField)
        .addField(argumentNameField)
        .addField(descriptionField)
        .addMethod(positionalMethod())
        .addMethod(describeParamMethod)
        .addMethod(exampleMethod)
        .addMethod(missingRequiredLambdaMethod())
        .addMethod(privateConstructor());
    spec.addMethod(shortNameMapMethod)
        .addMethod(longNameMapMethod)
        .addMethod(parsersMethod);
    spec.addMethod(positionalValuesMethod());
    spec.addMethod(positionalValueMethod());
    spec.addMethod(positionalParsersMethod);
    spec.addMethod(validShortTokenMethod());
    spec.addMethod(describeMethod());
    spec.addMethod(parserMethod());
    spec.addMethod(positionalParserMethod());
    return spec.build();
  }

  private MethodSpec positionalMethod() {
    return MethodSpec.methodBuilder("positional")
        .addStatement("return $N.isPresent()", positionalIndexField)
        .returns(BOOLEAN)
        .build();
  }

  private TypeSpec optionEnumConstant(Param param) {
    List<String> desc = param.description();
    String argumentName = param.descriptionArgumentName();
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("longName", param.longName());
    map.put("shortName", param.shortName() == null ? "null" : "'" + param.shortName() + "'");
    map.put("bundleKey", param.bundleKey().orElse("null"));
    map.put("positionalIndex", param.positionalIndex().isPresent() ?
        CodeBlock.of("$T.of($L)", OptionalInt.class, param.positionalIndex().getAsInt()) :
        CodeBlock.of("$T.empty()", OptionalInt.class));
    map.put("argumentName", argumentName);
    map.put("descExpression", descExpression(desc));
    String format = String.join(", ",
        "$longName:S",
        "$shortName:L",
        "$bundleKey:S",
        "$positionalIndex:L",
        "$argumentName:S",
        "$descExpression:L");

    CodeBlock block = CodeBlock.builder().addNamed(format, map).build();
    TypeSpec.Builder spec = anonymousClassBuilder(block);
    if (param.isPositional()) {
      spec.addMethod(positionalParserMethodOverride(param));
    } else {
      spec.addMethod(parserMethodOverride(param));
    }
    if (param.isFlag()) {
      spec.addMethod(validShortTokenOverride(param));
      spec.addMethod(describeMethodFlagOverride());
    } else if (param.isPositional()) {
      spec.addMethod(describeMethodPositionalOverride());
    } else if (param.isRepeatable()) {
      spec.addMethod(describeMethodRepeatableOverride());
    }
    return spec.build();
  }

  private MethodSpec describeMethodFlagOverride() {
    return MethodSpec.methodBuilder("describe")
        .returns(STRING)
        .addStatement("return describeParam($S)", "")
        .addAnnotation(Override.class)
        .build();
  }

  private MethodSpec describeMethodRepeatableOverride() {
    return MethodSpec.methodBuilder("describe")
        .returns(STRING)
        .addStatement("return describeParam($T.format($S, $N))", String.class, " <%s...>", argumentNameField)
        .build();
  }

  private MethodSpec describeMethodPositionalOverride() {
    return MethodSpec.methodBuilder("describe")
        .returns(STRING)
        .addStatement("return $N", argumentNameField)
        .addAnnotation(Override.class)
        .build();
  }

  private MethodSpec parserMethodOverride(Param param) {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parser")
        .addAnnotation(Override.class)
        .returns(context.optionParserType());
    if (param.isRepeatable()) {
      spec.addStatement("return new $T(this)", context.repeatableOptionParserType());
    } else if (param.isFlag()) {
      spec.addStatement("return new $T(this)", context.flagOptionParserType());
    } else {
      spec.addStatement("return new $T(this)", context.regularOptionParserType());
    }
    return spec.build();
  }

  private MethodSpec positionalParserMethodOverride(Param param) {
    return MethodSpec.methodBuilder("positionalParser")
        .addAnnotation(Override.class)
        .returns(context.positionalOptionParserType())
        .addStatement("return new $T()", param.isRepeatable() ?
            context.repeatablePositionalOptionParserType() :
            context.regularPositionalOptionParserType())
        .build();
  }

  private CodeBlock descExpression(List<String> desc) {
    if (desc.isEmpty()) {
      return CodeBlock.builder().add("$T.emptyList()", Collections.class).build();
    } else if (desc.size() == 1) {
      return CodeBlock.builder().add("$T.singletonList($S)", Collections.class, desc.get(0)).build();
    }
    Object[] args = new Object[1 + desc.size()];
    args[0] = Arrays.class;
    for (int i = 0; i < desc.size(); i++) {
      args[i + 1] = desc.get(i);
    }
    return CodeBlock.builder()
        .add(String.format("$T.asList($Z%s)",
            String.join(",$Z", nCopies(desc.size(), "$S"))), args)
        .build();
  }

  private static MethodSpec shortNameMapMethod(
      ClassName optionType,
      FieldSpec shortNameField) {
    ParameterSpec shortNames = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        TypeName.get(Character.class), optionType), "shortNames")
        .build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    return MethodSpec.methodBuilder("shortNameMap")
        .addCode(nameMapCreationCode(shortNameField, shortNames, option))
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
    return MethodSpec.methodBuilder("longNameMap")
        .addCode(nameMapCreationCode(longNameField, longNames, option))
        .returns(longNames.type)
        .addModifiers(STATIC)
        .build();
  }

  private static CodeBlock nameMapCreationCode(FieldSpec optionField, ParameterSpec resultMap, ParameterSpec option) {

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>($T.values().length)",
        resultMap.type, resultMap, HashMap.class, option.type);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", option.type, option, option.type);

    builder.beginControlFlow("if ($N.$N != null)", option, optionField)
        .addStatement("$N.put($N.$N, $N)", resultMap, option, optionField, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $N", resultMap);

    return builder.build();
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


  private static MethodSpec parsersMethod(
      TypeName parsersType,
      Context context) {
    ParameterSpec parsers = ParameterSpec.builder(parsersType, "parsers")
        .build();
    ParameterSpec option = ParameterSpec.builder(context.optionType(), "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>($T.class)",
        parsers.type, parsers, EnumMap.class, context.optionType());

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", context.optionType(), option, context.optionType());

    builder.beginControlFlow("if (!$N.positional())", option)
        .addStatement("$N.put($N, $N.parser())", parsers, option, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $N", parsers);

    return MethodSpec.methodBuilder("parsers")
        .addCode(builder.build())
        .returns(parsers.type)
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec positionalParsersMethod(
      TypeName positionalParsersType,
      Context context) {
    ParameterSpec parsers = ParameterSpec.builder(positionalParsersType, "parsers")
        .build();
    ParameterSpec option = ParameterSpec.builder(context.optionType(), "option").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = new $T<>()",
        parsers.type, parsers, ArrayList.class);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", context.optionType(), option, context.optionType());

    builder.beginControlFlow("if ($N.positional())", option)
        .addStatement("$N.add($N.positionalParser())", parsers, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();
    builder.addStatement("return $N", parsers);

    return MethodSpec.methodBuilder("positionalParsers")
        .addCode(builder.build())
        .returns(parsers.type)
        .addModifiers(STATIC)
        .build();
  }

  private MethodSpec positionalValuesMethod() {

    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("values");

    spec.beginControlFlow("if (!$N.isPresent())", positionalIndexField)
        .addStatement("return $T.empty()", Stream.class)
        .endControlFlow();

    spec.beginControlFlow("if ($N.getAsInt() >= $N.size())", positionalIndexField, positionalParameter)
        .addStatement("return $T.empty()", Stream.class)
        .endControlFlow();

    spec.addStatement("return $N.subList($N.getAsInt(), $N.size()).stream()", positionalParameter, positionalIndexField, positionalParameter);
    return spec.addParameter(positionalParameter)
        .returns(STREAM_OF_STRING).build();
  }

  private MethodSpec positionalValueMethod() {

    ParameterSpec positionalParameter = ParameterSpec.builder(LIST_OF_STRING, "positional").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("value");

    spec.beginControlFlow("if (!$N.isPresent())", positionalIndexField)
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow();

    spec.beginControlFlow("if ($N.getAsInt() >= $N.size())", positionalIndexField, positionalParameter)
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow();

    spec.addStatement("return $T.of($N.get($N.getAsInt()))",
        Optional.class, positionalParameter, positionalIndexField);

    return spec.addParameter(positionalParameter)
        .returns(optionalOf(STRING)).build();
  }

  private static MethodSpec validShortTokenMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("validShortToken");
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    spec.addParameter(token);
    spec.addStatement("return $N.length() >= 2 && $N.charAt(0) == '-'", token, token);
    spec.returns(BOOLEAN);
    return spec.build();
  }

  private static MethodSpec validShortTokenOverride(Param param) {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("validShortToken");
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    spec.addParameter(token);
    spec.addStatement("return $S.equals($N)", "-" + param.shortName(), token);
    spec.addAnnotation(Override.class);
    spec.returns(BOOLEAN);
    return spec.build();
  }

  private MethodSpec describeMethod() {
    return MethodSpec.methodBuilder("describe")
        .returns(STRING)
        .addStatement("return describeParam($T.format($S, $N))", String.class, " <%s>", argumentNameField)
        .build();
  }

  private MethodSpec parserMethod() {
    return MethodSpec.methodBuilder("parser")
        .returns(context.optionParserType())
        .addStatement("throw new $T()", AssertionError.class)
        .build();
  }

  private MethodSpec positionalParserMethod() {
    return MethodSpec.methodBuilder("positionalParser")
        .returns(context.positionalOptionParserType())
        .addStatement("throw new $T()", AssertionError.class)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec longName = ParameterSpec.builder(longNameField.type, longNameField.name).build();
    ParameterSpec shortName = ParameterSpec.builder(shortNameField.type, shortNameField.name).build();
    ParameterSpec bundleKey = ParameterSpec.builder(bundleKeyField.type, bundleKeyField.name).build();
    ParameterSpec positionalIndex = ParameterSpec.builder(positionalIndexField.type, positionalIndexField.name).build();
    ParameterSpec description = ParameterSpec.builder(descriptionField.type, descriptionField.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(argumentNameField.type, argumentNameField.name).build();
    MethodSpec.Builder spec = MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", longNameField, longName)
        .addStatement("this.$N = $N", shortNameField, shortName)
        .addStatement("this.$N = $N", bundleKeyField, bundleKey)
        .addStatement("this.$N = $N", positionalIndexField, positionalIndex)
        .addStatement("this.$N = $N", descriptionField, description)
        .addStatement("this.$N = $N", argumentNameField, argumentName);

    spec.addParameters(asList(
        longName, shortName, bundleKey, positionalIndex, argumentName, description));
    return spec.build();
  }

  private MethodSpec missingRequiredLambdaMethod() {
    String ifMessage = "Missing parameter: <%s>";
    String elseMessage = "Missing required option: %s (%s)";
    CodeBlock lambda;
    lambda = CodeBlock.of("positionalIndex.isPresent() " +
            "? new $T($T.format($S, this)) " +
            ": new $T($T.format($S, this, describeParam($S)))",
        IllegalArgumentException.class, String.class, ifMessage,
        IllegalArgumentException.class, String.class, elseMessage, "");
    return MethodSpec.methodBuilder("missingRequired")
        .returns(ParameterizedTypeName.get(Supplier.class, IllegalArgumentException.class))
        .addCode("return () -> $L; ", lambda)
        .build();
  }

  MethodSpec shortNameMapMethod() {
    return shortNameMapMethod;
  }

  MethodSpec longNameMapMethod() {
    return longNameMapMethod;
  }

  MethodSpec parsersMethod() {
    return parsersMethod;
  }

  MethodSpec positionalParsersMethod() {
    return positionalParsersMethod;
  }
}
