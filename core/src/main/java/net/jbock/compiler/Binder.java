package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Analyser.STRING_LIST;
import static net.jbock.compiler.Analyser.otherTokens;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.lang.model.element.ElementKind;
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
        .addFields(Arrays.asList(optMap, sMap, flags, otherTokens, rest))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(bindMethod())
        .addMethod(otherTokensMethod())
        .build();
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
    if (context.executableElement.getKind() == ElementKind.CONSTRUCTOR) {
      builder.addStatement("return new $T(\n$L)",
          context.enclosingType, args.build());
    } else {
      builder.addStatement("return $T.$L(\n$L)",
          context.enclosingType,
          context.executableElement.getSimpleName().toString(), args.build());
    }
    return MethodSpec.methodBuilder("bind")
        .addCode(builder.build())
        .addExceptions(context.thrownTypes)
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
