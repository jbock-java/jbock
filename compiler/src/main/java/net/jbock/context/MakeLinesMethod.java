package net.jbock.context;

import com.squareup.javapoet.MethodSpec;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.LIST_OF_STRING;

@ContextScope
public class MakeLinesMethod {


  @Inject
  MakeLinesMethod() {
  }

  MethodSpec get() {
    return methodBuilder("makeLines")
        .addModifiers(PRIVATE)
        .returns(LIST_OF_STRING)
        .build();
  }
}
