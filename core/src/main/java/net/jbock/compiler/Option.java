package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.ArgumentName;
import net.jbock.Description;
import net.jbock.compiler.Processor.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;
import static net.jbock.compiler.Analyser.STRING;

final class Option {

  private static final FieldSpec DESCRIPTION = FieldSpec.builder(
      Analyser.STRING_LIST, "description", PRIVATE, FINAL).build();

  private static final FieldSpec ARGUMENT_NAME = FieldSpec.builder(
      Analyser.STRING, "descriptionParameter", PRIVATE, FINAL).build();

  private static final String DD = "--";
  private static final String CS = ", ";
  private static final String NL = "\n";

  final ClassName optionClass;

  private final Constructor constructor;
  private final ClassName optionTypeClass;
  private final boolean needsSuffix;

  private final FieldSpec optionType;
  private final MethodSpec describeNamesMethod;
  private final MethodSpec descriptionBlockMethod;

  private Option(Constructor constructor, ClassName optionClass, ClassName optionTypeClass, FieldSpec optionType) {
    this.constructor = constructor;
    this.optionClass = optionClass;
    this.optionTypeClass = optionTypeClass;
    this.optionType = optionType;
    this.describeNamesMethod = describeNamesMethod(optionType, optionTypeClass);
    this.descriptionBlockMethod = descriptionBlockMethod();
    Set<String> uppercaseArgumentNames = IntStream.range(0, constructor.parameters.size())
        .mapToObj(this::enumConstant)
        .collect(Collectors.toSet());
    this.needsSuffix = uppercaseArgumentNames.size() < constructor.parameters.size();
  }

  static Option create(Constructor constructor, ClassName argumentInfo, ClassName optionTypeClass, FieldSpec optionType) {
    return new Option(constructor, argumentInfo, optionTypeClass, optionType);
  }

  String enumConstant(int i) {
    String suffix = needsSuffix ? String.format("_%d", i) : "";
    return upcase(constructor.parameters.get(i).parameterName) + suffix;
  }

  static String constructorArgumentsForJavadoc(Constructor constructor) {
    return constructor.parameters.stream()
        .map(o -> o.optionType)
        .map(type -> {
          switch (type) {
            case FLAG:
              return "boolean";
            case STRING:
              return "String";
            default:
              return "java.util.List";
          }
        })
        .collect(Collectors.joining(",\n       "));
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(optionClass)
        .addJavadoc("The enum constants correspond to the constructor arguments.\n");
    for (int i = 0; i < constructor.parameters.size(); i++) {
      Names names = constructor.parameters.get(i);
      String[] desc = getText(names.description);
      String argumentName = Processor.ARGNAME_LESS.contains(names.optionType) ? null : getArgumentName(names.argName);
      String enumConstant = enumConstant(i);
      String format = String.format("$S, $S, $T.$L, $S, new $T[] {%s}",
          String.join(", ", Collections.nCopies(desc.length, "$S")));
      List<Comparable<? extends Comparable<?>>> fixArgs =
          Arrays.asList(names.longName, names.shortName(), optionTypeClass, names.optionType, argumentName, STRING);
      List<Object> args = new ArrayList<>(fixArgs.size() + desc.length);
      args.addAll(fixArgs);
      args.addAll(Arrays.asList(desc));
      builder.addEnumConstant(enumConstant, anonymousClassBuilder(format,
          args.toArray()).build());
    }
    return builder.addModifiers(PUBLIC)
        .addFields(Arrays.asList(LONG_NAME, SHORT_NAME, optionType, ARGUMENT_NAME, DESCRIPTION))
        .addMethod(describeMethod())
        .addMethod(describeNamesMethod)
        .addMethod(descriptionBlockMethod)
        .addMethod(shortNameMethod())
        .addMethod(longNameMethod())
        .addMethod(descriptionMethod())
        .addMethod(descriptionParameterMethod())
        .addMethod(typeMethod())
        .addMethod(privateConstructor())
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec longName = ParameterSpec.builder(LONG_NAME.type, LONG_NAME.name).build();
    ParameterSpec shortName = ParameterSpec.builder(STRING, SHORT_NAME.name).build();
    ParameterSpec optionType = ParameterSpec.builder(this.optionType.type, this.optionType.name).build();
    ParameterSpec description = ParameterSpec.builder(ArrayTypeName.of(STRING), DESCRIPTION.name).build();
    ParameterSpec argumentName = ParameterSpec.builder(ARGUMENT_NAME.type, ARGUMENT_NAME.name).build();
    //@formatter:off
    return MethodSpec.constructorBuilder()
        .beginControlFlow("if ($N != null && $N.length() != 1)", shortName, shortName)
          .addStatement("throw new $T()", AssertionError.class)
          .endControlFlow()
        .beginControlFlow("if ($N != null && $N.length() < 1)", longName, longName)
          .addStatement("throw new $T()", AssertionError.class)
          .endControlFlow()
        .beginControlFlow("if ($N == null)", description)
          .addStatement("throw new $T($S)", AssertionError.class, description.name)
          .endControlFlow()
        .addStatement("this.$N = $N", LONG_NAME, longName)
        .addStatement("this.$N = $N == null ? null : $N.charAt(0)", SHORT_NAME, shortName, shortName)
        .addStatement("this.$N = $N", this.optionType, optionType)
        .addStatement("this.$N = $T.unmodifiableList($T.asList($N))", DESCRIPTION,
            Collections.class, Arrays.class, description)
        .addStatement("this.$N = $N", ARGUMENT_NAME, argumentName)
        .addParameters(Arrays.asList(longName, shortName, optionType, argumentName, description))
        .build();
    //@formatter:on
  }

