package net.jbock.coerce.reference;

class DirectType<E> extends ReferencedType<E> {

  DirectType(Declared<E> referencedType) {
    super(referencedType);
  }

  @Override
  public boolean isSupplier() {
    return false;
  }
}
