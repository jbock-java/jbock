package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.OptionType.REPEATABLE;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  final Option option;

  private final List<FieldSpec> fields;

  private Impl(
      ClassName type,
      Option option,
      List<FieldSpec> fields) {
    this.type = type;
    this.option = option;
    this.fields = fields;
  }

  static Impl create(
      Option option,
      ClassName implType) {
    List<FieldSpec> fields = new ArrayList<>(option.context.parameters.size());
    for (Param param : option.context.parameters) {
      fields.add(param.field());
    }
    return new Impl(implType, option, fields);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type);
    builder.superclass(TypeName.get(option.context.sourceType.asType()))
        .addFields(fields)
        .addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(bindMethods());
    if (option.context.generateToString) {
      builder.addMethod(toStringMethod());
    }
    return builder.build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(option.context.parameters.size());
    for (int j = 0; j < option.context.parameters.size(); j++) {
      Param param = option.context.parameters.get(j);
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.methodName())
          .addAnnotation(Override.class)
          .returns(param.returnType());
      if (param.paramType == REPEATABLE && param.array) {
        builder.addStatement("return $N.toArray(new $T[$N.size()])", fields.get(j), STRING, fields.get(j));
      } else {
        builder.addStatement("return $N", fields.get(j));
      }
      if (param.sourceMethod.getModifiers().contains(PUBLIC)) {
        builder.addModifiers(PUBLIC);
      }
      if (param.sourceMethod.getModifiers().contains(PROTECTED)) {
        builder.addModifiers(PROTECTED);
      }
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    for (int i = 0; i < fields.size(); i++) {
      Param p = option.context.parameters.get(i);
      FieldSpec field = fields.get(i);
      TypeName type;
      // optional impl fields always use Optional, so all primitive optionals get special treatment here
      if (p.isOptionalInt()) {
        type = optionalOf(TypeName.get(Integer.class));
      } else {
        type = field.type;
      }
      ParameterSpec param = ParameterSpec.builder(type, field.name).build();
      if (field.type.isPrimitive()) {
        builder.addStatement("this.$N = $N", field, param);
      } else if (p.paramType == REPEATABLE) {
        builder.addStatement("this.$N = $T.unmodifiableList($N)", field, Collections.class, param);
      } else if (p.isOptionalInt()) {
        builder.addStatement("this.$N = mapOptionalInt($N)", field, param);
      } else {
        builder.addStatement("this.$N = $T.requireNonNull($N)", field, Objects.class, param);
      }
      builder.addParameter(param);
    }
    return builder.build();
  }

  private MethodSpec toStringMethod() {
    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("toString");
    builder.addStatement("$T $N = new $T($S, $S, $S)",
        StringJoiner.class, joiner, StringJoiner.class, ", ", "{", "}");

    if (option.context.positionalParamTypes.contains(REPEATABLE) ||
        option.context.nonpositionalParamTypes.contains(REPEATABLE) ||
        option.context.nonpositionalParamTypes.contains(OptionType.REGULAR) ||
        option.context.positionalParamTypes.contains(OptionType.REGULAR)) {
      ParameterSpec quote = option.context.quoteParam();
      ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
      builder.addStatement("$T $N = $N -> $T.format($S, $N)",
          quote.type, quote, s, String.class, "\"%s\"", s);
    }

    if (option.context.positionalParamTypes.contains(REPEATABLE) ||
        option.context.nonpositionalParamTypes.contains(REPEATABLE)) {
      ParameterSpec collect = option.context.toArrayParam();
      builder.addStatement("$T $N = $T.joining($S, $S, $S)",
          collect.type, collect, Collectors.class, ",", "[", "]");
    }

    for (Param param : option.context.parameters) {
      builder.addCode(param.paramType.jsonStatement(this, joiner, param));
    }
    builder.addStatement("return $N.toString()", joiner);

    return builder.addModifiers(PUBLIC)
        .addAnnotation(Override.class)
        .returns(STRING)
        .build();
  }
}
