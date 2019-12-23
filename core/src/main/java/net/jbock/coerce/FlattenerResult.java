package net.jbock.coerce;

import net.jbock.compiler.TypevarMapping;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Objects;

public class FlattenerResult {

  private List<TypeMirror> typeParameters;
  private TypevarMapping solution;

  FlattenerResult(List<TypeMirror> typeParameters, TypevarMapping solution) {
    this.typeParameters = typeParameters;
    this.solution = solution;
  }

  public List<TypeMirror> getTypeParameters() {
    return typeParameters;
  }

  public TypeMirror resolveTypevar(TypeMirror inputType) {
    if (inputType.getKind() != TypeKind.TYPEVAR) {
      return inputType;
    }
    return Objects.requireNonNull(solution.get(inputType.toString()), "assertion failed: null");
  }
}
