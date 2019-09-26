package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;

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
    if (param.isPositional()) {
      return CodeBlock.builder().add(
          "$T.$L.value($N)",
          helper.context.optionType(),
          param.enumConstant(),
          helper.positionalParameter)
          .build();
    } else {
      return CodeBlock.builder().add(
          "$N.get($T.$L).value()",
          helper.parsersField,
          helper.context.optionType(),
          param.enumConstant())
          .build();
    }
  }

  private static CodeBlock repeatable(Helper helper, Param param) {
    if (param.isPositional()) {
      return CodeBlock.builder().add(
          "$T.$L.values($N)",
          helper.context.optionType(),
          param.enumConstant(),
          helper.positionalParameter)
          .build();
    } else {
      return CodeBlock.builder().add(
          "$N.get($T.$L).values()",
          helper.parsersField,
          helper.context.optionType(),
          param.enumConstant())
          .build();
    }
  }

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   * This expression will evaluate either to a {@link java.util.stream.Stream} or an {@link java.util.Optional}.
   */
  static CodeBlock getStreamExpression(Helper helper, Param param) {
    switch (param.coercion().parameterType()) {
      case REPEATABLE:
        return repeatable(helper, param);
      case FLAG:
        return flag(helper, param);
      default:
        return regular(helper, param);
    }
  }
}
