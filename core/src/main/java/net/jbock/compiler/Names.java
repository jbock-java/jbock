package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

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

    builder.addStatement("$T $N = new $T<>()",
        longNames.type, longNames, HashMap.class);

    builder.addStatement("$T $N = new $T<>()",
        shortNames.type, shortNames, HashMap.class);

    // begin iteration over options
    builder.beginControlFlow("for ($T $N : $T.values())", optionClass, option, optionClass);

    builder.beginControlFlow("if ($N.$N() != null)", option, SHORT_NAME)
        .addStatement("$N.put($N.$N(), $N)", shortNames, option, SHORT_NAME, option)
        .endControlFlow();

    builder.beginControlFlow("if ($N.$N() != null)", option, LONG_NAME)
        .addStatement("$N.put($N.$N(), $N)", longNames, option, LONG_NAME, option)
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
