package net.jbock.coerce.reference;

import javax.lang.model.type.TypeMirror;
import java.util.List;

// mapper or collector
public class ReferencedType<E> {

  private final List<? extends TypeMirror> typeArguments;
  private final boolean supplier; // wrapped in Supplier?

  ReferencedType(List<? extends TypeMirror> typeArguments, boolean supplier) {
    this.typeArguments = typeArguments;
    this.supplier = supplier;
  }

  public boolean isSupplier() {
    return supplier;
  }

  public List<? extends TypeMirror> typeArguments() {
    return typeArguments;
  }
}
