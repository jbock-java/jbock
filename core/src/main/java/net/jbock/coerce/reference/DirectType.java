package net.jbock.coerce.reference;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

class DirectType extends AbstractReferencedType {

  DirectType(DeclaredType referencedType) {
    super(referencedType);
  }

  @Override
  public Map<String, TypeMirror> mapTypevars(Map<String, TypeMirror> solution) {
    return solution;
  }

  @Override
  public boolean isSupplier() {
    return false;
  }
}
