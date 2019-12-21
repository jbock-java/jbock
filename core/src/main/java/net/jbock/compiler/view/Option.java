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
import net.jbock.compiler.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see GeneratedClass
 */
final class Option {

  private final Context context;

  private final MethodSpec describeParamMethod;

  private final FieldSpec descriptionField;

  private final FieldSpec namesField;

  private final FieldSpec bundleKeyField;

  private final MethodSpec optionNamesMethod;

  private final MethodSpec optionParsersMethod;

  private final FieldSpec shapeField;

  private final MethodSpec paramParsersMethod;

  private Option(
      Context context,
      FieldSpec bundleKeyField,
      FieldSpec descriptionField,
      FieldSpec namesField,
      MethodSpec optionNamesMethod,
      MethodSpec describeParamMethod,
      MethodSpec optionParsersMethod,
      FieldSpec shapeField,
      MethodSpec paramParsersMethod) {
    this.descriptionField = descriptionField;
    this.bundleKeyField = bundleKeyField;
    this.context = context;
    this.optionNamesMethod = optionNamesMethod;
    this.describeParamMethod = describeParamMethod;
    this.namesField = namesField;
    this.optionParsersMethod = optionParsersMethod;
    this.shapeField = shapeField;
    this.paramParsersMethod = paramParsersMethod;
  }

  static Option create(Context context) {
    FieldSpec namesField = FieldSpec.builder(LIST_OF_STRING, "names").addModifiers(FINAL).build();
    FieldSpec bundleKeyField = FieldSpec.builder(STRING, "bundleKey").addModifiers(FINAL).build();
    FieldSpec descriptionField = FieldSpec.builder(LIST_OF_STRING, "description").addModifiers(FINAL).build();
    FieldSpec shapeField = FieldSpec.builder(STRING, "shape").addModifiers(FINAL).build();
    TypeName parsersType = ParameterizedTypeName.get(ClassName.get(Map.class), context.optionType(), context.optionParserType());
    TypeName positionalParsersType = ParameterizedTypeName.get(ClassName.get(List.class), context.paramParserType());
    MethodSpec optionNamesMethod = optionNamesMethod(context.optionType(), namesField);
    MethodSpec parsersMethod = optionParsersMethod(parsersType, context);
    MethodSpec positionalParsersMethod = paramParsersMethod(positionalParsersType, context);

    MethodSpec describeParamMethod = describeParamMethod(namesField);

    return new Option(
        context,
        bundleKeyField,
        descriptionField,
        namesField,
        optionNamesMethod,
        describeParamMethod,
        parsersMethod,
        shapeField,
        positionalParsersMethod);
  }

  TypeSpec define() {
    List<Parameter> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(context.optionType());
    for (Parameter param : parameters) {
      String enumConstant = param.enumConstant();
      spec.addEnumConstant(enumConstant, optionEnumConstant(param));
    }
    return spec.addModifiers(PRIVATE)
        .addField(namesField)
        .addField(bundleKeyField)
        .addField(descriptionField)
        .addField(shapeField)
        .addMethod(describeParamMethod)
        .addMethod(missingRequiredLambdaMethod())
        .addMethod(privateConstructor())
        .addMethod(optionNamesMethod)
        .addMethod(optionParsersMethod)
        .addMethod(paramParsersMethod)
        .build();
  }

  private TypeSpec optionEnumConstant(Parameter param) {
    Map<String, Object> map = new LinkedHashMap<>();
    CodeBlock names = getNames(param);
    map.put("names", names);
    map.put("bundleKey", param.bundleKey().orElse(null));
    map.put("descExpression", descExpression(param.description()));
    map.put("shape", param.shape());
    String format = String.join(", ",
        "$names:L",
        "$bundleKey:S",
        "$descExpression:L",
        "$shape:S");

    CodeBlock block = CodeBlock.builder().addNamed(format, map).build();
    return anonymousClassBuilder(block).build();
  }

  private CodeBlock getNames(Parameter param) {
    List<String> names = param.names();
    switch (names.size()) {
      case 0:
        return CodeBlock.of("$T.emptyList()", Collections.class);
      case 1:
        return CodeBlock.of("$T.singletonList($S)", Collections.class, names.get(0));
      default:
        return arraysOfStringInvocation(names);
    }
  }

  private CodeBlock descExpression(List<String> desc) {
    switch (desc.size()) {
      case 0:
        return CodeBlock.builder().add("$T.emptyList()", Collections.class).build();
      case 1:
        return CodeBlock.builder().add("$T.singletonList($S)", Collections.class, desc.get(0)).build();
      default:
        return arraysOfStringInvocation(desc);
    }
  }

  private CodeBlock arraysOfStringInvocation(List<String> strings) {
    Object[] args = new Object[1 + strings.size()];
    args[0] = Arrays.class;
    for (int i = 0; i < strings.size(); i++) {
      args[i + 1] = strings.get(i);
    }
    return CodeBlock.of(String.format("$T.asList($Z%s)",
        String.join(",$Z", nCopies(strings.size(), "$S"))), args);
  }

