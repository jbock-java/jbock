package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.Param;

import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
public final class Impl {

  private final Context context;

  private Impl(
      Context context) {
    this.context = context;
  }

  public static Impl create(
      Context context) {
    return new Impl(context);
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.implType())
        .superclass(TypeName.get(context.sourceElement().asType()));
    for (Param param : context.parameters()) {
      spec.addField(param.field());
    }
    spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(context.parameters().stream()
            .map(this::parameterMethodOverride)
            .collect(Collectors.toList()));
    return spec.build();
  }

  private MethodSpec parameterMethodOverride(Param param) {
    return MethodSpec.methodBuilder(param.methodName())
        .addAnnotation(Override.class)
        .returns(param.returnType())
        .addModifiers(param.getAccessModifiers())
        .addStatement("return $N", param.field())
        .build();
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    for (Param p : context.parameters()) {
      spec.addStatement("this.$N = $L",
          p.field(), p.coercion().extractExpr());
      spec.addParameter(p.coercion().constructorParam());
    }
    return spec.build();
  }
}
