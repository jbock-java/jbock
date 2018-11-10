package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.compiler.Util.snakeToCamel;

public final class BasicInfo {

  public final boolean repeatable;

  public final boolean optional;

  private final LiftedType liftedType;

  private final String paramName;

  private BasicInfo(boolean repeatable, boolean optional, LiftedType liftedType, String paramName) {
    this.repeatable = repeatable;
    this.optional = optional;
    this.liftedType = liftedType;
    this.paramName = paramName;
  }

  static BasicInfo create(boolean repeatable, boolean optional, TypeMirror returnType, String paramName) {
    return new BasicInfo(repeatable, optional, LiftedType.lift(returnType), snakeToCamel(paramName));
  }

  public String paramName() {
    return paramName;
  }

  public TypeMirror returnType() {
    return liftedType.liftedType();
  }

  public TypeMirror originalReturnType() {
    return liftedType.liftedType();
  }

  Function<ParameterSpec, CodeBlock> extractExpr() {
    return liftedType.extractExpr();
  }

  FieldSpec fieldSpec() {
    return FieldSpec.builder(TypeName.get(liftedType.originalType()), paramName, FINAL).build();
  }
}