  private static MethodSpec optionNamesMethod(
      ClassName optionType,
      FieldSpec namesField) {
    ParameterSpec result = builder(ParameterizedTypeName.get(
        ClassName.get(Map.class), STRING, optionType), "result").build();
    ParameterSpec option = builder(optionType, "option").build();
    ParameterSpec name = builder(STRING, "name").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("optionNames");
    spec.addStatement("$T $N = new $T<>($T.values().length)",
        result.type, result, HashMap.class, option.type);

    // begin iteration over options
    spec.beginControlFlow("for ($T $N : $T.values())", option.type, option, option.type);
    // begin iteration over names
    spec.beginControlFlow("for ($T $N : $N.$N)", STRING, name, option, namesField);

    spec.addStatement("$N.put($N, $N)", result, name, option);

    // end iteration over names
    spec.endControlFlow();
    // end iteration over options
    spec.endControlFlow();

    return spec.returns(result.type)
        .addStatement("return $N", result)
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec describeParamMethod(FieldSpec namesField) {
    ParameterSpec argname = builder(STRING, "argname").build();
    return MethodSpec.methodBuilder("describeParam")
        .addParameter(argname)
        .returns(STRING)
        .beginControlFlow("if (names.size() == 1)", namesField)
        .addStatement("return $S + $N.get(0) + $N",
            "    ", namesField, argname)
        .endControlFlow()
        .addStatement("return $N.get(0) + $S + $N.get(1) + $N",
            namesField, ", ", namesField, argname)
        .build();
  }

  private static MethodSpec optionParsersMethod(
      TypeName parsersType,
      Context context) {
    ParameterSpec parsers = builder(parsersType, "parsers").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("optionParsers")
        .returns(parsers.type)
        .addModifiers(STATIC);
    spec.addStatement("$T $N = new $T<>($T.class)",
        parsers.type, parsers, EnumMap.class, context.optionType());

    for (Parameter param : context.parameters()) {
      if (param.isPositional()) {
        continue;
      }
      if (param.isRepeatable()) {
        spec.addStatement("$N.put($L, new $T($L))",
            parsers, param.enumConstant(), context.optionParserType(), param.enumConstant());
      } else if (param.isFlag()) {
        spec.addStatement("$N.put($L, new $T($L))",
            parsers, param.enumConstant(),
            context.flagParserType(), param.enumConstant());
      } else {
        spec.addStatement("$N.put($L, new $T($L))",
            parsers, param.enumConstant(),
            context.regularOptionParserType(), param.enumConstant());
      }
    }

    return spec.addStatement("return $N", parsers).build();
  }

  private static MethodSpec paramParsersMethod(
      TypeName positionalParsersType,
      Context context) {
    ParameterSpec parsers = builder(positionalParsersType, "parsers").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("paramParsers")
        .returns(parsers.type)
        .addModifiers(STATIC)
        .addStatement("$T $N = new $T<>()", parsers.type, parsers, ArrayList.class);
    for (Parameter param : context.parameters()) {
      if (!param.isPositional()) {
        continue;
      }
      spec.addStatement("$N.add(new $T())", parsers, param.isRepeatable() ?
          context.paramParserType() :
          context.regularParamParserType());
    }
    return spec.addStatement("return $N", parsers).build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec names = builder(namesField.type, namesField.name).build();
    ParameterSpec bundleKey = builder(bundleKeyField.type, bundleKeyField.name).build();
    ParameterSpec description = builder(descriptionField.type, descriptionField.name).build();
    ParameterSpec shape = builder(shapeField.type, shapeField.name).build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", namesField, names)
        .addStatement("this.$N = $N", bundleKeyField, bundleKey)
        .addStatement("this.$N = $N", descriptionField, description)
        .addStatement("this.$N = $N", shapeField, shape)
        .addParameters(asList(names, bundleKey, description, shape))
        .build();
  }

  private MethodSpec missingRequiredLambdaMethod() {
    CodeBlock lambda = CodeBlock.of("new $T($S + (names.isEmpty() ? name() : " +
            "$T.format($S, name(), $T.join($S, names))))",
        IllegalArgumentException.class,
        "Missing required: ",
        String.class,
        "%s (%s)", String.class, ", ");
    return MethodSpec.methodBuilder("missingRequired")
        .returns(ParameterizedTypeName.get(Supplier.class, IllegalArgumentException.class))
        .addCode("return () -> $L;\n", lambda)
        .build();
  }

  MethodSpec optionNamesMethod() {
    return optionNamesMethod;
  }

  MethodSpec optionParsersMethod() {
    return optionParsersMethod;
  }

  MethodSpec paramParsersMethod() {
    return paramParsersMethod;
  }
}
