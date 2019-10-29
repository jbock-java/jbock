package net.jbock.coerce.reference;

import javax.lang.model.type.TypeMirror;
import java.util.List;

// mapper or collector
public class ReferencedType<E> {

  private final Declared<E> expectedType; // erases to Function or Collector (see ExpectedType)
  private final boolean supplier; // wrapped in Supplier?

  ReferencedType(Declared<E> expectedType, boolean supplier) {
    this.expectedType = expectedType;
    this.supplier = supplier;
  }

  public boolean isSupplier() {
    return supplier;
  }

  public List<? extends TypeMirror> typeArguments() {
    return expectedType.typeArguments();
  }
}
