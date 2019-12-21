package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.Parameter;

import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl inner class.
 *
 * @see GeneratedClass
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
        .superclass(context.sourceElement());
    for (Parameter param : context.parameters()) {
      spec.addField(param.field());
    }
    return spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(context.parameters().stream()
            .map(this::parameterMethodOverride)
            .collect(Collectors.toList()))
        .build();
  }

  private MethodSpec parameterMethodOverride(Parameter param) {
    return MethodSpec.methodBuilder(param.methodName())
        .returns(param.returnType())
        .addModifiers(param.getAccessModifiers())
        .addStatement("return $N", param.field())
        .build();
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    for (Parameter p : context.parameters()) {
      spec.addStatement("this.$N = $L",
          p.field(), p.coercion().extractExpr());
      spec.addParameter(p.coercion().constructorParam());
    }
    return spec.build();
  }
}
