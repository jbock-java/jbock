package net.jbock.coerce;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.compiler.Util.snakeToCamel;

public final class BasicInfo {

  public final TypeMirror returnType;

  private final String paramName;

  BasicInfo(TypeMirror returnType, String paramName) {
    this.returnType = returnType;
    this.paramName = paramName;
  }

  public String paramName() {
    return snakeToCamel(paramName);
  }

  FieldSpec fieldSpec() {
    return FieldSpec.builder(TypeName.get(returnType),
        snakeToCamel(paramName))
        .addModifiers(FINAL)
        .build();
  }
}
