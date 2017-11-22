package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;
import static net.jbock.compiler.Analyser.STRING;
import static net.jbock.compiler.OptionType.EVERYTHING_AFTER;
import static net.jbock.compiler.OptionType.IS_BINDING;
import static net.jbock.compiler.OptionType.IS_SPECIAL;
import static net.jbock.compiler.OptionType.OTHER_TOKENS;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.jbock.Description;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Processor.Context;

final class Option {

  private static final FieldSpec DESCRIPTION = FieldSpec.builder(
      Analyser.STRING_LIST, "description", PRIVATE, FINAL).build();

  private static final FieldSpec ARGUMENT_NAME = FieldSpec.builder(
      Analyser.STRING, "descriptionArgumentName", PRIVATE, FINAL).build();

  final ClassName optionClass;

  private final Context context;
  final ClassName optionTypeClass;
  private final boolean needsSuffix;

  final FieldSpec optionType;
  private final MethodSpec describeNamesMethod;
  private final MethodSpec describeParamMethod;
  private final MethodSpec descriptionBlockMethod;

  private Option(Context context, ClassName optionClass, ClassName optionTypeClass, FieldSpec optionType) {
    this.context = context;
    this.optionClass = optionClass;
    this.optionTypeClass = optionTypeClass;
    this.optionType = optionType;
    this.describeParamMethod = describeParamMethod(context, optionType, optionTypeClass);
    this.describeNamesMethod = describeNamesMethod(describeParamMethod, optionType, optionTypeClass);
    this.descriptionBlockMethod = descriptionBlockMethod();
    Set<String> uppercaseArgumentNames = IntStream.range(0, context.parameters.size())
        .mapToObj(this::enumConstant)
        .collect(Collectors.toSet());
    this.needsSuffix = uppercaseArgumentNames.size() < context.parameters.size();
  }

  static Option create(Context context, ClassName optionClass, ClassName optionTypeClass, FieldSpec optionType) {
    return new Option(context, optionClass, optionTypeClass, optionType);
  }

  String enumConstant(int i) {
    String suffix = needsSuffix ? String.format("_%d", i) : "";
    return upcase(context.parameters.get(i).parameterName()) + suffix;
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(optionClass);
    for (int i = 0; i < context.parameters.size(); i++) {
      Param param = context.parameters.get(i);
      String[] desc = getText(param.description());
      String argumentName = Processor.ARGNAME_LESS.contains(param.optionType()) ?
          null :
          param.description() == null ? "VAL" : param.description().argumentName();
      String enumConstant = enumConstant(i);
      String format = String.format("$S, $S, $T.$L, $S, new $T[] {\n    %s}",
          String.join(",\n    ", Collections.nCopies(desc.length, "$S")));
      List<Comparable<? extends Comparable<?>>> fixArgs =
          Arrays.asList(param.longName(), param.shortName(), optionTypeClass, param.optionType(), argumentName, STRING);
      List<Object> args = new ArrayList<>(fixArgs.size() + desc.length);
      args.addAll(fixArgs);
      args.addAll(Arrays.asList(desc));
      builder.addEnumConstant(enumConstant, anonymousClassBuilder(format,
          args.toArray()).build());
    }
    return builder.addModifiers(PUBLIC)
        .addFields(Arrays.asList(LONG_NAME, SHORT_NAME, optionType, ARGUMENT_NAME, DESCRIPTION))
        .addMethod(describeMethod())
        .addMethod(toStringMethod())
        .addMethod(describeNamesMethod)
        .addMethod(describeParamMethod)
        .addMethod(descriptionBlockMethod)
        .addMethod(shortNameMethod())
        .addMethod(longNameMethod())
        .addMethod(descriptionMethod())
        .addMethod(descriptionParameterMethod())
        .addMethod(typeMethod())
        .addMethod(isSpecialMethod())
        .addMethod(isBindingMethod())
        .addMethod(privateConstructor())
        .build();
  }

