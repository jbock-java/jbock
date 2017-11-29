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
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .superclass(TypeName.get(context.sourceType.asType()))
        .addFields(Arrays.asList(
            optMapField,
            sMapField,
            flagsField));
    if (context.otherTokens) {
      builder.addField(otherTokensField);
    }
    if (context.everythingAfter()) {
      builder.addField(restField);
    }
    return builder
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
      builder.addStatement(param.optionType.extractStatement(option, j));
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", optMapField, Collections.class, option.optMapParameter);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", sMapField, Collections.class, option.sMapParameter);
    builder.addStatement("this.$N = $T.unmodifiableSet($N)", flagsField, Collections.class, option.flagsParameter);
    if (context.otherTokens) {
      builder.addStatement("this.$N = $T.unmodifiableList($N)", otherTokensField, Collections.class, option.otherTokensParameter);
    }
    if (context.everythingAfter()) {
      builder.addStatement("this.$N = $T.unmodifiableList($N)", restField, Collections.class, option.restParameter);
    }
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
        .addParameters(Arrays.asList(
            option.optMapParameter,
            option.sMapParameter,
            option.flagsParameter,
            option.otherTokensParameter,
            option.restParameter))
        .addCode(builder.build())
        .build();
  }
}
