package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;

/**
 * The positional constants that {@link OptionType} defines.
 */
enum PositionalType {

  POSITIONAL_REQUIRED(0) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          helper.extractPositionalRequiredMethod,
          helper.context.positionalIndex(param.index),
          helper.positionalParameter,
          helper.ddIndexParameter,
          helper.option.type, helper.option.enumConstant(param))
          .build();
    }
  },

  POSITIONAL_REQUIRED_INT(0) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N, $T.$L)",
          helper.extractPositionalRequiredIntMethod,
          helper.context.positionalIndex(param.index),
          helper.positionalParameter,
          helper.ddIndexParameter,
          helper.option.type,
          helper.option.enumConstant(param))
          .build();
    }
  },

  POSITIONAL_OPTIONAL(1) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalOptionalMethod,
          helper.context.positionalIndex(param.index),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_OPTIONAL_INT(1) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalOptionalIntMethod,
          helper.context.positionalIndex(param.index),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST(2) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($L, $N, $N)",
          helper.extractPositionalListMethod,
          helper.context.positionalIndex(param.index),
          helper.positionalParameter,
          helper.ddIndexParameter)
          .build();
    }
  },

  POSITIONAL_LIST_2(2) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($N, $N)",
          helper.extractPositionalList2Method,
          helper.ddIndexParameter,
          helper.positionalParameter)
          .build();
    }
  };

  // order of methods must be ascending in the sourceType
  final int order;

  PositionalType(int order) {
    this.order = order;
  }

  /**
   * @return An expression that extracts the value of the given param
   *     from the positional and ddIndex params.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);
}
