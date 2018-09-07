package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;

import java.util.LinkedHashMap;
import java.util.Map;

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
            .add("$N.map($L).orElse($S)",
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
      CodeBlock.Builder builder = CodeBlock.builder();
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("joiner", joiner);
      map.put("key", jsonKey(param));
      map.put("mapExpr", param.coercion().mapJsonExpr(param.field()));
      map.put("field", param.field());
      String format = "$joiner:N.add(" +
          "$key:S + " +
          "$field:N.stream()$Z" +
          ".map($mapExpr:L)$Z" +
          ".collect(toArray))";
      return builder.addStatement(
          "$L", CodeBlock.builder().addNamed(
              format, map).build())
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
