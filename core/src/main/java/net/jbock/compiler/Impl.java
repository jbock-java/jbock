package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.STRING;
import static net.jbock.compiler.Analyser.STRING_LIST;
import static net.jbock.compiler.Analyser.otherTokens;

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

final class Impl {

  private final ClassName implClass;
  private final Option option;
  private final TypeName keysClass;
  private final FieldSpec optMap;
  private final FieldSpec sMap;
  private final FieldSpec flags;
  private final FieldSpec rest = FieldSpec.builder(STRING_LIST, "rest")
      .addModifiers(FINAL)
      .build();
  private final JbockContext context;

  private Impl(
      ClassName implClass,
      ClassName keysClass,
      Option option,
      FieldSpec optMap,
      FieldSpec sMap,
      FieldSpec flags,
      JbockContext context) {
    this.implClass = implClass;
    this.keysClass = keysClass;
    this.option = option;
    this.optMap = optMap;
    this.sMap = sMap;
    this.flags = flags;
    this.context = context;
  }

  static Impl create(Analyser analyser, Helper helper) {
    return new Impl(
        analyser.implClass,
        analyser.helperClass,
        analyser.option,
        helper.optMap,
        helper.sMap,
        helper.flags,
        analyser.context);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(implClass)
        .superclass(TypeName.get(context.sourceType.asType()))
        .addFields(Arrays.asList(optMap, sMap, flags, otherTokens, rest))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethods(bindMethods())
        .build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      OptionType optionType = param.optionType();
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.parameterName())
          .addModifiers(PUBLIC)
          .addAnnotation(Override.class)
          .returns(optionType.sourceType);
      if (optionType == OptionType.FLAG) {
        builder.addStatement("return $N.contains($T.$N)", flags, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OPTIONAL) {
        builder.addStatement("return $T.ofNullable($N.get($T.$L))",
            Optional.class, sMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.REQUIRED) {
        ParameterSpec p = ParameterSpec.builder(STRING, option.enumConstant(j).toLowerCase(Locale.US)).build();
        builder.addStatement("$T $N = $N.get($T.$L)",
            STRING, p, sMap,
            option.optionClass, option.enumConstant(j));
        builder.addStatement("assert $N != null", p);
        builder.addStatement("return $N", p);
      } else if (optionType == OptionType.OTHER_TOKENS) {
        builder.addStatement("return $N", otherTokens);
      } else if (optionType == OptionType.EVERYTHING_AFTER) {
        builder.addStatement("return $N", rest);
      } else {
        builder.addStatement("return $N.getOrDefault($T.$L, $T.emptyList())",
            optMap, option.optionClass, option.enumConstant(j), Collections.class);
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
        Analyser.otherTokens.type, Analyser.otherTokens.name).build();
    ParameterSpec esc = ParameterSpec.builder(this.rest.type, this.rest.name).build();
    builder.addStatement("this.$N = $N.$N", this.optMap, helper, optMap);
    builder.addStatement("this.$N = $N.$N", this.sMap, helper, sMap);
    builder.addStatement("this.$N = $N.$N", this.flags, helper, flags);
    builder.addStatement("this.$N = $N", Analyser.otherTokens, otherTokens);
    builder.addStatement("this.$N = $N", this.rest, esc);
    ParameterSpec p = ParameterSpec.builder(option.optionClass, "option")
        .build();
    builder.beginControlFlow("for ($T $N: $T.values())", p.type, p, p.type)
        .beginControlFlow("if ($N.$N == $T.$L && $N.get($N) == null)",
            p, option.optionTypeField, option.optionType, OptionType.REQUIRED, sMap, p)
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
