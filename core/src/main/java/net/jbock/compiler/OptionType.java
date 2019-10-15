package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.ParameterType;

/**
 * Basic option types
 */
class OptionType {

  private static CodeBlock flag(Helper helper, Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$N).flag()",
        helper.parsersField,
        helper.context.optionType(),
        param.enumConstant())
        .build();
  }

  private static CodeBlock regular(Helper helper, Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$L).value()",
        helper.parsersField,
        helper.context.optionType(),
        param.enumConstant())
        .build();
  }

  private static CodeBlock regularPositional(Helper helper, Param param) {
    return CodeBlock.builder().add(
        "$N.get($L).value()",
        helper.positionalParsersField,
        param.positionalIndex().orElseThrow(AssertionError::new))
        .build();
  }

  private static CodeBlock repeatable(Helper helper, Param param) {
    return CodeBlock.builder().add(
        "$N.get($T.$L).values()",
        helper.parsersField,
        helper.context.optionType(),
        param.enumConstant())
        .build();
  }

  private static CodeBlock repeatablePositional(Helper helper, Param param) {
    return CodeBlock.builder().add(
        "$N.get($L).values()",
        helper.positionalParsersField,
        param.positionalIndex().orElseThrow(AssertionError::new))
        .build();
  }

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   * This expression will evaluate either to a {@link java.util.stream.Stream} or an {@link java.util.Optional}.
   */
  static CodeBlock getStreamExpression(Helper helper, Param param) {
    ParameterType parameterType = param.coercion().parameterType();
    if (param.isPositional()) {
      if (parameterType == ParameterType.REPEATABLE) {
        return repeatablePositional(helper, param);
      } else {
        return regularPositional(helper, param);
      }
    }
    switch (parameterType) {
      case REPEATABLE:
        return repeatable(helper, param);
      case FLAG:
        return flag(helper, param);
      default:
        return regular(helper, param);
    }
  }
}
