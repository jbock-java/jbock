package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.SourceElement;
import net.jbock.scope.ContextScope;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl inner class.
 *
 * @see GeneratedClass
 */
@ContextScope
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
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
    if (sourceElement.isInterface()) {
      spec.addSuperinterface(sourceElement.typeName());
    } else {
      spec.superclass(sourceElement.typeName());
    }
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      spec.addField(c.asField());
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
        .addStatement("return $N", c.asField())
        .build();
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    for (ConvertedParameter<? extends AbstractParameter> c : context.parameters()) {
      FieldSpec field = c.asField();
      spec.addStatement("this.$N = $N", field, c.asParam());
      spec.addParameter(c.asParam());
    }
    return spec.build();
  }
}
