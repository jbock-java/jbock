package net.jbock.coerce;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.compiler.Util.snakeToCamel;

public final class BasicInfo {

  public final boolean repeatable;

  public final TypeMirror returnType;

  private final String paramName;

  private BasicInfo(boolean repeatable, TypeMirror returnType, String paramName) {
    this.repeatable = repeatable;
    this.returnType = returnType;
    this.paramName = paramName;
  }

  static BasicInfo create(boolean repeatable, TypeMirror returnType, String paramName) {
    return new BasicInfo(repeatable, returnType, snakeToCamel(paramName));
  }

  public String paramName() {
    return paramName;
  }

  FieldSpec fieldSpec() {
    return FieldSpec.builder(TypeName.get(returnType), paramName, FINAL).build();
  }
}
