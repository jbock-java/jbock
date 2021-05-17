package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl inner class.
 *
 * @see GeneratedClass
 */
@Reusable
public class Impl {

  private final AllParameters context;
  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;

  @Inject
  Impl(AllParameters context, GeneratedTypes generatedTypes, SourceElement sourceElement) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.sourceElement = sourceElement;
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType())
        .superclass(sourceElement.typeName());
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      spec.addField(FieldSpec.builder(c.parameter().returnType(), c.enumName().camel()).build());
    }
    return spec.addModifiers(PRIVATE, STATIC)
        .addMethod(implConstructor())
        .addMethods(context.parameters().stream()
            .map(this::parameterMethodOverride)
            .collect(Collectors.toList()))
        .build();
  }

  private MethodSpec parameterMethodOverride(ConvertedParameter<? extends AbstractParameter> c) {
    AbstractParameter param = c.parameter();
    return MethodSpec.methodBuilder(param.methodName())
        .returns(param.returnType())
        .addModifiers(param.getAccessModifiers())
        .addStatement("return $N", FieldSpec.builder(param.returnType(), c.enumName().camel()).build())
        .build();
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      TypeName returnType = c.parameter().returnType();
      spec.addStatement("this.$N = $L", FieldSpec.builder(returnType, c.enumName().camel()).build(), c.extractExpr());
      spec.addParameter(c.constructorParam());
    }
    return spec.build();
  }
}
