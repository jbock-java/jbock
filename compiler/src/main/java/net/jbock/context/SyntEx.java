package net.jbock.context;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.util.ConverterFailure;
import net.jbock.util.ItemType;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

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
        .superclass(RuntimeException.class)
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(message)
            .addStatement("super($N)", message)
            .build())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }
}
