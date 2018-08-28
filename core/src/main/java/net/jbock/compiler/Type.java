package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.*;
import java.util.stream.Collectors;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.PositionalType.*;
import static net.jbock.compiler.Util.optionalOf;

/**
 * The non-positional constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(BOOLEAN, null, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N.contains($T.$N)",
          helper.flagsField,
          helper.option.type,
          helper.option.enumConstant(param))
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

  OPTIONAL(optionalOf(STRING), POSITIONAL_OPTIONAL, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$T.ofNullable($N.get($T.$L))",
          Optional.class,
          helper.sMapField,
          helper.option.type,
          helper.option.enumConstant(param))
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.beginControlFlow("if ($N.isPresent())", impl.field(param))
          .addStatement("$N.add($S + '\"' + $N.get() + '\"')",
              joiner,
              jsonKey(param),
              impl.field(param))
          .endControlFlow()
          .beginControlFlow("else")
          .addStatement("$N.add($S + null)",
              joiner,
              jsonKey(param))
          .endControlFlow();
      return builder.build();
    }
  },

  OPTIONAL_INT(ClassName.get(OptionalInt.class), POSITIONAL_OPTIONAL_INT, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractOptionalIntMethod,
          helper.option.type,
          helper.option.enumConstant(param))
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.beginControlFlow("if ($N.isPresent())", impl.field(param))
          .addStatement("$N.add($S + $N.getAsInt())",
              joiner,
              jsonKey(param),
              impl.field(param))
          .endControlFlow()
          .beginControlFlow("else")
          .addStatement("$N.add($S + null)",
              joiner,
              jsonKey(param))
          .endControlFlow();
      return builder.build();
    }
  },

  REQUIRED(STRING, POSITIONAL_REQUIRED, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredMethod,
          helper.option.type,
          helper.option.enumConstant(param))
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.addStatement("$N.add($S + '\"' + $N + '\"')",
          joiner,
          jsonKey(param),
          impl.field(param));
      return builder.build();
    }
  },

  REQUIRED_INT(TypeName.INT, POSITIONAL_REQUIRED_INT, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredIntMethod,
          helper.option.type,
          helper.option.enumConstant(param))
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

  REPEATABLE(LIST_OF_STRING, POSITIONAL_LIST, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N.getOrDefault($T.$L, $T.emptyList())",
          helper.optMapField,
          helper.option.type,
          helper.option.enumConstant(param),
          Collections.class)
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
      CodeBlock.Builder builder = CodeBlock.builder();
      CodeBlock mapper = CodeBlock.builder()
          .add("$N -> '\"' + $N + '\"'", s, s)
          .build();
      CodeBlock collector = CodeBlock.builder()
          .add("$T.joining($S, $S, $S)", Collectors.class, ",", "[", "]")
          .build();
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("joiner", joiner);
      map.put("key", jsonKey(param));
      map.put("field", impl.field(param));
      map.put("mapper", mapper);
      map.put("collector", collector);
      return builder.addStatement(
          "$L", CodeBlock.builder().addNamed("$joiner:N.add($key:S + $field:N.stream()$Z.map($mapper:L)$Z.collect($collector:L))", map).build())
          .build();
    }
  };

  final boolean required;
  final TypeName returnType;
  final PositionalType positionalType;

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);

  abstract CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param);

  Type(TypeName returnType, PositionalType positionalType, boolean required) {
    this.returnType = returnType;
    this.positionalType = positionalType;
    this.required = required;
  }

  private static String jsonKey(Param param) {
    return '"' + param.methodName() + "\":";
  }
}
