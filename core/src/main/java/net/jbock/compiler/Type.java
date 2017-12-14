package net.jbock.compiler;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.PositionalType.POSITIONAL_LIST;
import static net.jbock.compiler.PositionalType.POSITIONAL_OPTIONAL;
import static net.jbock.compiler.PositionalType.POSITIONAL_OPTIONAL_INT;
import static net.jbock.compiler.PositionalType.POSITIONAL_REQUIRED;
import static net.jbock.compiler.PositionalType.POSITIONAL_REQUIRED_INT;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

/**
 * The non-positional constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(BOOLEAN, null) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N.contains($T.$N)",
          helper.flagsField,
          helper.option.type,
          helper.option.enumConstant(j))
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      return CodeBlock.builder()
          .add("$N", impl.fields.get(j))
          .build();
    }
  },

  OPTIONAL(OPTIONAL_STRING, POSITIONAL_OPTIONAL) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$T.ofNullable($N.get($T.$L))",
          Optional.class,
          helper.sMapField,
          helper.option.type,
          helper.option.enumConstant(j))
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
      return CodeBlock.builder()
          .add("$N.map($N -> '\"' + $N + '\"').orElse(null)",
              impl.fields.get(j), s, s)
          .build();
    }
  },

  OPTIONAL_INT(ClassName.get(OptionalInt.class), POSITIONAL_OPTIONAL_INT) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractOptionalIntMethod,
          helper.option.type,
          helper.option.enumConstant(j))
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      return CodeBlock.builder()
          .add("$N.stream().boxed().findFirst().orElse(null)", impl.fields.get(j))
          .build();
    }
  },

  REQUIRED(STRING, POSITIONAL_REQUIRED) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredMethod,
          helper.option.type,
          helper.option.enumConstant(j))
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      return CodeBlock.builder()
          .add("'\"' + $N + '\"'", impl.fields.get(j))
          .build();
    }
  },

  REQUIRED_INT(TypeName.INT, POSITIONAL_REQUIRED_INT) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N($T.$L)",
          helper.extractRequiredIntMethod,
          helper.option.type,
          helper.option.enumConstant(j))
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      return CodeBlock.builder()
          .add("$N", impl.fields.get(j))
          .build();
    }
  },

  REPEATABLE(LIST_OF_STRING, POSITIONAL_LIST) {
    @Override
    CodeBlock extractExpression(Helper helper, int j) {
      return CodeBlock.builder().add(
          "$N.getOrDefault($T.$L, $T.emptyList())",
          helper.optMapField,
          helper.option.type,
          helper.option.enumConstant(j),
          Collections.class)
          .build();
    }

    @Override
    CodeBlock jsonExpression(Impl impl, int j) {
      ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
      return CodeBlock.builder()
          .add("$N.stream().map($N -> '\"' + $N + '\"').collect($T.joining($S, $S, $S))",
              impl.fields.get(j), s, s, Collectors.class, ", ", "[", "]")
          .build();
    }
  };

  final TypeName returnType;
  final PositionalType positionalType;

  /**
   * @param j an index in the Option enum
   * @return An expression that returns the value of the parameter specified by {@code j}.
   *     The expression will be used inside a static method,
   *     which has the parameters listed in {@link Option} (see comment there).
   */
  abstract CodeBlock extractExpression(Helper helper, int j);

  abstract CodeBlock jsonExpression(Impl impl, int j);

  Type(TypeName returnType, PositionalType positionalType) {
    this.returnType = returnType;
    this.positionalType = positionalType;
  }
}