  MethodSpec printUsageMethod() {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec out = ParameterSpec.builder(ClassName.get(PrintStream.class), "out").build();
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    return MethodSpec.methodBuilder("printUsage")
        .beginControlFlow("for ($T $N: $T.values())", option.type, option, optionClass)
        .addStatement("$N.println($N.describe($N))", out, option, indent)
        .endControlFlow()
        .addModifiers(STATIC, PUBLIC)
        .addParameters(Arrays.asList(out, indent))
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec longName = ParameterSpec.builder(LONG_NAME.type, LONG_NAME.name).build();
    ParameterSpec shortName = ParameterSpec.builder(STRING, SHORT_NAME.name).build();
    ParameterSpec optionType = ParameterSpec.builder(this.optionType.type, this.optionType.name).build();
    ParameterSpec description = ParameterSpec.builder(ArrayTypeName.of(STRING), DESCRIPTION.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(ARGUMENT_NAME.type, ARGUMENT_NAME.name).build();
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    builder
        .beginControlFlow("if (!$N.$N())", optionType, IS_SPECIAL)
        .addStatement("assert $N == null || $N.length() == 1", shortName, shortName)
        .addStatement("assert $N == null || !$N.isEmpty()", longName, longName)
        .addStatement("assert $N != null || $N != null", longName, shortName)
        .addStatement("assert $N != null", description)
        .endControlFlow();

    builder
        .addStatement("this.$N = $N", LONG_NAME, longName)
        .addStatement("this.$N = $N == null ? null : $N.charAt(0)", SHORT_NAME, shortName, shortName)
        .addStatement("this.$N = $N", this.optionType, optionType)
        .addStatement("this.$N = $T.unmodifiableList($T.asList($N))", DESCRIPTION,
            Collections.class, Arrays.class, description)
        .addStatement("this.$N = $N", ARGUMENT_NAME, argumentName);

    builder.addParameters(Arrays.asList(
        longName, shortName, optionType, argumentName, description));
    return builder.build();
  }

  private static MethodSpec shortNameMethod() {
    return MethodSpec.methodBuilder(SHORT_NAME.name)
        .addStatement("return $T.toString($N, null)", Objects.class, SHORT_NAME)
        .returns(STRING)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec longNameMethod() {
    return MethodSpec.methodBuilder(LONG_NAME.name)
        .addStatement("return $N", LONG_NAME)
        .returns(LONG_NAME.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec descriptionMethod() {
    return MethodSpec.methodBuilder(DESCRIPTION.name)
        .addStatement("return $N", DESCRIPTION)
        .returns(DESCRIPTION.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec descriptionParameterMethod() {
    return MethodSpec.methodBuilder(ARGUMENT_NAME.name)
        .addStatement("return $N", ARGUMENT_NAME)
        .returns(ARGUMENT_NAME.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec typeMethod() {
    return MethodSpec.methodBuilder(optionType.name)
        .addStatement("return $N", optionType)
        .returns(optionType.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec isSpecialMethod() {
    return MethodSpec.methodBuilder(OptionType.IS_SPECIAL.name)
        .addStatement("return $N.$N()", optionType, OptionType.IS_SPECIAL)
        .returns(OptionType.IS_SPECIAL.returnType)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec isBindingMethod() {
    return MethodSpec.methodBuilder(IS_BINDING.name)
        .addStatement("return $N.$N()", optionType, IS_BINDING)
        .returns(IS_BINDING.returnType)
        .addModifiers(PUBLIC)
        .build();
  }

  private static String upcase(String input) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i > 0) {
          sb.append('_');
        }
        sb.append(c);
      } else {
        sb.append(Character.toUpperCase(c));
      }
    }
    return sb.toString();
  }

  private static String[] getText(Description description) {
    if (description == null) {
      return new String[]{"--- description goes here ---"};
    }
    return description.value();
  }

  private static MethodSpec descriptionBlockMethod() {
    ParameterSpec indent = ParameterSpec.builder(TypeName.INT, "indent").build();
    ParameterSpec sIndent = ParameterSpec.builder(STRING, "indentString").build();
    ParameterSpec aIndent = ParameterSpec.builder(ArrayTypeName.of(TypeName.CHAR), "a").build();
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = new $T[$N]", aIndent.type, aIndent, TypeName.CHAR, indent)
        .addStatement("$T.fill($N, ' ')", Arrays.class, aIndent)
        .addStatement("$T $N = new $T($N)", STRING, sIndent, STRING, aIndent)
        .addStatement("return $N + $T.join('\\n' + $N, $N)", sIndent, STRING, sIndent, DESCRIPTION);
    return MethodSpec.methodBuilder("descriptionBlock")
        .addModifiers(PUBLIC)
        .addParameter(indent)
        .returns(Analyser.STRING)
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
        .returns(Analyser.STRING)
        .addParameter(indent)
        .addCode(codeBlock)
        .build();
  }

  private static MethodSpec describeNamesMethod(
      MethodSpec describeParamMethod,
      FieldSpec optionType,
      ClassName optionTypeClass) {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.beginControlFlow("if ($N())", IS_BINDING)
        .addStatement("return $N() + ' ' + $N", describeParamMethod, ARGUMENT_NAME)
        .endControlFlow();
    builder.addStatement("return $N()", describeParamMethod);
    return MethodSpec.methodBuilder("describeNames")
        .addModifiers(PUBLIC)
        .returns(Analyser.STRING)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec describeParamMethod(
      Context context,
      FieldSpec optionType,
      ClassName optionTypeClass) {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    CodeBlock.Builder builder = CodeBlock.builder();

    builder.beginControlFlow("if ($N == $T.$L)", optionType, optionTypeClass, OTHER_TOKENS)
        .addStatement("return $S", "Other tokens")
        .endControlFlow();

    builder.beginControlFlow("if ($N == $T.$L)", optionType, optionTypeClass, EVERYTHING_AFTER)
        .addStatement("return $S + $S + $S", "Everything after '", context.stopword, "'")
        .endControlFlow();

    builder.addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class);

    builder.beginControlFlow("if ($N != null)", SHORT_NAME)
        .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
        .endControlFlow();

    builder.beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
        .addStatement("$N.append(',').append(' ')", sb)
        .endControlFlow();

    builder.beginControlFlow("if ($N != null)", LONG_NAME)
        .addStatement("$N.append('-').append('-').append($N)", sb, LONG_NAME)
        .endControlFlow();

    builder.addStatement("return $N.toString()", sb);

    return MethodSpec.methodBuilder("describeParam")
        .addModifiers(PRIVATE)
        .returns(Analyser.STRING)
        .addCode(builder.build())
        .build();
  }


  private MethodSpec toStringMethod() {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    return MethodSpec.methodBuilder("toString")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .returns(Analyser.STRING)
        .addStatement("return name() + $S + $N() + $S", " (", describeParamMethod, ")")
        .build();
  }
}
