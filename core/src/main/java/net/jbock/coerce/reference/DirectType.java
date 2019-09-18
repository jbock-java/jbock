package net.jbock.coerce.reference;

import javax.lang.model.type.DeclaredType;

class DirectType extends AbstractReferencedType {

  DirectType(DeclaredType referencedType) {
    super(referencedType);
  }

  @Override
  public String getTypevar(String typeParameter) {
    return typeParameter;
  }

  @Override
  public boolean isSupplier() {
    return false;
  }
}
