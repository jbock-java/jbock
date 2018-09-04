package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.PositionalType.POSITIONAL_LIST;
import static net.jbock.compiler.PositionalType.POSITIONAL_OPTIONAL;
import static net.jbock.compiler.PositionalType.POSITIONAL_OPTIONAL_INT;
import static net.jbock.compiler.PositionalType.POSITIONAL_REQUIRED;
import static net.jbock.compiler.PositionalType.POSITIONAL_REQUIRED_INT;
import static net.jbock.compiler.Util.optionalOf;

/**
 * The non-positional constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(null, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N.contains($T.$N)",
          helper.flagsField,
          helper.option.type,
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

    @Override
    TypeName returnType(Param param) {
      return BOOLEAN;
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(BOOLEAN);
    }
  },

  OPTIONAL(POSITIONAL_OPTIONAL, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$T.ofNullable($N.get($T.$L))",
          Optional.class,
          helper.sMapField,
          helper.option.type,
          param.enumConstant())
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.beginControlFlow("if ($N.isPresent())", impl.field(param))
          .addStatement("$N.add($S + $N.get() + '\"')",
              joiner,
              jsonKey(param) +  '"',
              impl.field(param))
          .endControlFlow()
          .beginControlFlow("else")
          .addStatement("$N.add($S)", joiner, jsonKey(param) + "null")
          .endControlFlow();
      return builder.build();
    }

    @Override
    TypeName returnType(Param param) {
      return optionalOf(STRING);
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(optionalOf(STRING));
    }
  },

  OPTIONAL_INT(POSITIONAL_OPTIONAL_INT, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractOptionalIntMethod,
          helper.option.type,
          param.enumConstant())
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
          .addStatement("$N.add($S)", joiner, jsonKey(param) + "null")
          .endControlFlow();
      return builder.build();
    }

    @Override
    TypeName returnType(Param param) {
      return ClassName.get(OptionalInt.class);
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(ClassName.get(OptionalInt.class));
    }
  },

  REQUIRED(POSITIONAL_REQUIRED, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredMethod,
          helper.option.type,
          param.enumConstant())
          .build();
    }

    @Override
    CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param) {
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.addStatement("$N.add($S + $N + '\"')",
          joiner, jsonKey(param) + '"', impl.field(param));
      return builder.build();
    }

    @Override
    TypeName returnType(Param param) {
      return STRING;
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(STRING);
    }
  },

  REQUIRED_INT(POSITIONAL_REQUIRED_INT, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredIntMethod,
          helper.option.type,
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

    @Override
    TypeName returnType(Param param) {
      return TypeName.INT;
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(TypeName.INT);
    }
  },

  REPEATABLE(POSITIONAL_LIST, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      return CodeBlock.builder().add(
          "$N.getOrDefault($T.$L, $T.emptyList())",
          helper.optMapField,
          helper.option.type,
          param.enumConstant(),
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

    @Override
    TypeName returnType(Param param) {
      return param.array ? STRING_ARRAY : LIST_OF_STRING;
    }


    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(STRING_ARRAY, LIST_OF_STRING);
    }
  };

  final boolean required;
  final PositionalType positionalType;

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);

  abstract CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param);

  Type(PositionalType positionalType, boolean required) {
    this.positionalType = positionalType;
    this.required = required;
  }

  private static String jsonKey(Param param) {
    return '"' + param.methodName() + "\":";
  }

  abstract TypeName returnType(Param param);

  abstract Stream<TypeName> returnTypes();
}
