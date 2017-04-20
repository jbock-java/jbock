package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;

final class Binder {

  private final ClassName binderClass;
  private final Option option;
  private final ClassName argumentClass;
  private final FieldSpec optMap;
  private final FieldSpec free;
  private final FieldSpec value;
  private final ExecutableElement constructor;

  private Binder(ClassName binderClass,
                 Option option,
                 ClassName argumentClass,
                 FieldSpec optMap,
                 FieldSpec free,
                 FieldSpec value, ExecutableElement constructor) {
    this.binderClass = binderClass;
    this.option = option;
    this.argumentClass = argumentClass;
    this.optMap = optMap;
    this.free = free;
    this.value = value;
    this.constructor = constructor;
  }

  static Binder create(ClassName binderClass,
                       Option optionClass,
                       ClassName argumentClass,
                       FieldSpec optMap,
                       FieldSpec free,
                       FieldSpec value,
                       ExecutableElement constructor) {
    return new Binder(binderClass, optionClass,
        argumentClass,
        optMap, free, value, constructor);
  }

  TypeSpec define() {
    TypeName originalClass = TypeName.get(constructor.getEnclosingElement().asType());
    return TypeSpec.classBuilder(binderClass)
        .addFields(Arrays.asList(optMap, free))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addMethod(privateConstructor())
        .addMethod(bindMethod())
        .addMethod(argumentsMethod())
        .addMethod(freeMethod())
        .addJavadoc("Parsed arguments, ready to be passed to the constructor.\n\n" +
                "@see $T#$T($L)\n", originalClass, originalClass,
            Option.constructorArgumentsForJavadoc(constructor))
        .build();
  }

  private MethodSpec bindMethod() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec a = ParameterSpec.builder(argumentClass, "a").build();
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      OptionType optionType = Names.create(variableElement).optionType;
      if (optionType == OptionType.FLAG) {
        builder.add("$N.containsKey($T.$N)", optMap, option.optionClass, option.enumConstant(j));
      } else if (optionType == OptionType.STRING) {
        builder.add("$N.getOrDefault($T.$L, $T.emptyList()).stream()\n",
            optMap, option.optionClass, option.enumConstant(j), Collections.class)
            .add("        .map($N -> $N.$N)\n", a, a, value)
            .add("        .findFirst().orElse(null)");
      } else if (optionType == OptionType.OTHER_TOKENS) {
        builder.add("$N", free);
      } else {
        builder.add("$N.getOrDefault($T.$L, $T.emptyList()).stream()\n",
            optMap, option.optionClass, option.enumConstant(j), Collections.class)
            .add("        .map($N -> $N.$N)\n", a, a, value)
            .add("        .collect($T.toList())", Collectors.class);
      }
    }
    builder.add(");\n");
    TypeName originalClass = TypeName.get(constructor.getEnclosingElement().asType());
    return MethodSpec.methodBuilder("bind")
        .addCode(builder.build())
        .addModifiers(PUBLIC)
        .addJavadoc("Invokes the constructor.\n" +
            "\n" +
            "@return an instance of {@link $T}\n", originalClass)
        .returns(ClassName.get(asType(constructor.getEnclosingElement())))
        .build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec optMap = ParameterSpec.builder(this.optMap.type, this.optMap.name)
        .build();
    ParameterSpec trash = ParameterSpec.builder(this.free.type, this.free.name)
        .build();
    builder.addStatement("this.$N = $T.unmodifiableList($N)", this.free, Collections.class, trash);
    builder.addStatement("this.$N = $T.unmodifiableMap($N)", this.optMap, Collections.class, optMap);
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(optMap, trash))
        .addCode(builder.build())
        .addModifiers(PRIVATE)
        .build();
  }

  private MethodSpec argumentsMethod() {
    return MethodSpec.methodBuilder("arguments")
        .addStatement("return $N", optMap)
        .addJavadoc("Early access to the parsing results,\n" +
            "for manual inspection before invoking {@link this#bind()}\n" +
            "\n" +
            "@return an unmodifiable map\n")
        .returns(optMap.type)
        .addModifiers(PUBLIC)
        .build();
  }

  private MethodSpec freeMethod() {
    return MethodSpec.methodBuilder("free")
        .addStatement("return $N", free)
        .addJavadoc("Collection of all unbound tokens.\n" +
            "\n" +
            "@return tokens that the parser ignored, an unmodifiable list\n")
        .returns(free.type)
        .addModifiers(PUBLIC)
        .build();
  }
}
