package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;

/**
 * The positional constants that {@link OptionType} defines.
 */
enum PositionalType {

  POSITIONAL_REQUIRED(0) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          helper.extractPositionalRequiredMethod,
          helper.context.positionalIndex(j),
          helper.positionalParameter,
          helper.ddIndexParameter,
          helper.option.type, helper.option.enumConstant(j))
          .build();
    }
  },

  POSITIONAL_REQUIRED_INT(0) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          helper.extractPositionalRequiredIntMethod,
          helper.context.positionalIndex(j),
          helper.positionalParameter,
          helper.ddIndexParameter,
          helper.option.type, helper.option.enumConstant(j))
          .build();
    }
  },

  POSITIONAL_OPTIONAL(1) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalOptionalMethod,
          helper.context.positionalIndex(j),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_OPTIONAL_INT(1) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalOptionalIntMethod,
          helper.context.positionalIndex(j),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST(2) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalListMethod,
          helper.context.positionalIndex(j),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST_2(2) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($N, $N)",
          helper.extractPositionalList2Method,
          helper.ddIndexParameter,
          helper.positionalParameter)
          .build();
    }
  };

  final int order;

  PositionalType(int order) {
    this.order = order;
  }

  /**
   * @param j an index in the Option enum
   * @return An expression that returns the value of the parameter specified by {@code j}.
   *     The expression will be used inside a method of class {@link Helper}.
   */
  abstract CodeBlock extractExpression(Helper helper, int j);
}
