package net.jbock.compiler;

import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.List;
import java.util.Optional;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

enum OptionType {

  FLAG(TypeName.BOOLEAN),
  OPTIONAL(ParameterizedTypeName.get(ClassName.get(Optional.class), ClassName.get(String.class))),
  REQUIRED(ClassName.get(String.class)),
  REPEATABLE(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class))),
  OTHER_TOKENS(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class))),
  EVERYTHING_AFTER(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)));

  final TypeName sourceType;

  OptionType(TypeName sourceType) {
    this.sourceType = sourceType;
  }


  static TypeSpec define(ClassName optionTypeClass) {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(optionTypeClass);
    for (OptionType optionType : values()) {
      builder.addEnumConstant(optionType.name());
    }
    return builder.addModifiers(PUBLIC)
        .build();
  }
}
