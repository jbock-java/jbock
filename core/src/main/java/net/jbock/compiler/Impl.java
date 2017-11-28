package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  private final FieldSpec optMapField;
  private final FieldSpec sMapField;
  private final FieldSpec flagsField;

  private final Option option;
  private final OptionType optionType;
  private final FieldSpec restField = FieldSpec.builder(LIST_OF_STRING, "rest", FINAL)
      .build();
  private final FieldSpec otherTokensField = FieldSpec.builder(LIST_OF_STRING, "otherTokens", FINAL)
      .build();

  private final Context context;
  private final Helper helper;

  private Impl(
      ClassName type,
      FieldSpec optMapField,
      FieldSpec sMapField,
      FieldSpec flagsField,
      Option option,
      OptionType optionType,
      Context context,
      Helper helper) {
    this.type = type;
    this.optMapField = optMapField;
    this.sMapField = sMapField;
    this.flagsField = flagsField;
    this.option = option;
    this.optionType = optionType;
    this.context = context;
    this.helper = helper;
  }

  static Impl create(
      Context context,
      ClassName implType,
      OptionType optionType,
      Option option,
      Helper helper) {
    FieldSpec optMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, LIST_OF_STRING), "optMap", FINAL).build();
    FieldSpec sMapField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
        option.type, STRING), "sMap", FINAL).build();
    FieldSpec flagsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Set.class),
        option.type), "flags", FINAL).build();
    return new Impl(
        implType,
        optMapField,
        sMapField,
        flagsField, option,
        optionType,
        context,
        helper);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(type)
        .superclass(TypeName.get(context.sourceType.asType()))
        .addFields(Arrays.asList(
            optMapField,
            sMapField,
            flagsField,
            otherTokensField,
            restField))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethods(bindMethods())
        .build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.parameterName())
          .addModifiers(PUBLIC)
          .addAnnotation(Override.class)
          .returns(param.optionType.sourceType);
      if (param.optionType == Type.FLAG) {
        builder.addStatement("return $N.contains($T.$N)",
            flagsField, option.type, option.enumConstant(j));
      } else if (param.optionType == Type.OPTIONAL) {
        builder.addStatement("return $T.ofNullable($N.get($T.$L))",
            Optional.class, sMapField, option.type, option.enumConstant(j));
      } else if (param.optionType == Type.REQUIRED) {
        builder.addStatement("return $N.get($T.$L)",
            sMapField, option.type, option.enumConstant(j));
      } else if (param.optionType == Type.OTHER_TOKENS) {
        builder.addStatement("return $N", otherTokensField);
      } else if (param.optionType == Type.EVERYTHING_AFTER) {
        builder.addStatement("return $N", restField);
      } else {
        builder.addStatement("return $N.getOrDefault($T.$L, $T.emptyList())",
            optMapField, option.type, option.enumConstant(j), Collections.class);
      }
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec otherTokens = ParameterSpec.builder(otherTokensField.type, otherTokensField.name).build();
    ParameterSpec rest = ParameterSpec.builder(restField.type, restField.name).build();
    ParameterSpec optMap = ParameterSpec.builder(optMapField.type, optMapField.name).build();
    ParameterSpec sMap = ParameterSpec.builder(sMapField.type, sMapField.name).build();
    ParameterSpec flags = ParameterSpec.builder(flagsField.type, flagsField.name).build();
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", optMapField, Collections.class, optMap);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", sMapField, Collections.class, sMap);
    builder.addStatement("this.$N = $T.unmodifiableSet($N)", flagsField, Collections.class, flags);
    builder.addStatement("this.$N = $T.unmodifiableList($N)", otherTokensField, Collections.class, otherTokens);
    builder.addStatement("this.$N = $T.unmodifiableList($N)", restField, Collections.class, rest);
    ParameterSpec p = ParameterSpec.builder(option.type, "option")
        .build();
    builder.add("\n");
    builder.beginControlFlow("for ($T $N: $T.values())", p.type, p, p.type)
        .beginControlFlow("if ($N.$N == $T.$L && $N.get($N) == null)",
            p, option.typeField, optionType.type, Type.REQUIRED, sMapField, p)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing required option: ", p)
        .endControlFlow()
        .endControlFlow();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(optMap, sMap, flags, otherTokens, rest))
        .addCode(builder.build())
        .build();
  }
}
