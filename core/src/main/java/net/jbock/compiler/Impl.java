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
import java.util.Locale;
import java.util.Optional;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  private final Option option;
  private final OptionType optionType;
  private final TypeName keysClass;
  private final FieldSpec restField = FieldSpec.builder(LIST_OF_STRING, "rest")
      .addModifiers(FINAL)
      .build();

  private final Context context;
  private final Helper helper;

  private Impl(
      ClassName type,
      ClassName keysClass,
      Option option,
      OptionType optionType,
      Context context,
      Helper helper) {
    this.type = type;
    this.keysClass = keysClass;
    this.option = option;
    this.optionType = optionType;
    this.context = context;
    this.helper = helper;
  }

  static Impl create(
      Context context,
      OptionType optionType,
      Option option,
      Helper helper) {
    ClassName implClass = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    return new Impl(
        implClass,
        helper.type,
        option,
        optionType,
        context,
        helper);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(type)
        .superclass(TypeName.get(context.sourceType.asType()))
        .addFields(Arrays.asList(
            helper.optMapField,
            helper.sMapField,
            helper.flagsField,
            helper.otherTokensField,
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
      Type optionType = param.optionType();
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.parameterName())
          .addModifiers(PUBLIC)
          .addAnnotation(Override.class)
          .returns(optionType.sourceType);
      if (optionType == Type.FLAG) {
        builder.addStatement("return $N.contains($T.$N)",
            this.helper.flagsField, option.type, option.enumConstant(j));
      } else if (optionType == Type.OPTIONAL) {
        builder.addStatement("return $T.ofNullable($N.get($T.$L))",
            Optional.class, this.helper.sMapField, option.type, option.enumConstant(j));
      } else if (optionType == Type.REQUIRED) {
        ParameterSpec p = ParameterSpec.builder(STRING, option.enumConstant(j).toLowerCase(Locale.US)).build();
        builder.addStatement("$T $N = $N.get($T.$L)",
            STRING, p, this.helper.sMapField,
            option.type, option.enumConstant(j))
            .addStatement("assert $N != null", p)
            .addStatement("return $N", p);
      } else if (optionType == Type.OTHER_TOKENS) {
        builder.addStatement("return $N", helper.otherTokensField);
      } else if (optionType == Type.EVERYTHING_AFTER) {
        builder.addStatement("return $N", restField);
      } else {
        builder.addStatement("return $N.getOrDefault($T.$L, $T.emptyList())",
            this.helper.optMapField, option.type, option.enumConstant(j), Collections.class);
      }
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec helper = ParameterSpec.builder(this.keysClass, "helper")
        .build();
    ParameterSpec otherTokens = ParameterSpec.builder(
        this.helper.otherTokensField.type, this.helper.otherTokensField.name).build();
    ParameterSpec esc = ParameterSpec.builder(this.restField.type, this.restField.name).build();
    builder.addStatement("this.$N = $N.$N", this.helper.optMapField, helper, this.helper.optMapField);
    builder.addStatement("this.$N = $N.$N", this.helper.sMapField, helper, this.helper.sMapField);
    builder.addStatement("this.$N = $N.$N", this.helper.flagsField, helper, this.helper.flagsField);
    builder.addStatement("this.$N = $N", this.helper.otherTokensField, otherTokens);
    builder.addStatement("this.$N = $N", this.restField, esc);
    ParameterSpec p = ParameterSpec.builder(option.type, "option")
        .build();
    builder.beginControlFlow("for ($T $N: $T.values())", p.type, p, p.type)
        .beginControlFlow("if ($N.$N == $T.$L && $N.get($N) == null)",
            p, option.typeField, optionType.type, Type.REQUIRED, this.helper.sMapField, p)
        .addStatement("throw new $T($S + $N)", IllegalArgumentException.class,
            "Missing required option: ", p)
        .endControlFlow()
        .endControlFlow();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(helper, otherTokens, esc))
        .addCode(builder.build())
        .build();
  }
}
