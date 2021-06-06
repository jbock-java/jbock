package net.jbock.context;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class SyntEx {

  private final GeneratedTypes generatedTypes;

  private final ParameterSpec message = ParameterSpec.builder(STRING, "message")
      .build();

  @Inject
  SyntEx(GeneratedTypes generatedTypes) {
    this.generatedTypes = generatedTypes;
  }

  public TypeSpec define() {
    return TypeSpec.classBuilder(generatedTypes.syntExType())
        .superclass(Exception.class)
        .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", CodeBlock.of("$S", "serial")).build())
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(message)
            .addStatement("super($N)", message)
            .build())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }
}
