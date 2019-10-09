package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
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

  private Impl(
      Context context) {
    this.context = context;
  }

  static Impl create(
      Context context) {
    return new Impl(context);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.implType())
        .superclass(TypeName.get(context.sourceElement().asType()));
    for (Param param : context.parameters()) {
      spec.addField(param.field());
    }
    spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(bindMethods());
    return spec.build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters().size());
    for (Param param : context.parameters()) {
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.methodName())
          .addAnnotation(Override.class)
          .returns(param.returnType());
      builder.addStatement("return $N", param.field());
      if (param.isPublic()) {
        builder.addModifiers(PUBLIC);
      }
      if (param.isProtected()) {
        builder.addModifiers(PROTECTED);
      }
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    for (Param p : context.parameters()) {
      FieldSpec field = p.field();
      CodeBlock extractExpr = p.coercion().extractExpr();
      builder.addStatement("this.$N = $L", field, extractExpr);
      ParameterSpec constructorParam = p.coercion().constructorParam();
      builder.addParameter(constructorParam);
    }
    return builder.build();
  }
}
