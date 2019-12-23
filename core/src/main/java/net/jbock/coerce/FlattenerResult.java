package net.jbock.coerce;

import net.jbock.compiler.TypevarMapping;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class FlattenerResult {

  private List<TypeMirror> typeParameters;
  private TypevarMapping solution;

  FlattenerResult(List<TypeMirror> typeParameters, TypevarMapping solution) {
    this.typeParameters = typeParameters;
    this.solution = solution;
  }

  public TypeMirror get(String key) {
    return solution.get(key);
  }

  public List<TypeMirror> getTypeParameters() {
    return typeParameters;
  }
}
