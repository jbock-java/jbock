package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

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
      builder.addStatement("$N.add($S + $N)",
          joiner,
          jsonKey(param),
          impl.field(param));
      return builder.build();
    }
  },

  OPTIONAL {
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
      if (Constants.OPTIONAL_INT.equals(param.returnType())) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.beginControlFlow("if ($N.isPresent())", impl.field(param))
            .addStatement("$N.add($S + $N.getAsInt())",
                joiner,
                jsonKey(param),
                impl.field(param))
            .endControlFlow()
            .beginControlFlow("else")
            .addStatement("$N.add($S)", joiner, jsonKey(param) + "null")
            .endControlFlow();
        return builder.build();
      } else if (TypeName.INT.equals(param.returnType())) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$N.add($S + $N)",
            joiner,
            jsonKey(param),
            impl.field(param));
        return builder.build();
      } else if (param.required) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$N.add($S + $N + '\"')",
            joiner, jsonKey(param) + '"', impl.field(param));
        return builder.build();
      }
      return CodeBlock.builder()
          .addStatement("$N.add($S + $N.map($N).orElse($S))",
              joiner,
              jsonKey(param),
              impl.field(param),
              impl.option.context.quoteParam(),
              "null")
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
      map.put("field", impl.field(param));
      map.put("mapper", impl.option.context.quoteParam());
      map.put("collector", impl.option.context.toArrayParam());
      String format = "$joiner:N.add(" +
          "$key:S + " +
          "$field:N.stream()$Z" +
          ".map($mapper:N)$Z" +
          ".collect($collector:N))";
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
