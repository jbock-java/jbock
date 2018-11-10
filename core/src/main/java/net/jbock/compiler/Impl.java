package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  private final Context context;

  private final Option option;

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
      builder.addStatement("this.$N = $L", field, p.coercion().extractExpr());
      builder.addParameter(p.coercion().constructorParam());
    }
    return builder.build();
  }
}
