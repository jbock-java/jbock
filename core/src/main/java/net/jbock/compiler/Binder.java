package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.STRING_LIST;
import static net.jbock.compiler.Analyser.otherTokens;

final class Binder {

  private final ClassName binderClass;
  private final Option option;
  private final FieldSpec optMap;
  private final FieldSpec sMap;
  private final FieldSpec flags;
  private final FieldSpec rest = FieldSpec.builder(STRING_LIST, "rest")
      .addModifiers(PRIVATE, FINAL)
      .build();
  private final Processor.Constructor constructor;

  private Binder(ClassName binderClass,
                 Option option,
                 FieldSpec optMap,
                 FieldSpec sMap,
                 FieldSpec flags, Processor.Constructor constructor) {
    this.binderClass = binderClass;
    this.option = option;
    this.optMap = optMap;
    this.sMap = sMap;
    this.flags = flags;
    this.constructor = constructor;
  }

  static Binder create(Analyser analyser) {
    return new Binder(
        analyser.binderClass,
        analyser.option,
        analyser.optMap,
        analyser.sMap,
        analyser.flags,
        analyser.constructor);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(binderClass)
        .addFields(Arrays.asList(optMap, sMap, flags, otherTokens, rest))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(bindMethod())
        .addMethod(otherTokensMethod())
        .build();
  }

  private MethodSpec bindMethod() {
    CodeBlock.Builder builder = CodeBlock.builder();
    for (int j = 0; j < constructor.parameters.size(); j++) {
      if (j > 0) {
        builder.add(",\n");
      }
      OptionType optionType = constructor.parameters.get(j).optionType();
      if (optionType == OptionType.FLAG) {
        builder.add("$N.contains($T.$N)", flags, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OPTIONAL) {
        builder.add("$T.ofNullable($N.get($T.$L))",
            Optional.class, sMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OTHER_TOKENS) {
        builder.add("$N", otherTokens);
      } else if (optionType == OptionType.EVERYTHING_AFTER) {
        builder.add("$N", rest);
      } else {
        builder.add("$N.getOrDefault($T.$L, $T.emptyList())",
            optMap, option.optionClass, option.enumConstant(j), Collections.class);
      }
    }
    return MethodSpec.methodBuilder("bind")
        .addStatement("return new $T(\n$L)",
            constructor.enclosingType, builder.build())
        .addExceptions(constructor.thrownTypes)
        .addModifiers(PUBLIC)
        .returns(constructor.enclosingType)
        .build();
  }

  private MethodSpec otherTokensMethod() {
    return MethodSpec.methodBuilder("otherTokens")
        .addStatement("return $N", otherTokens)
        .returns(otherTokens.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec optMap = ParameterSpec.builder(this.optMap.type, this.optMap.name)
        .build();
    ParameterSpec sMap = ParameterSpec.builder(this.sMap.type, this.sMap.name)
        .build();
    ParameterSpec flags = ParameterSpec.builder(this.flags.type, this.flags.name)
        .build();
    ParameterSpec otherTokens = ParameterSpec.builder(
        Analyser.otherTokens.type, Analyser.otherTokens.name).build();
    ParameterSpec esc = ParameterSpec.builder(this.rest.type, this.rest.name).build();
    builder.addStatement("this.$N = $N", this.optMap, optMap);
    builder.addStatement("this.$N = $N", this.sMap, sMap);
    builder.addStatement("this.$N = $N", this.flags, flags);
    builder.addStatement("this.$N = $N", Analyser.otherTokens, otherTokens);
    builder.addStatement("this.$N = $N", this.rest, esc);
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(optMap, sMap, flags, otherTokens, esc))
        .addCode(builder.build())
        .addModifiers(PRIVATE)
        .build();
  }
}
