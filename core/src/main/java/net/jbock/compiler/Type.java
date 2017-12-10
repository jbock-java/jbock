package net.jbock.compiler;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STRING;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

/**
 * An enumeration of the enum constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(BOOLEAN, false, false, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N.contains($T.$N)",
          option.flagsParameter, option.type, option.enumConstant(j))
          .build();
    }
  },

  OPTIONAL(OPTIONAL_STRING, false, true, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$T.ofNullable($N.get($T.$L))",
          Optional.class, option.sMapParameter, option.type, option.enumConstant(j))
          .build();
    }
  },

  OPTIONAL_INT(ClassName.get(OptionalInt.class), false, true, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($N, $T.$L)",
          option.extractOptionalIntMethod, option.sMapParameter,
          option.type, option.enumConstant(j))
          .build();
    }
  },

  REQUIRED(STRING, false, true, true) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($N, $T.$L)",
          option.extractRequiredMethod, option.sMapParameter,
          option.type, option.enumConstant(j))
          .build();
    }
  },

  REQUIRED_INT(TypeName.INT, false, true, true) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($N, $T.$L)",
          option.extractRequiredIntMethod, option.sMapParameter,
          option.type, option.enumConstant(j))
          .build();
    }
  },

  REPEATABLE(LIST_OF_STRING, false, true, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N.getOrDefault($T.$L, $T.emptyList())",
          option.optMapParameter, option.type, option.enumConstant(j), Collections.class)
          .build();
    }
  },

  POSITIONAL_REQUIRED(STRING, true, false, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          option.extractPositionalRequiredMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_OPTIONAL(OPTIONAL_STRING, true, false, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          option.extractPositionalOptionalMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST(LIST_OF_STRING, true, false, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          option.extractPositionalListMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST_2(LIST_OF_STRING, true, false, false) {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($N, $N)",
          option.extractPositionalList2Method,
          option.ddIndexParameter,
          option.positionalParameter)
          .build();
    }
  };

  final TypeName returnType;
  final boolean positional;
  final boolean binding;
  final boolean required;

  /**
   * @param j an index in the Option enum
   * @return An expression that returns the value of the parameter specified by {@code j}.
   *     The expression will be used inside a static method,
   *     which has the parameters listed in {@link Option} (see comment there).
   */
  abstract CodeBlock extractExpression(Option option, int j);

  Type(
      TypeName returnType,
      boolean positional,
      boolean binding,
      boolean required) {
    this.returnType = returnType;
    this.positional = positional;
    this.binding = binding;
    this.required = required;
  }
}
