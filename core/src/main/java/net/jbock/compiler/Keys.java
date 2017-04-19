package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;

final class Keys {

  private final ClassName optionClass;
  private final ClassName optionTypeClass;
  private final ClassName keysClass;
  private final FieldSpec longFlags;
  private final FieldSpec shortFlags;
  private final FieldSpec longNames;
  private final FieldSpec shortNames;
  private final FieldSpec optionType;

  private Keys(ClassName optionClass,
               ClassName optionTypeClass,
               ClassName keysClass,
               FieldSpec longFlags, FieldSpec shortFlags,
               FieldSpec longNames, FieldSpec shortNames, FieldSpec optionType) {
    this.optionClass = optionClass;
    this.optionTypeClass = optionTypeClass;
    this.keysClass = keysClass;
    this.longFlags = longFlags;
    this.shortFlags = shortFlags;
    this.longNames = longNames;
    this.shortNames = shortNames;
    this.optionType = optionType;
  }

  static Keys create(ClassName optionClass,
                     ClassName optionTypeClass,
                     ClassName keysClass,
                     FieldSpec longFlags, FieldSpec shortFlags,
                     FieldSpec longNames, FieldSpec shortNames,
                     FieldSpec optionType) {
    return new Keys(optionClass, optionTypeClass, keysClass, longFlags, shortFlags, longNames, shortNames, optionType);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(keysClass)
        .addFields(Arrays.asList(longFlags, shortFlags, longNames, shortNames))
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(privateConstructor())
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec shortFlags = ParameterSpec.builder(this.shortFlags.type, this.shortFlags.name)
        .build();
    ParameterSpec longFlags = ParameterSpec.builder(this.longFlags.type, this.longFlags.name)
        .build();
    ParameterSpec longNames = ParameterSpec.builder(this.longNames.type, this.longNames.name)
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(this.shortNames.type, this.shortNames.name)
        .build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option")
        .build();
    builder.addStatement("$T $N = new $T<>()", longFlags.type, longFlags, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortFlags.type, shortFlags, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", longNames.type, longNames, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortNames.type, shortNames, HashMap.class);
    //@formatter:off
    builder.beginControlFlow("for ($T $N : $T.values())", optionClass, option, optionClass)
          .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG)
            .beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
              .addStatement("$N.put($N.$N, $N)", shortFlags, option, SHORT_NAME, option)
              .endControlFlow()
            .beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
              .addStatement("$N.put($N.$N, $N)", longFlags, option, LONG_NAME, option)
              .endControlFlow()
            .endControlFlow()
          .beginControlFlow("else")
            .beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
              .addStatement("$N.put($N.$N, $N)", shortNames, option, SHORT_NAME, option)
              .endControlFlow()
            .beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
              .addStatement("$N.put($N.$N, $N)", longNames, option, LONG_NAME, option)
              .endControlFlow()
            .endControlFlow()
        .endControlFlow();
    //@formatter:on
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", longFlags, Collections.class, longFlags);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", shortFlags, Collections.class, shortFlags);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", longNames, Collections.class, longNames);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", shortNames, Collections.class, shortNames);
    return MethodSpec.constructorBuilder()
        .addCode(builder.build())
        .build();
  }

}
