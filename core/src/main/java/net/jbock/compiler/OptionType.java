package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import static javax.lang.model.element.Modifier.PUBLIC;

enum OptionType {

  FLAG, AT_MOST_ONCE, REPEATABLE, OTHER_TOKENS, EVERYTHING_AFTER;

  static TypeSpec define(ClassName optionTypeClass) {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(optionTypeClass);
    for (OptionType optionType : values()) {
      builder.addEnumConstant(optionType.name());
    }
    return builder.addModifiers(PUBLIC)
        .build();
  }
}
