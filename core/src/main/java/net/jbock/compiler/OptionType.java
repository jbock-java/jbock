package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;

/**
 * Basic option types
 */
enum OptionType {

  FLAG {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N.get($T.$N).flag()",
          helper.parsersField,
          helper.context.optionType(),
          param.enumConstant())
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.addStatement("$N.add($S + $L)",
          joiner,
          jsonKey(param),
          param.coercion().jsonExpr(param.field()));
      return builder.build();
    }
  },

  REGULAR {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
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

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock valueExpr;
      if (param.required || param.coercion().special()) {
        valueExpr = param.coercion().jsonExpr(param.field());
      } else {
        valueExpr = CodeBlock.builder()
            .add("$N$L.orElse($S)",
                param.field(),
                param.coercion().mapJsonExpr(param.field()),
                "null")
            .build();
      }
      return CodeBlock.builder()
          .addStatement("$N.add($S + $L)",
              joiner,
              jsonKey(param),
              valueExpr)
          .build();
    }
  },

  REPEATABLE {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
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

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock valueExpr = CodeBlock.builder()
          .add("$N.stream()$L.collect(toArray)",
              param.field(), param.coercion().mapJsonExpr(param.field())).build();
      return CodeBlock.builder()
          .addStatement("$N.add($S + $L)",
              joiner,
              jsonKey(param),
              valueExpr)
          .build();
    }
  };

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);

  abstract CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param);

  private static String jsonKey(Param param) {
    return '"' + param.methodName() + "\":";
  }
}
