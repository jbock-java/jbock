package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.reference.TypecheckFailure;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class FlattenerResult {

  private List<TypeMirror> typeParameters;

  private TypevarMapping merged;

  FlattenerResult(List<TypeMirror> typeParameters, TypevarMapping merged) {
    this.typeParameters = typeParameters;
    this.merged = merged;
  }

  public List<TypeMirror> getTypeParameters() {
    return typeParameters;
  }

  public Either<TypecheckFailure, TypeMirror> substitute(TypeMirror input) {
    return merged.substitute(input);
  }
}
