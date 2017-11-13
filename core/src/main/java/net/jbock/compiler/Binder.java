package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.STRING_LIST;
import static net.jbock.compiler.Analyser.otherTokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Processor.Context;

final class Binder {

  private final ClassName binderClass;
  private final Option option;
  private final FieldSpec optMap;
  private final FieldSpec sMap;
  private final FieldSpec flags;
  private final FieldSpec rest = FieldSpec.builder(STRING_LIST, "rest")
      .addModifiers(PRIVATE, FINAL)
      .build();
  private final Context context;

  private Binder(ClassName binderClass,
                 Option option,
                 FieldSpec optMap,
                 FieldSpec sMap,
                 FieldSpec flags,
                 Context context) {
    this.binderClass = binderClass;
    this.option = option;
    this.optMap = optMap;
    this.sMap = sMap;
    this.flags = flags;
    this.context = context;
  }

  static Binder create(Analyser analyser) {
    return new Binder(
        analyser.binderClass,
        analyser.option,
        analyser.optMap,
        analyser.sMap,
        analyser.flags,
        analyser.context);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(binderClass)
        .superclass(TypeName.get(context.sourceType.asType()))
        .addFields(Arrays.asList(optMap, sMap, flags, otherTokens, rest))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(bindMethod())
        .addMethod(otherTokensMethod())
        .build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      OptionType optionType = param.optionType();
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.parameterName())
          .addModifiers(PUBLIC, FINAL)
          .returns(optionType.sourceType);
      if (optionType == OptionType.FLAG) {
        builder.addCode("return $N.contains($T.$N)", flags, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OPTIONAL) {
        builder.addCode("return $T.ofNullable($N.get($T.$L))",
            Optional.class, sMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OTHER_TOKENS) {
        builder.addCode("return $N", otherTokens);
      } else if (optionType == OptionType.EVERYTHING_AFTER) {
        builder.addCode("return $N", rest);
      } else {
        builder.addCode("return $N.getOrDefault($T.$L, $T.emptyList())",
            optMap, option.optionClass, option.enumConstant(j), Collections.class);
      }
      result.add(builder.build());
    }
    return result;
  }


    private MethodSpec bindMethod() {
    CodeBlock.Builder args = CodeBlock.builder();
    for (int j = 0; j < context.parameters.size(); j++) {
      if (j > 0) {
        args.add(",\n");
      }
      OptionType optionType = context.parameters.get(j).optionType();
      if (optionType == OptionType.FLAG) {
        args.add("$N.contains($T.$N)", flags, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OPTIONAL) {
        args.add("$T.ofNullable($N.get($T.$L))",
            Optional.class, sMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.OTHER_TOKENS) {
        args.add("$N", otherTokens);
      } else if (optionType == OptionType.EVERYTHING_AFTER) {
        args.add("$N", rest);
      } else {
        args.add("$N.getOrDefault($T.$L, $T.emptyList())",
            optMap, option.optionClass, option.enumConstant(j), Collections.class);
      }
    }
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("return new $T(\n$L)",
        binderClass, args.build());
    return MethodSpec.methodBuilder("bind")
        .addCode(builder.build())
        .addModifiers(PUBLIC)
        .returns(context.returnType())
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
