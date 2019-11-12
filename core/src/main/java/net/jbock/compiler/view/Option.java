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
import java.util.StringJoiner;
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
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

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

  private final FieldSpec namesField;

  private final FieldSpec bundleKeyField;

  private final FieldSpec positionalIndexField;

  private final MethodSpec optionNamesMethod;

  private final MethodSpec parsersMethod;

  private final MethodSpec positionalParsersMethod;

  private Option(
      Context context,
      FieldSpec bundleKeyField,
      FieldSpec positionalIndexField,
      FieldSpec descriptionField,
      FieldSpec argumentNameField,
      FieldSpec namesField,
      MethodSpec exampleMethod,
      MethodSpec optionNamesMethod,
      MethodSpec describeParamMethod,
      MethodSpec parsersMethod,
      MethodSpec positionalParsersMethod) {
    this.positionalIndexField = positionalIndexField;
    this.exampleMethod = exampleMethod;
    this.descriptionField = descriptionField;
    this.argumentNameField = argumentNameField;
    this.bundleKeyField = bundleKeyField;
    this.context = context;
    this.optionNamesMethod = optionNamesMethod;
    this.describeParamMethod = describeParamMethod;
    this.namesField = namesField;
    this.parsersMethod = parsersMethod;
    this.positionalParsersMethod = positionalParsersMethod;
  }

  static Option create(Context context) {
    FieldSpec namesField = FieldSpec.builder(LIST_OF_STRING, "names").addModifiers(FINAL).build();
    FieldSpec positionalIndexField = FieldSpec.builder(OptionalInt.class, "positionalIndex").addModifiers(FINAL).build();
    FieldSpec bundleKeyField = FieldSpec.builder(STRING, "bundleKey").addModifiers(FINAL).build();
    TypeName parsersType = ParameterizedTypeName.get(ClassName.get(Map.class), context.optionType(), context.optionParserType());
    TypeName positionalParsersType = ParameterizedTypeName.get(ClassName.get(List.class), context.positionalOptionParserType());
    MethodSpec optionNamesMethod = optionNamesMethod(context.optionType(), namesField);
    MethodSpec parsersMethod = parsersMethod(parsersType, context);
    MethodSpec positionalParsersMethod = positionalParsersMethod(positionalParsersType, context);
    FieldSpec argumentNameField = FieldSpec.builder(STRING, "descriptionArgumentName").addModifiers(FINAL).build();
    MethodSpec exampleMethod = exampleMethod(namesField, argumentNameField);
    FieldSpec descriptionField = FieldSpec.builder(LIST_OF_STRING, "description").addModifiers(FINAL).build();

    MethodSpec describeParamMethod = describeParamMethod(namesField);

    return new Option(
        context,
        bundleKeyField,
        positionalIndexField,
        descriptionField,
        argumentNameField,
        namesField,
        exampleMethod,
        optionNamesMethod,
        describeParamMethod,
        parsersMethod,
        positionalParsersMethod);
  }

  TypeSpec define() {
    List<Param> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(context.optionType());
    for (Param param : parameters) {
      String enumConstant = param.enumConstant();
      spec.addEnumConstant(enumConstant, optionEnumConstant(param));
    }
    return spec.addModifiers(PRIVATE)
        .addField(namesField)
        .addField(bundleKeyField)
        .addField(positionalIndexField)
        .addField(argumentNameField)
        .addField(descriptionField)
        .addMethod(positionalMethod())
        .addMethod(describeParamMethod)
        .addMethod(exampleMethod)
        .addMethod(missingRequiredLambdaMethod())
        .addMethod(privateConstructor())
        .addMethod(optionNamesMethod)
        .addMethod(parsersMethod)
        .addMethod(positionalValuesMethod())
        .addMethod(positionalValueMethod())
        .addMethod(positionalParsersMethod)
        .addMethod(validShortTokenMethod())
        .addMethod(describeMethod())
        .addMethod(parserMethod())
        .addMethod(positionalParserMethod())
        .build();
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
    CodeBlock names = getNames(param);
    map.put("names", names);
    map.put("bundleKey", param.bundleKey().orElse(null));
    map.put("positionalIndex", param.positionalIndex().isPresent() ?
        CodeBlock.of("$T.of($L)", OptionalInt.class, param.positionalIndex().getAsInt()) :
        CodeBlock.of("$T.empty()", OptionalInt.class));
    map.put("argumentName", argumentName);
    map.put("descExpression", descExpression(desc));
    String format = String.join(", ",
        "$names:L",
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
      validShortTokenMethodOverrideFlag(param).ifPresent(spec::addMethod);
      spec.addMethod(describeMethodOverrideFlag());
    } else if (param.isPositional()) {
      spec.addMethod(describeMethodPositionalOverride());
    } else if (param.isRepeatable()) {
      spec.addMethod(describeMethodRepeatableOverride());
    }
    return spec.build();
  }

  private CodeBlock getNames(Param param) {
    if (param.longName().isPresent() && !param.shortName().isPresent()) {
      return CodeBlock.of("$T.singletonList($S)", Collections.class, param.longName().get());
    } else if (!param.longName().isPresent() && param.shortName().isPresent()) {
      return CodeBlock.of("$T.singletonList($S)", Collections.class, param.shortName().get());
    } else if (!param.longName().isPresent()) {
      return CodeBlock.of("$T.emptyList()", Collections.class);
    } else {
      return CodeBlock.of("$T.asList($S, $S)", Arrays.class, param.shortName().get(), param.longName().get());
    }
  }

  private MethodSpec describeMethodOverrideFlag() {
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

  private static MethodSpec optionNamesMethod(
      ClassName optionType,
      FieldSpec namesField) {
    ParameterSpec optionNames = ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Map.class), STRING, optionType), "optionNames").build();
    ParameterSpec option = ParameterSpec.builder(optionType, "option").build();
    ParameterSpec name = ParameterSpec.builder(STRING, "name").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("longNameMap");
    spec.addStatement("$T $N = new $T<>($T.values().length)",
        optionNames.type, optionNames, HashMap.class, option.type);

    // begin iteration over options
    spec.beginControlFlow("for ($T $N : $T.values())", option.type, option, option.type);
    // begin iteration over names
    spec.beginControlFlow("for ($T $N : $N.$N)", STRING, name, option, namesField);

    spec.addStatement("$N.put($N, $N)", optionNames, name, option);

    // end iteration over names
    spec.endControlFlow();
    // end iteration over options
    spec.endControlFlow();

    return spec.returns(optionNames.type)
        .addStatement("return $N", optionNames)
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec describeParamMethod(FieldSpec namesField) {

    ParameterSpec argname = ParameterSpec.builder(STRING, "argname").build();

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();
    ParameterSpec name = ParameterSpec.builder(STRING, "name").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("describeParam")
        .addStatement("$T $N = new $T($S)", joiner.type, joiner, joiner.type, ", ");

    spec.beginControlFlow("for ($T $N : $N)", STRING, name, namesField)
        .addStatement("$N.add($N + $N)", joiner, name, argname)
        .endControlFlow();

    return spec.addParameter(argname)
        .returns(STRING)
        .addStatement("return $N.toString()", joiner)
        .build();
  }

  private static MethodSpec exampleMethod(
      FieldSpec namesField,
      FieldSpec argumentNameField) {

    return MethodSpec.methodBuilder("example")
        .returns(STRING)
        .addStatement("return $T.format($S, $N.get(0), $N)",
            String.class, "%s <%s>", namesField, argumentNameField)
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
        .returns(OPTIONAL_STRING).build();
  }

  private static MethodSpec validShortTokenMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("validShortToken");
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    spec.addParameter(token);
    spec.addStatement("return $N.length() >= 2 && $N.charAt(0) == '-'", token, token);
    spec.returns(BOOLEAN);
    return spec.build();
  }

  private static Optional<MethodSpec> validShortTokenMethodOverrideFlag(Param param) {
    return param.shortName().map(shortName -> {
      ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
      return MethodSpec.methodBuilder("validShortToken")
          .addParameter(token)
          .addStatement("return $S.equals($N)", shortName, token)
          .addAnnotation(Override.class)
          .returns(BOOLEAN)
          .build();
    });
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
    ParameterSpec names = ParameterSpec.builder(namesField.type, namesField.name).build();
    ParameterSpec bundleKey = ParameterSpec.builder(bundleKeyField.type, bundleKeyField.name).build();
    ParameterSpec positionalIndex = ParameterSpec.builder(positionalIndexField.type, positionalIndexField.name).build();
    ParameterSpec description = ParameterSpec.builder(descriptionField.type, descriptionField.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(argumentNameField.type, argumentNameField.name).build();
    MethodSpec.Builder spec = MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", namesField, names)
        .addStatement("this.$N = $N", bundleKeyField, bundleKey)
        .addStatement("this.$N = $N", positionalIndexField, positionalIndex)
        .addStatement("this.$N = $N", descriptionField, description)
        .addStatement("this.$N = $N", argumentNameField, argumentName);

    spec.addParameters(asList(
        names, bundleKey, positionalIndex, argumentName, description));
    return spec.build();
  }

  private MethodSpec missingRequiredLambdaMethod() {
    String ifMessage = "Missing parameter: <%s>";
    String elseMessage = "Missing required option: %s (%s)";
    CodeBlock lambda;
    lambda = CodeBlock.builder()
        .add("positionalIndex.isPresent()\n").indent()
        .add("? new $T($T.format($S, this))\n", IllegalArgumentException.class, String.class, ifMessage)
        .add(": new $T($T.format($S, this, describeParam($S)))", IllegalArgumentException.class, String.class, elseMessage, "")
        .unindent().build();
    return MethodSpec.methodBuilder("missingRequired")
        .returns(ParameterizedTypeName.get(Supplier.class, IllegalArgumentException.class))
        .addCode("return () -> $L;\n", lambda)
        .build();
  }

  MethodSpec optionNamesMethod() {
    return optionNamesMethod;
  }

  MethodSpec parsersMethod() {
    return parsersMethod;
  }

  MethodSpec positionalParsersMethod() {
    return positionalParsersMethod;
  }
}
