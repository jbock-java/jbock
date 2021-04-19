package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.listOf;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see GeneratedClass
 */
final class OptionEnum {

  private final Context context;

  private final GeneratedTypes generatedTypes;

  private final FieldSpec descriptionField;

  private final FieldSpec namesField;

  private final FieldSpec bundleKeyField;

  private final MethodSpec optionNamesMethod;

  private final MethodSpec optionParsersMethod;

  private final MethodSpec paramParsersMethod;

  private final FieldSpec shapeField;

  @Inject
  OptionEnum(Context context, GeneratedTypes generatedTypes) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    FieldSpec namesField = FieldSpec.builder(LIST_OF_STRING, "names").build();
    FieldSpec bundleKeyField = FieldSpec.builder(STRING, "bundleKey").build();
    FieldSpec descriptionField = FieldSpec.builder(LIST_OF_STRING, "description").build();
    FieldSpec shapeField = FieldSpec.builder(STRING, "shape").build();
    MethodSpec optionNamesMethod = optionNamesMethod(generatedTypes.optionType(), namesField);
    MethodSpec optionParsersMethod = optionParsersMethod(context, generatedTypes);
    MethodSpec paramParsersMethod = paramParsersMethod(context, generatedTypes);
    this.bundleKeyField = bundleKeyField;
    this.descriptionField = descriptionField;
    this.namesField = namesField;
    this.optionNamesMethod = optionNamesMethod;
    this.optionParsersMethod = optionParsersMethod;
    this.shapeField = shapeField;
    this.paramParsersMethod = paramParsersMethod;
  }

  TypeSpec define() {
    List<Parameter> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(generatedTypes.optionType());
    for (Parameter param : parameters) {
      String enumConstant = param.enumConstant();
      spec.addEnumConstant(enumConstant, optionEnumConstant(param));
    }
    return spec.addModifiers(PRIVATE)
        .addField(namesField)
        .addField(bundleKeyField)
        .addField(descriptionField)
        .addField(shapeField)
        .addMethod(missingRequiredMethod())
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
    map.put("shape", param.sample());
    String format = String.join(", ", "$names:L", "$bundleKey:S", "$descExpression:L", "$shape:S");

    return anonymousClassBuilder(CodeBlock.builder().addNamed(format, map).build()).build();
  }

  private CodeBlock getNames(Parameter param) {
    List<String> names = param.dashedNames();
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
        String.join(",$W", nCopies(strings.size(), "$S"))), args);
  }

  private static MethodSpec optionNamesMethod(ClassName optionType, FieldSpec namesField) {
    ParameterSpec result = builder(mapOf(STRING, optionType), "result").build();
    ParameterSpec option = builder(optionType, "option").build();
    ParameterSpec name = builder(STRING, "name").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>($T.values().length)",
        result.type, result, HashMap.class, option.type);

    code.add("for ($T $N : $T.values())\n", option.type, option, option.type).indent()
        .addStatement("$N.$N.forEach($N -> $N.put($N, $N))", option, namesField, name, result, name, option)
        .unindent();
    code.addStatement("return $N", result);

    return MethodSpec.methodBuilder("optionNames").returns(result.type)
        .addCode(code.build())
        .addModifiers(STATIC)
        .build();
  }

  private static MethodSpec optionParsersMethod(Context context, GeneratedTypes generatedTypes) {
    ParameterSpec parsers = builder(mapOf(generatedTypes.optionType(), generatedTypes.optionParserType()), "parsers").build();

    return MethodSpec.methodBuilder("optionParsers").returns(parsers.type)
        .addCode(optionParsersMethodCode(context, generatedTypes, parsers))
        .addModifiers(STATIC).build();
  }

  private static CodeBlock optionParsersMethodCode(Context context, GeneratedTypes generatedTypes, ParameterSpec parsers) {
    List<NamedOption> options = context.options();
    if (options.isEmpty()) {
      return CodeBlock.builder().addStatement("return $T.emptyMap()", Collections.class).build();
    }
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>($T.class)", parsers.type, parsers, EnumMap.class, generatedTypes.optionType());
    for (Parameter param : options) {
      code.addStatement("$N.put($L, new $T())", parsers, param.enumConstant(), optionParserType(generatedTypes, param));
    }
    code.addStatement("return $N", parsers);
    return code.build();
  }

  private static ClassName optionParserType(GeneratedTypes generatedTypes, Parameter param) {
    if (param.isRepeatable()) {
      return generatedTypes.repeatableOptionParserType();
    }
    if (param.isFlag()) {
      return generatedTypes.flagParserType();
    }
    return generatedTypes.regularOptionParserType();
  }

  private static MethodSpec paramParsersMethod(Context context, GeneratedTypes generatedTypes) {
    ParameterSpec parsers = builder(listOf(generatedTypes.paramParserType()), "parsers").build();
    CodeBlock code = paramParsersMethodCode(context, generatedTypes);
    return MethodSpec.methodBuilder("paramParsers")
        .returns(parsers.type)
        .addModifiers(STATIC)
        .addStatement(code)
        .build();
  }

  private static CodeBlock paramParsersMethodCode(Context context, GeneratedTypes generatedTypes) {
    List<PositionalParameter> params = context.params();
    if (params.isEmpty()) {
      return CodeBlock.of("return $T.emptyList()", Collections.class);
    }
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("return $T.asList(", Arrays.class);
    for (int i = 0; i < params.size(); i++) {
      Parameter param = params.get(i);
      code.add("new $T()", param.isRepeatable() ? generatedTypes.repeatableParamParserType() : generatedTypes.regularParamParserType());
      if (i < params.size() - 1) {
        code.add(",$W");
      }
    }
    return code.add(")").build();
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

  private MethodSpec missingRequiredMethod() {
    CodeBlock.Builder code = CodeBlock.builder()
        .add("return new $T($S + name() +\n", RuntimeException.class, "Missing required: ").indent()
        .addStatement("(names.isEmpty() ? $S : $S + $T.join($S, names) + $S))", "", " (", String.class, ", ", ")").unindent();
    return MethodSpec.methodBuilder("missingRequired")
        .returns(RuntimeException.class)
        .addCode(code.build())
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
