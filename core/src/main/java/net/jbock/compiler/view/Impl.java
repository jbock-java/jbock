package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.ConvertedParameter;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.AbstractParameter;

import javax.inject.Inject;
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

  private final GeneratedTypes generatedTypes;

  @Inject
  Impl(Context context, GeneratedTypes generatedTypes) {
    this.context = context;
    this.generatedTypes = generatedTypes;
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType())
        .superclass(generatedTypes.sourceType());
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      spec.addField(FieldSpec.builder(c.parameter().returnType(), c.enumName().camel()).build());
    }
    return spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor(context))
        .addMethods(context.parameters().stream()
            .map(Impl::parameterMethodOverride)
            .collect(Collectors.toList()))
        .build();
  }

  private static MethodSpec parameterMethodOverride(ConvertedParameter<? extends AbstractParameter> c) {
    AbstractParameter param = c.parameter();
    return MethodSpec.methodBuilder(param.methodName())
        .returns(param.returnType())
        .addModifiers(param.getAccessModifiers())
        .addStatement("return $N", FieldSpec.builder(param.returnType(), c.enumName().camel()).build())
        .build();
  }

  private static MethodSpec implConstructor(Context context) {
    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      TypeName returnType = c.parameter().returnType();
      spec.addStatement("this.$N = $L", FieldSpec.builder(returnType, c.enumName().camel()).build(), c.extractExpr());
      spec.addParameter(c.constructorParam());
    }
    return spec.build();
  }
}
