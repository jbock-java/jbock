package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Defines the option type enum
 */
enum OptionType {

  FLAG(null, false) {
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

    @Override
    TypeName returnType(Param param) {
      return BOOLEAN;
    }

    @Override
    Stream<TypeName> returnTypes() {
      return Stream.of(BOOLEAN);
    }
  },

  OPTIONAL(PositionalOrder.OPTIONAL, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      if (param.isPositional()) {
        return CodeBlock.builder().add(
            "$N($L, $N)",
            helper.extractPositionalOptionalMethod,
            helper.context.positionalIndex(param.index),
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
      CodeBlock.Builder builder = CodeBlock.builder();
      builder.beginControlFlow("if ($N.isPresent())", impl.field(param))
          .addStatement("$N.add($S + $N.get() + '\"')",
              joiner,
              jsonKey(param) + '"',
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

  OPTIONAL_INT(PositionalOrder.OPTIONAL, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      if (param.isPositional()) {
        return CodeBlock.builder().add(
            "$N($L, $N)",
            helper.extractPositionalOptionalIntMethod,
            helper.context.positionalIndex(param.index),
            helper.positionalParameter)
            .build();
      } else {
        return CodeBlock.builder().add(
            "$N.get($T.$L).value()$L",
            helper.parsersField,
            helper.context.optionType(),
            param.enumConstant(),
            mapToInt())
            .build();
      }
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

  REQUIRED(PositionalOrder.REQUIRED, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      if (param.isPositional()) {
        return CodeBlock.builder().add(
            "$N($L, $N, $T.$L)",
            helper.extractPositionalRequiredMethod,
            helper.context.positionalIndex(param.index),
            helper.positionalParameter,
            helper.context.optionType(),
            param.enumConstant())
            .build();
      } else {
        return CodeBlock.builder().add(
            "$N.get($T.$L).value()$L",
            helper.parsersField,
            helper.context.optionType(),
            param.enumConstant(),
            orElseThrowMissing(helper.context.optionType(), param.enumConstant()))
            .build();
      }
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

  REQUIRED_INT(PositionalOrder.REQUIRED, true) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      if (param.isPositional()) {
        return CodeBlock.builder().add(
            "$N($L, $N, $T.$L)",
            helper.extractPositionalRequiredIntMethod,
            helper.context.positionalIndex(param.index),
            helper.positionalParameter,
            helper.context.optionType(),
            param.enumConstant())
            .build();
      } else {
        return CodeBlock.builder().add(
            "$N.get($T.$L).value()$L$L",
            helper.parsersField,
            helper.context.optionType(),
            param.enumConstant(),
            mapToInt(),
            orElseThrowMissing(helper.context.optionType(), param.enumConstant()))
            .build();
      }
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

  REPEATABLE(PositionalOrder.LIST, false) {
    @Override
    CodeBlock extractExpression(Helper helper, Param param) {
      if (param.isPositional()) {
        return CodeBlock.builder().add(
            "$N($L, $N)",
            helper.extractPositionalListMethod,
            helper.context.positionalIndex(param.index),
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
  final PositionalOrder positionalOrder;

  /**
   * @return An expression that extracts the value of the given param from the helper state.
   */
  abstract CodeBlock extractExpression(Helper helper, Param param);

  abstract CodeBlock jsonStatement(Impl impl, ParameterSpec joiner, Param param);

  OptionType(PositionalOrder positionalOrder, boolean required) {
    this.positionalOrder = positionalOrder;
    this.required = required;
  }

  private static String jsonKey(Param param) {
    return '"' + param.methodName() + "\":";
  }

  abstract TypeName returnType(Param param);

  abstract Stream<TypeName> returnTypes();

  static TypeSpec define(Context context) {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(context.optionTypeType());
    for (OptionType optionType : context.nonpositionalParamTypes) {
      builder.addEnumConstant(optionType.name()).build();
    }
    for (OptionType optionType : context.positionalParamTypes) {
      builder.addEnumConstant(optionType.name()).build();
    }
    return builder.addModifiers(PRIVATE).build();
  }

  private static CodeBlock missingRequiredOptionMessage(ClassName className, String enumConstant) {
    return CodeBlock.builder()
        .add("$T.format($S,$W$T.$L, $T.$L.describeParam($S))",
            String.class,
            "Missing required option: %s (%s)",
            className, enumConstant,
            className, enumConstant,
            "")
        .build();
  }

  private static CodeBlock orElseThrowMissing(ClassName className, String enumConstant) {
    return CodeBlock.builder()
        .add("\n.orElseThrow(() -> new $T($L))", IllegalArgumentException.class,
            missingRequiredOptionMessage(className, enumConstant))
        .build();
  }

  private static CodeBlock mapToInt() {
    return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
  }
}
