package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.reference.TypecheckFailure;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class FlattenerResult {

  private final List<TypeMirror> typeParameters;

  private final TypevarMapping mapping;

  FlattenerResult(List<TypeMirror> typeParameters, TypevarMapping mapping) {
    this.typeParameters = typeParameters;
    this.mapping = mapping;
  }

  public List<TypeMirror> getTypeParameters() {
    return typeParameters;
  }

  public Either<TypecheckFailure, TypeMirror> substitute(TypeMirror input) {
    return mapping.substitute(input);
  }
}
