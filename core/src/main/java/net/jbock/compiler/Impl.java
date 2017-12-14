package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  final List<FieldSpec> fields;

  final Option option;

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
      fields.add(FieldSpec.builder(param.paramType.returnType, param.methodName())
          .addModifiers(PRIVATE, FINAL)
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
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(implConstructor())
        .addMethod(toStringMethod())
        .addMethods(bindMethods());
    return builder.build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(option.context.parameters.size());
    for (int j = 0; j < option.context.parameters.size(); j++) {
      Param param = option.context.parameters.get(j);
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.methodName())
          .addModifiers(PUBLIC)
          .addAnnotation(Override.class)
          .returns(param.paramType.returnType)
          .addStatement("return $N", fields.get(j));
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    for (FieldSpec field : fields) {
      ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
      if (field.type.isPrimitive()) {
        builder.addStatement("this.$N = $N", field, param);
      } else {
        builder.addStatement("this.$N = requireNonNull($N)", field, param);
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

    for (int i = 0; i < option.context.parameters.size(); i++) {
      Param param = option.context.parameters.get(i);
      builder.addCode(param.paramType.jsonStatement(this, joiner, i));
    }
    builder.addStatement("return $N.toString()", joiner);

    return builder.addModifiers(PUBLIC)
        .addAnnotation(Override.class)
        .returns(STRING)
        .build();
  }
}
