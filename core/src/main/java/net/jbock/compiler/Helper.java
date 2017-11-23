package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;
import static net.jbock.compiler.Analyser.STRING;
import static net.jbock.compiler.Analyser.STRING_LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

final class Helper {

  private final ClassName optionClass;
  private final ClassName optionTypeClass;
  private final ClassName keysClass;
  private final FieldSpec longNamesField;
  private final FieldSpec shortNamesField;

  final FieldSpec optMap;
  final FieldSpec sMap;
  final FieldSpec flags;
  final Option option;
  final MethodSpec addFlagMethod;
  final MethodSpec addMethod;

  private Helper(
      ClassName optionClass,
      ClassName optionTypeClass,
      ClassName keysClass,
      FieldSpec longNamesField,
      FieldSpec shortNamesField,
      FieldSpec optMap,
      FieldSpec sMap,
      FieldSpec flags,
      Option option,
      MethodSpec addFlagMethod,
      MethodSpec addMethod) {
    this.optionClass = optionClass;
    this.optionTypeClass = optionTypeClass;
    this.keysClass = keysClass;
    this.longNamesField = longNamesField;
    this.shortNamesField = shortNamesField;
    this.optMap = optMap;
    this.sMap = sMap;
    this.flags = flags;
    this.option = option;
    this.addFlagMethod = addFlagMethod;
    this.addMethod = addMethod;
  }

  static Helper create(
      TypeName optMapType,
      TypeName sMapType,
      TypeName flagsType,
      Option option,
      ClassName helperClass,
      FieldSpec longNamesField,
      FieldSpec shortNamesField) {
    FieldSpec optMap = FieldSpec.builder(optMapType, "optMap", FINAL).build();
    FieldSpec sMap = FieldSpec.builder(sMapType, "sMap", FINAL).build();
    FieldSpec flags = FieldSpec.builder(flagsType, "flags", FINAL).build();
    MethodSpec addFlagMethod = addFlagMethod(option.optionClass, option.optionTypeClass, flags);
    MethodSpec addMethod = addMethod(option.optionClass, option.optionTypeClass,
        optMap, sMap, option.isBindingMethod);
    return new Helper(
        option.optionClass,
        option.optionTypeClass,
        helperClass,
        longNamesField,
        shortNamesField,
        optMap,
        sMap,
        flags,
        option,
        addFlagMethod,
        addMethod);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(keysClass)
        .addFields(Arrays.asList(longNamesField, shortNamesField, optMap, sMap, flags))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(addMethod)
        .addMethod(addFlagMethod)
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec longNames = ParameterSpec.builder(this.longNamesField.type, this.longNamesField.name)
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(this.shortNamesField.type, this.shortNamesField.name)
        .build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option")
        .build();

    builder.add("\n");
    builder.addStatement("this.$N = new $T<>($T.class)", optMap, EnumMap.class, optionClass)
        .addStatement("this.$N = new $T<>($T.class)", sMap, EnumMap.class, optionClass)
        .addStatement("this.$N = $T.noneOf($T.class)", flags, EnumSet.class, optionClass);

    builder.add("\n");
    builder.addStatement("$T $N = new $T<>()",
        longNames.type, longNames, HashMap.class)
        .addStatement("$T $N = new $T<>()",
            shortNames.type, shortNames, HashMap.class);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", optionClass, option, optionClass);

    builder.beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
        .addStatement("$N.put($N.$N.toString(), $N)", shortNames, option, SHORT_NAME, option)
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
        .addStatement("$N.put($N.$N, $N)", longNames, option, LONG_NAME, option)
        .endControlFlow();

    // end iteration over options
    builder.endControlFlow();

    builder.addStatement("this.$N = $T.unmodifiableMap($N)",
        longNames, Collections.class, longNames);

    builder.addStatement("this.$N = $T.unmodifiableMap($N)",
        shortNames, Collections.class, shortNames);

    return MethodSpec.constructorBuilder()
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec addFlagMethod(
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec flags) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    return MethodSpec.methodBuilder("add")
        .addStatement("assert $N.type == $T.$L", option, optionTypeClass, OptionType.FLAG)
        .addStatement("return $N.add($N)", flags, option)
        .addParameter(option)
        .returns(BOOLEAN)
        .build();
  }

  private static MethodSpec addMethod(
      ClassName optionClass,
      ClassName optionTypeClass,
      FieldSpec optMap,
      FieldSpec sMap,
      MethodSpec isBindingMethod) {
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec argument = ParameterSpec.builder(STRING, "argument").build();
    ParameterSpec bucket = ParameterSpec.builder(STRING_LIST, "bucket").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("add");
    builder.addStatement("assert $N.$N()", option, isBindingMethod);

    // begin handle repeatable
    builder.beginControlFlow("if ($N.type == $T.$L)", option, optionTypeClass, OptionType.REPEATABLE);

    builder.addStatement("$T $N = $N.get($N)", bucket.type, bucket, optMap, option);
    builder.beginControlFlow("if ($N == null)", bucket)
        .addStatement("$N = new $T<>()", bucket, ArrayList.class)
        .addStatement("$N.put($N, $N)", optMap, option, bucket)
        .endControlFlow();
    builder.addStatement("$N.add($N)", bucket, argument);
    builder.addStatement("return $L", true);

    // done handling repeatable
    builder.endControlFlow();

    builder.beginControlFlow("if ($N.containsKey($N))", sMap, option)
        .addStatement("return $L", false)
        .endControlFlow()
        .addStatement("$N.put($N, $N)", sMap, option, argument);

    builder.addStatement("return $L", true);

    return builder.addParameters(Arrays.asList(option, argument))
        .returns(BOOLEAN)
        .build();
  }
}
