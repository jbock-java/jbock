package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;

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
import net.jbock.com.squareup.javapoet.TypeSpec;

final class Helper {

  private final ClassName optionClass;
  private final ClassName keysClass;
  private final FieldSpec longNames;
  private final FieldSpec shortNames;

  final FieldSpec optMap;
  final FieldSpec sMap;
  final FieldSpec flags;

  private Helper(
      ClassName optionClass,
      ClassName keysClass,
      FieldSpec longNames,
      FieldSpec shortNames,
      FieldSpec optMap,
      FieldSpec sMap,
      FieldSpec flags) {
    this.optionClass = optionClass;
    this.keysClass = keysClass;
    this.longNames = longNames;
    this.shortNames = shortNames;
    this.optMap = optMap;
    this.sMap = sMap;
    this.flags = flags;
  }

  static Helper create(Analyser analyser) {
    FieldSpec optMap = FieldSpec.builder(analyser.optMapType, "optMap", FINAL).build();
    FieldSpec sMap = FieldSpec.builder(analyser.sMapType, "sMap", FINAL).build();
    FieldSpec flags = FieldSpec.builder(analyser.flagsType, "flags", FINAL).build();

    return new Helper(
        analyser.option.optionClass,
        analyser.helperClass,
        analyser.longNames,
        analyser.shortNames,
        optMap,
        sMap,
        flags);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(keysClass)
        .addFields(Arrays.asList(longNames, shortNames, optMap, sMap, flags))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(privateConstructor())
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec longNames = ParameterSpec.builder(this.longNames.type, this.longNames.name)
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(this.shortNames.type, this.shortNames.name)
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

}
