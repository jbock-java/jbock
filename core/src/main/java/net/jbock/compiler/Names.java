package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;

final class Names {

  private final ClassName optionClass;
  private final ClassName keysClass;
  private final FieldSpec longNames;
  private final FieldSpec shortNames;

  private Names(ClassName optionClass,
                ClassName keysClass,
                FieldSpec longNames, FieldSpec shortNames) {
    this.optionClass = optionClass;
    this.keysClass = keysClass;
    this.longNames = longNames;
    this.shortNames = shortNames;
  }

  static Names create(Analyser analyser) {
    return new Names(
        analyser.option.optionClass,
        analyser.keysClass,
        analyser.longNames,
        analyser.shortNames);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(keysClass)
        .addFields(Arrays.asList(longNames, shortNames))
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
    builder.addStatement("$T $N = new $T<>()", longNames.type, longNames, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortNames.type, shortNames, HashMap.class);
    //@formatter:off
    builder.beginControlFlow("for ($T $N : $T.values())", optionClass, option, optionClass)
          .beginControlFlow("if ($N.$N() != null)", option, SHORT_NAME)
            .beginControlFlow("if ($N.put($N.$N(), $N) != null)", shortNames, option, SHORT_NAME, option)
              .addStatement("throw new $T($S + $N)", AssertionError.class, "duplicate short: ", option)
              .endControlFlow()
            .endControlFlow()
          .beginControlFlow("if ($N.$N() != null)", option, LONG_NAME)
            .beginControlFlow("if ($N.put($N.$N(), $N) != null)", longNames, option, LONG_NAME, option)
              .addStatement("throw new $T($S + $N)", AssertionError.class, "duplicate long: ", option)
              .endControlFlow()
            .endControlFlow()
        .endControlFlow();
    //@formatter:on
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", longNames, Collections.class, longNames);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", shortNames, Collections.class, shortNames);
    return MethodSpec.constructorBuilder()
        .addCode(builder.build())
        .build();
  }

}
