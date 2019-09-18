package net.jbock.coerce.reference;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Objects;

class SupplierType extends AbstractReferencedType {

  private final Map<String, TypeMirror> typevarMapping;

  SupplierType(DeclaredType referencedType, Map<String, TypeMirror> typevarMapping) {
    super(referencedType);
    this.typevarMapping = typevarMapping;
  }

  @Override
  public String getTypevar(String typeParameter) {
    return Objects.toString(typevarMapping.get(typeParameter), typeParameter);
  }

  @Override
  public boolean isSupplier() {
    return true;
  }
}
