package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.OptionType.REPEATABLE;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final Context context;

  final Option option;

  private Impl(
      Context context,
      Option option) {
    this.context = context;
    this.option = option;
  }

  static Impl create(
      Context context,
      Option option) {
    return new Impl(context, option);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.implType())
        .superclass(TypeName.get(context.sourceType.asType()));
    for (Param param : option.context.parameters) {
      spec.addField(param.field());
    }
    spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(bindMethods());
    if (option.context.generateToString) {
      spec.addMethod(toStringMethod());
    }
    return spec.build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(option.context.parameters.size());
    for (Param param : option.context.parameters) {
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.methodName())
          .addAnnotation(Override.class)
          .returns(param.returnType());
      builder.addStatement("return $N", param.field());
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
    for (Param p : option.context.parameters) {
      FieldSpec field = p.field();
      ParameterSpec param = ParameterSpec.builder(p.coercion().paramType(), field.name).build();
      builder.addStatement("this.$N = $L", field, p.coercion().extract());
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