  private static MethodSpec shortNameMethod() {
    return MethodSpec.methodBuilder(SHORT_NAME.name)
        .addStatement("return $T.toString($N, null)", Objects.class, SHORT_NAME)
        .returns(STRING)
        .addJavadoc("The short name is exactly one character in length, or null.\n" +
            "\n" +
            "@return short name, without the '-'; possibly null\n")
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec longNameMethod() {
    return MethodSpec.methodBuilder(LONG_NAME.name)
        .addStatement("return $N", LONG_NAME)
        .returns(LONG_NAME.type)
        .addJavadoc("The long name is at least one character in length, or null.\n" +
            "\n" +
            "@return long name, without the '--'; possibly null\n")
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec descriptionMethod() {
    return MethodSpec.methodBuilder(DESCRIPTION.name)
        .addStatement("return $N", DESCRIPTION)
        .returns(DESCRIPTION.type)
        .addJavadoc("@return description lines\n")
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec descriptionParameterMethod() {
    return MethodSpec.methodBuilder(ARGUMENT_NAME.name)
        .addStatement("return $N", ARGUMENT_NAME)
        .addJavadoc("@return example parameter name, possibly null\n")
        .returns(ARGUMENT_NAME.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec typeMethod() {
    return MethodSpec.methodBuilder(optionType.name)
        .addStatement("return $N", optionType)
        .addJavadoc("@return option type\n")
        .returns(optionType.type)
        .addModifiers(PUBLIC)
        .build();
  }

  public static String upcase(String input) {
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

  private static String getArgumentName(ArgumentName argumentName) {
    if (argumentName == null) {
      return "VAL";
    }
    return argumentName.value();
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
        .addStatement("$N.append($S)", sb, NL)
        .addStatement("$N.append($N($N))", sb, descriptionBlockMethod, indent)
        .addStatement("return $N.toString()", sb)
        .build();
    return MethodSpec.methodBuilder("describe")
        .addModifiers(PUBLIC)
        .returns(Analyser.STRING)
        .addParameter(indent)
        .addJavadoc("Convenience method to get a formatted description of the argument.\n" +
            "\n" +
            "@param indent number of space characters to indent the description with\n" +
            "@return printable description\n")
        .addCode(codeBlock)
        .build();
  }

  private static MethodSpec describeNamesMethod(FieldSpec optionType, ClassName optionTypeClass) {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
      .beginControlFlow("if ($N == $T.$L)", optionType, optionTypeClass, OptionType.OTHER_TOKENS)
        .addStatement("return '[' + $N + ']'", LONG_NAME)
        .endControlFlow()
      .beginControlFlow("if ($N == $T.$L)", optionType, optionTypeClass, OptionType.FLAG)
        .addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class)
        .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
          .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
          .addStatement("$N.append($S).append($S).append($N)", sb, CS, DD, LONG_NAME)
          .endControlFlow()
        .beginControlFlow("else if ($N != null)", LONG_NAME)
          .addStatement("$N.append($S).append($N)", sb, DD, LONG_NAME)
          .endControlFlow()
        .beginControlFlow("else")
          .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
          .endControlFlow()
        .addStatement("return $N.toString()", sb)
        .endControlFlow()
      .addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class)
      .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
        .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
        .addStatement("$N.append($S).append($S).append($N).append('=').append($N)", sb, CS, DD, LONG_NAME, ARGUMENT_NAME)
        .endControlFlow()
      .beginControlFlow("else if ($N != null)", LONG_NAME)
        .addStatement("$N.append($S).append($N).append('=').append($N)", sb, DD, LONG_NAME, ARGUMENT_NAME)
        .endControlFlow()
      .beginControlFlow("else")
        .addStatement("$N.append('-').append($N).append(' ').append($N)", sb, SHORT_NAME, ARGUMENT_NAME)
        .endControlFlow()
      .addStatement("return $N.toString()", sb);
    //@formatter:on
    return MethodSpec.methodBuilder("describeNames")
        .addModifiers(PUBLIC)
        .returns(Analyser.STRING)
        .addCode(builder.build())
        .build();
  }
}
