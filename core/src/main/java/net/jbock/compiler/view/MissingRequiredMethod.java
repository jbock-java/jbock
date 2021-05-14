package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

class MissingRequiredMethod {

  private final MethodSpec method = missingRequiredMethod();

  @Inject
  MissingRequiredMethod() {
  }

  MethodSpec method() {
    return method;
  }

  private static MethodSpec missingRequiredMethod() {
    ParameterSpec name = builder(STRING, "name").build();
    return methodBuilder("missingRequired")
        .returns(RuntimeException.class)
        .addStatement("return new $T($S + $N)", RuntimeException.class, "Missing required: ", name)
        .addParameter(name)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }
}
