package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;

import java.util.List;
import java.util.Optional;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

enum OptionType {

  FLAG(TypeName.BOOLEAN, false, false),
  OPTIONAL(ParameterizedTypeName.get(ClassName.get(Optional.class), ClassName.get(String.class)), false, true),
  REQUIRED(ClassName.get(String.class), false, true),
  REPEATABLE(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), false, true),
  OTHER_TOKENS(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), true, false),
  EVERYTHING_AFTER(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), true, false);

  final TypeName sourceType;
  final boolean special;
  final boolean binding;

  private static final FieldSpec SPECIAL = FieldSpec.builder(TypeName.BOOLEAN, "special", PRIVATE, FINAL)
      .build();
  private static final FieldSpec BINDING = FieldSpec.builder(TypeName.BOOLEAN, "binding", PRIVATE, FINAL)
      .build();

  static final MethodSpec IS_SPECIAL = isSpecialMethod();
  static final MethodSpec IS_BINDING = isBindingMethod();

  OptionType(
      TypeName sourceType,
      boolean special,
      boolean binding) {
    this.sourceType = sourceType;
    this.special = special;
    this.binding = binding;
  }


  static TypeSpec define(ClassName optionTypeClass) {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(optionTypeClass);
    for (OptionType optionType : values()) {
      builder.addEnumConstant(optionType.name(),
          anonymousClassBuilder("$L, $L", optionType.special, optionType.binding)
              .build());
    }
    return builder.addModifiers(PUBLIC)
        .addField(SPECIAL)
        .addField(BINDING)
        .addMethod(privateConstructor())
        .addMethod(IS_SPECIAL)
        .addMethod(IS_BINDING)
        .build();
  }

  private static MethodSpec privateConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    ParameterSpec special = ParameterSpec.builder(SPECIAL.type, SPECIAL.name).build();
    ParameterSpec binding = ParameterSpec.builder(BINDING.type, BINDING.name).build();
    builder.addStatement("this.$N = $N", SPECIAL, special);
    builder.addStatement("this.$N = $N", BINDING, binding);
    return builder.addParameter(special).addParameter(binding).build();
  }

  private static MethodSpec isSpecialMethod() {
    return MethodSpec.methodBuilder("isSpecial")
        .addStatement("return $N", SPECIAL)
        .returns(TypeName.BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec isBindingMethod() {
    return MethodSpec.methodBuilder("isBinding")
        .addStatement("return $N", BINDING)
        .returns(TypeName.BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }
}
