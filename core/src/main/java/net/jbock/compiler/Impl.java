package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.util.ArrayList;
import java.util.List;
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

  private final Context context;

  private final List<FieldSpec> fields;

  private Impl(
      ClassName type,
      Context context,
      List<FieldSpec> fields) {
    this.type = type;
    this.context = context;
    this.fields = fields;
  }

  static Impl create(
      Context context,
      ClassName implType) {
    List<FieldSpec> fields = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      fields.add(FieldSpec.builder(param.paramType.returnType, param.methodName())
          .addModifiers(PRIVATE, FINAL)
          .build());
    }
    return new Impl(
        implType,
        context,
        fields);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type);
    builder.superclass(TypeName.get(context.sourceType.asType()))
        .addFields(fields)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(implConstructor())
        .addMethods(bindMethods());
    return builder.build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
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
}
