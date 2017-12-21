package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;

/**
 * The positional constants that {@link OptionType} defines.
 */
enum PositionalType {

  POSITIONAL_REQUIRED(PositionalOrder.REQUIRED) {
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

  POSITIONAL_REQUIRED_INT(PositionalOrder.REQUIRED) {
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

  POSITIONAL_OPTIONAL(PositionalOrder.OPTIONAL) {
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

  POSITIONAL_OPTIONAL_INT(PositionalOrder.OPTIONAL) {
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

  POSITIONAL_LIST(PositionalOrder.LIST) {
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

  POSITIONAL_LIST_2(PositionalOrder.LIST) {
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

  // order of positional methods must be ascending in the sourceType
  final PositionalOrder order;

  PositionalType(PositionalOrder order) {
    this.order = order;
  }

  /**
   * @return An expression that extracts the value of the given param
   *     from the positional and ddIndex params.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);
}
