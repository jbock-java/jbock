package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.util.*;

import static javax.lang.model.element.Modifier.*;
import static net.jbock.compiler.Constants.STRING;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  private final Option option;

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
    for (int j = 0; j < option.context.parameters.size(); j++) {
      Param param = option.context.parameters.get(j);
      fields.add(FieldSpec.builder(param.paramType == Type.REPEATABLE ? Constants.LIST_OF_STRING : param.returnType(), param.methodName())
          .addModifiers(FINAL)
          .build());
    }
    return new Impl(
        implType,
        option,
        fields);
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
      if (param.paramType == Type.REPEATABLE && param.array) {
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
      ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
      if (field.type.isPrimitive()) {
        builder.addStatement("this.$N = $N", field, param);
      } else if (p.paramType == Type.REPEATABLE) {
        builder.addStatement("this.$N = $T.unmodifiableList($N)", field, Collections.class, param);
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

    for (Param param : option.context.parameters) {
      builder.addCode(param.paramType.jsonStatement(this, joiner, param));
    }
    builder.addStatement("return $N.toString()", joiner);

    return builder.addModifiers(PUBLIC)
        .addAnnotation(Override.class)
        .returns(STRING)
        .build();
  }

  FieldSpec field(Param param) {
    return fields.get(param.index);
  }
}
