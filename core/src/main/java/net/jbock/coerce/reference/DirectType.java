package net.jbock.coerce.reference;

import javax.lang.model.type.DeclaredType;

public class DirectType extends AbstractReferencedType {

  public DirectType(DeclaredType referencedType) {
    super(referencedType);
  }

  @Override
  public String getTypevar(String typeParameter) {
    return typeParameter;
  }
}
