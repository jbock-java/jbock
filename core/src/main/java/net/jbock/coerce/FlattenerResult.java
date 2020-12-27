package net.jbock.coerce;

import net.jbock.compiler.TypevarMapping;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class FlattenerResult {

  private final List<TypeMirror> typeParameters;

  private final TypevarMapping mapping;

  public FlattenerResult(List<TypeMirror> typeParameters, TypevarMapping mapping) {
    this.typeParameters = typeParameters;
    this.mapping = mapping;
  }

  public List<TypeMirror> getTypeParameters() {
    return typeParameters;
  }

  public TypeMirror substitute(TypeMirror input) {
    return mapping.substitute(input);
  }
}
