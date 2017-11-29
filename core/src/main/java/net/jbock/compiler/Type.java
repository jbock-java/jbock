package net.jbock.compiler;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STRING;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

/**
 * An enumeration of the enum constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(BOOLEAN, false, false) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $N.contains($T.$N)",
          option.flagsParameter, option.type, option.enumConstant(j))
          .build();
    }
  },
  OPTIONAL(OPTIONAL_STRING, false, true) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $T.ofNullable($N.get($T.$L))",
          Optional.class, option.sMapParameter, option.type, option.enumConstant(j))
          .build();
    }
  },
  REQUIRED(STRING, false, true) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $N.get($T.$L)",
          option.sMapParameter, option.type, option.enumConstant(j))
          .build();
    }
  },
  REPEATABLE(LIST_OF_STRING, false, true) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $N.getOrDefault($T.$L, $T.emptyList())",
          option.optMapParameter, option.type, option.enumConstant(j), Collections.class)
          .build();
    }
  },
  OTHER_TOKENS(LIST_OF_STRING, true, false) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $N",
          option.otherTokensParameter)
          .build();
    }
  },
  EVERYTHING_AFTER(LIST_OF_STRING, true, false) {
    @Override
    CodeBlock extractStatement(Option option, int j) {
      return CodeBlock.builder().add(
          "return $N",
          option.restParameter)
          .build();
    }
  };

  final TypeName sourceType;
  final boolean special;
  final boolean binding;

  abstract CodeBlock extractStatement(Option option, int j);

  Type(
      TypeName sourceType,
      boolean special,
      boolean binding) {
    this.sourceType = sourceType;
    this.special = special;
    this.binding = binding;
  }

  static Set<Type> nonBinding() {
    EnumSet<Type> result = EnumSet.noneOf(Type.class);
    for (Type type : values()) {
      if (!type.binding) {
        result.add(type);
      }
    }
    return result;
  }

  static Set<Type> special() {
    EnumSet<Type> result = EnumSet.noneOf(Type.class);
    for (Type type : values()) {
      if (type.special) {
        result.add(type);
      }
    }
    return result;
  }
}
