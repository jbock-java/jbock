package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;

/**
 * Some of the enum constants that {@link OptionType} defines.
 */
enum PositionalType {

  POSITIONAL_REQUIRED() {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          option.extractPositionalRequiredMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter,
          option.type, option.enumConstant(j))
          .build();
    }
  },

  POSITIONAL_REQUIRED_INT() {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          option.extractPositionalRequiredIntMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter,
          option.type, option.enumConstant(j))
          .build();
    }
  },

  POSITIONAL_OPTIONAL() {
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

  POSITIONAL_OPTIONAL_INT() {
    @Override
    CodeBlock extractExpression(Option option, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          option.extractPositionalOptionalIntMethod,
          option.context.positionalIndex(j),
          option.positionalParameter,
          option.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST() {
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

  POSITIONAL_LIST_2() {
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

  /**
   * @param j an index in the Option enum
   * @return An expression that returns the value of the parameter specified by {@code j}.
   *     The expression will be used inside a static method,
   *     which has the parameters listed in {@link Option} (see comment there).
   */
  abstract CodeBlock extractExpression(Option option, int j);
}
